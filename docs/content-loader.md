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
| `item_group` | 24 | first slice |
| `item/normal` | 186 | first slice |
| `item/food` | 413 | first slice (potion effects, `always_edible`, `container` returned) |
| `block/normal` | 3 | first slice |
| `block/horizontal` | 764 | first slice (facing, rotated shapes) |
| `block/log` | 22 | next |
| `block/flower` | 99 | next |
| `block/slab`, `item/slab` | 220 + 110 | next |
| `block/stair` | 128 | next |
| `block/container` | 69 | later: needs the container feature |
| `block/crop`, `item/seed` | 85 + 85 | later: growth, hydration, fertilizer |
| `block/leaf`, `block/log`, `block/sapling`, `tree` | 42 + 22 + 25 + 25 | later: decay, spread, tree growth |
| `item/tool_hoe`, `item/tool_sickle` | 5 + 5 | later |
| `generation`, `grass`, `material`, `block_sound_group` | 27 + 1 + 1 + 1 | later: world generation is datapack based in 26.3 |
| `flicker`, `drip` (block state options) | 13 + 5 | later: client effects |

## Things that changed underneath

* 1.12's materials (`minecraft:rock`, `minecraft:wood` and so on) are gone; each becomes the properties it implied (sound, whether a
  tool is needed to drop, flammability, whether it is replaced like plants).
* Item ids with metadata (`minecraft:dye` with `data: 15`) were flattened in 1.13; recipes that use them need a mapping (bone meal and
  so on), kept in `convert/LegacyItems`.
* 1.12 texture names (`minecraft:blocks/planks_oak`) were renamed (`minecraft:block/oak_planks`); `convert/LegacyTextures` maps them.
* The Forge `forge:default-block` transform no longer exists; vanilla's block display is used.
