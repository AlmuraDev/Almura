# Content loader on 4.0

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
| `block/container` | 69 | later: needs the container feature |
| `block/crop`, `item/seed` | 85 + 85 | done: 3.5's growth (chance, light, temperature, heat lamp, hydration, rollback, even fields), bone meal at the content's chance, drops by stage, seeds from grass, 3.5's farmland; aquatic crops and harvesting sacks later |
| `block/leaf`, `block/sapling`, `tree` | 42 + 25 + 25 | later: decay, spread, tree growth |
| `generation`, `grass`, `material`, `block_sound_group` | 27 + 1 + 1 + 1 | later: world generation is datapack based in 26.3 |
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
- Aquatic crops (3: two `surface`, one `submerged`): planted on their seed's soil like land crops for now; 3.5 planted them in water.
- Harvesting sacks (`sack_harvest` on 11 crops): the sack item isn't ported yet.
- Seeds from grass: 3.5's hook asked Forge for a grass seed first, and Forge's list always held wheat, so its Almura seed roll never
  ran. 4.0 does what that code meant: one break in eight, when vanilla's wheat seed didn't drop, picks a random Almura seed and drops
  it at its `grass` chance (`GrassSeeds`). Say if the live server should keep 3.5's actual behaviour (no Almura seeds from grass).
