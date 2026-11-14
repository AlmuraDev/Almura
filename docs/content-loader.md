# Content loader on 4.0

4.0 reads the 4.0 content: the 3.0 content with its mistakes fixed and its OBJs in NeoForge's conventions, made by
`tools/content-4.0/fix_content.py` (`--source` the 3.0 content, `--out` the 4.0 content). Dev runs and tests read `runs/content-4.0`
(`-PalmuraContent=<folder>` for another); its home is `H:\Almura 3.0\ContentLoader\4.0`. The loader converts 1.12's formats (Forge
blockstates, 1.12 names, recipes and loot) but repairs nothing: `docs/content-fixes.md` lists what's still to decide.

How Almura 4.0 (Minecraft 26.3, NeoForge) loads the content Almura 3.x loaded on 1.12.2: the blocks, items, creative tabs, recipes,
translations, models and textures in the content repository (`H:\Almura 3.0\ContentLoader\3.0\fs_2`, copied to
`<game>/assets/fs_5` on clients and servers, as on 1.12).

## The approach

The content stays in its 1.12 format, so the content repository keeps one format and its tools (and whoever edits it) carry on as they
are. 4.0 reads it at startup and turns it into what 26.3 wants:

| 1.12 content | 26.3 |
| --- | --- |
| `content/item_group/*.json` | creative mode tabs, registered |
| `content/item/**.json` | items, registered (food as food and consumable components) |
| `content/block/**.json` | blocks and their block items, registered |
| `content/**/_translations/en_us.json` | a generated language file (`lang/en_us.json`) |
| `content/**/_recipes/*.json` | generated recipes (`data/almura/recipe/`), in 26.3's format |
| block `action.destroy` drops | generated loot tables (`data/almura/loot_table/blocks/`) |
| block `effective_tools` | generated tags (`mineable/pickaxe` and so on) |
| `blockstates/**.json` (Forge's `forge_marker` format) | generated 26.3 blockstates |
| `models/**.json` | passed through; 1.12 texture names renamed to 26.3's |
| `models/**.obj` + `.mtl` | passed through, loaded with NeoForge's OBJ loader through generated wrapper models |
| `textures/**` | passed through |
| (none) | generated item model definitions (`items/*.json`, new in 26.3) |

The generated and passed-through files are served from one pack built in memory (`almura_content`), added for both resources (client)
and data (server and client), always on and above vanilla. Nothing is written into the content folders.

Registries are fixed early in 26.3: the content is read when the mod is constructed and registered in `RegisterEvent`, before the
registries freeze. A client and a server must hold the same content to connect (NeoForge checks the registries match).

## Identifiers

As on 1.12, a content file's id is its path under its type folder, without `.json`: `content/item/normal/ingredient/aniseseed.json` is
`almura:normal/ingredient/aniseseed`. Folders starting with `_` hold translations and recipes, not content.

Translations are keyed by the file's name in its folder (`aniseseed.name`, `aniseseed.tooltip`); they become
`item.almura.normal.ingredient.aniseseed` (`block.almura.…` for blocks) and the same with `.tooltip`.

## Code

`com.almuradev.almura.content`:

* `source` reads the content folders (no Minecraft classes, so it is tested against the real content).
* `def` holds what was read: item groups, items, blocks.
* `convert` turns 1.12 JSON into 26.3 JSON: blockstates, models, item model definitions, recipes, loot tables, tags, language
  (no Minecraft classes either; tested against the real content).
* `registry` registers creative tabs, items and blocks from the definitions.
* `pack` serves the generated and passed-through files as a pack.

## Content types

| Type | Files | State |
| --- | --- | --- |
| `item_group` | 23 | done: creative tabs |
| `item/normal` | 186 | done (3 tools have no item model in the content) |
| `item/food` | 413 | done: hunger, saturation, healing (`almura:heal`), potion effects at their chance, containers handed back |
| `item/tool_hoe` | 5 | done: hoes of their tier |
| `item/tool_sickle` | 5 | done as tools; `effective_on` is the tag `almura:mineable/sickle`, crops included |
| `block/normal` | 3 | done |
| `block/horizontal` | 764 | done: facing, shapes turned with it |
| `block/log` | 22 | done: on their axis |
| `block/flower` | 99 | done: on earth, no collision |
| `block/stair` | 128 | done: made of their `block` |
| `block/slab`, `item/slab` | 220 + 110 | done: one 26.3 slab per single slab, its double state the 1.12 double slab |
| `block/container` | 69 | done: a chest's screen of their slots, contents kept in the dropped item (spilled when broken sneaking), the crop barrels take their seed and become the storage barrel when opened, the storage barrel takes seeds, crops and tree fruit and is named for what it holds; its label on the barrel later |
| `block/crop`, `item/seed` | 85 + 85 | done: 3.5's growth (chance, light, temperature, heat lamp, hydration, rollback, even fields), bone meal at the content's chance, drops by stage, seeds from grass, 3.5's farmland, aquatic crops in water, the harvesting sack |
| `block/leaf`, `block/sapling`, `tree` | 42 + 25 + 25 | done: leaves decay away from logs (vanilla's distance, 6 blocks; 1.12's was 4) with their decay drops at the biome's chance, fruit spreads into the air beside them, shears and silk touch take the leaf; saplings grow 3.5's trees (normal, big, palm) with fruit leaves at the tree's chance; ranges chosen by biome as 3.5 chose them |
| `generation`, `grass` | 27 + 1 | done: trees, the grass mix (wild flowers and crops) and ores in the overworld, holding vanilla's trees, grass and flowers back where they go, as 3.5 did |
| `material`, `block_sound_group` | 1 + 1 | later: blocks map 1.12's names to 26.3's materials and sounds; the content's own definitions aren't read |
| `flicker`, `drip` (block state options), blockstate `transform`s | 13 + 5 + 121 | later: client effects and item display |

`ContentConversionTest` reads the copied content and checks the pack; `ContentRegistrationTest` and `ContentDataTest` start the game's
mod loading (no window, no server) and check the registries and that 26.3 reads every recipe and loot table. `./gradlew test` writes
`build/content-report.txt` with what didn't convert and why.

## Things that changed underneath

* 1.12's materials (`minecraft:rock`, `minecraft:wood` and so on) are gone; each becomes the properties it implied (sound, whether a
  tool is needed to drop, flammability, whether it is replaced like plants).
* Item ids with metadata (`minecraft:dye` with `data: 15`) were flattened in 1.13; recipes that use them need a mapping (bone meal and
  so on), kept in `convert/LegacyItems`.
* 1.12 texture names (`minecraft:blocks/planks_oak`) were renamed (`minecraft:block/oak_planks`); `convert/LegacyTextures` maps them.
* The Forge `forge:default-block` transform no longer exists; vanilla's block display is used.

## To clean up later

- OBJ break particles. Each OBJ block's particle is cut from its texture sheet as 3.5 chose it: the patch under the first and
  third corners of the model's last face, scaled to 16x16 (`ContentPackBuilder.objParticle`). That's better than the whole sheet,
  but the last face is often a detail (the Tile Topped Bar corner breaks yellow). Pick a face that shows what the block is made of,
  such as the largest face or the top, or let the content name its particle texture.
- The Agriculture tab icon (an OBJ tool) shows tiny in its slot; OBJ items need slot and hand positions of their own.
- Seeds from grass: 3.5's hook asked Forge for a grass seed first, and Forge's list always held wheat, so its Almura seed roll never
  ran. 4.0 does what that code meant: one break in eight, when vanilla's wheat seed didn't drop, picks a random Almura seed and drops
  it at its `grass` chance (`GrassSeeds`). Say if the live server should keep 3.5's actual behaviour (no Almura seeds from grass).
- `flammability` and `fire_spread_speed` (25 leaves set them, at vanilla's 30 and 60) aren't read yet, so content blocks don't catch
  fire from a neighbouring fire.
- Leaf decay reaches 6 blocks from a log (26.3's leaves), not 1.12's 4; the content's trees are well inside either.
- World generation (`generation/feature/tree`, `generation/feature/grass`, `grass`, `generation/underground/ore`): done as 3.5 did it on
  1.12's decoration events. Once a chunk, the first tree generation whose biome chance passes grows its tree and vanilla grows no trees
  there; twice a chunk (flowers, then grass) the grass mix may be scattered and vanilla then places no flowers (or grass) there; ores
  are vanilla ore features (marble and salt: 5 veins of 10 a chunk, height 0 to 63, in stone). Overworld biomes only. 1.12's biome
  names are mapped to 26.3's (`swampland` is `swamp`, `roofed_forest` is `dark_forest`, the hills and "mutated" biomes are what they
  became). The `terraincontrol` entries match nothing until there is a TerrainControl.
- Wild crops: the grass mix holds ripe crops. On 1.12 a crop could only stand on farmland, so 3.5 most likely never placed them; 4.0
  lets a ripe crop stand on grass, so they appear in the wild. Seeds still plant only on their soil.
- Double grass and double flowers in the grass mix are placed whole (both halves); 1.12 placed only the lower half, which fell.
- The storage barrel's label (3.5 drew the first slot's item on paper on the barrel's sides) isn't drawn yet.
- A container broken in creative drops nothing, so what it held is lost, as on 3.5.
- Aquatic crops (`aquatic`: rice and wasabi `surface`, seaweed `submerged`): as 3.5, planted on a bed (sand, gravel, dirt,
  farmland or clay) into still water, always watered there; seaweed needs water above it, rice and wasabi air. 3.5 made them water
  material blocks with a fixed water level and drew rice's water surface in its model (`wet`, `crop/common/water_surface`); 26.3
  holds a water source in the block instead (`getFluidState`: seaweed always, rice and wasabi `waterlogged` in water), draws the
  water itself and leaves it when the crop goes, so the content's `wet` submodel is unused. Buckets can't take the water. Wild rice
  from the grass mix stands dry on grass.
- The harvesting sack (`almura:normal/tool/harvesting_sack`, `feature/harvestsack`): Almura's own item, as 3.5's; its model and
  recipe are the content's. Right click a ripe crop with `sack_harvest` (11) to pick it: a real player break (protection can deny
  it), its drops into the sack, the crop back at its `reset_age` (in its water for an aquatic one). Right click anything but a crop,
  the ground or a plant to open it: a single chest of 27 slots taking only what those crops drop, kept on the sack
  (`minecraft:container`). 3.5 also posted Sponge's drop event for Skills' XP; Skills isn't on 4.0 yet.
- The Farmer's Almanac and Skills Handbook (`almura:normal/tool/farmers_almanac`, `skills_handbook`; `feature/almanac`,
  `feature/handbook`): Almura's own items, with 3.5's models and textures in the jar (the content has none); their recipes are
  the content's. The almanac (permission `almura.item.farmers_almanac`, everyone's by default) shows a field spot's needs and
  conditions on farmland, a crop or a pipe, and the list of every crop otherwise, as 3.5's. Not yet: the Skills farming levels
  (no Skills on 4.0) and a crop's growth stages drawn as 3D models (Strata has no block model visual yet). The handbook reads the
  Skills plugin, so on 4.0 it says the server has no skills, as 3.5 did on a server without it.
- Agriculture pipes (`horizontal/agriculture`, 10): as 3.5's. `rotate_placement` turns one placed against the side of a block
  (the end of another pipe) to connect: `true`/`across` continues out from it (straight pipes, end caps), `toward` points the
  branch into it (T pipes), `corner` puts one arm into it and the other toward the player's side; on a floor or ceiling it faces
  the player. `drip` drips water from the underside (the ceiling pipes). Any agriculture block keeps farmland 6 around and 1 to 6
  below it wet (`FarmlandBlockMixin`), and the Farmer's Almanac shows a pipe's range and whether a field is in range.
- Cruets (`almura:normal/ingredient/glass_cruet`, `glass_water_cruet`, `glass_milk_cruet`): Almura's own items (`feature/cruet`), as
  3.5's; their models and the recipes using them are the content's. The glass cruet takes up water as a bucket does and is filled with
  milk from a cow; the water cruet pours; recipes hand the glass cruet back. Almura's pumpkin pie (with a milk cruet) replaces vanilla's
  (`data/minecraft/recipe/pumpkin_pie.json`). 3.5's fluid handler (pipes and tanks filling and emptying cruets) isn't ported.
