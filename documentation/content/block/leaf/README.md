# Leaf config: `prevent_decay`

This branch (`perf/leaf-ticks`) changes how leaf blocks tick and brings back the `prevent_decay` setting.

## Do I have to change any leaf configs?

**No.** Every existing leaf config keeps working as it is. `prevent_decay` is optional, and it does nothing until you add it to a leaf.

Leaf configs live under `assets/almura/content/block/leaf/` in the master asset directory on the server. The game loads them from `assets/fs_5` at runtime, so edit the server copy.

## What changed

1. **`prevent_decay` has its own key.**
   - Before, the code read it from the `spread` node by mistake, and its effect was commented out, so it never did anything.
   - It is now read from a separate `prevent_decay` node, and it works.
2. **`spread` behaves the same.** Fruit leaves still spread at the same chance, onto the first air block they find. The only difference: they no longer load a neighbouring chunk to look for air. Neighbours in unloaded chunks are skipped.
3. **Decay waits for the area to load.**
   - A leaf near the edge of loaded terrain now waits until the area around it is loaded before deciding whether to decay.
   - Before, it could decay based on the previous leaf's scan.
   - Vanilla logs still keep leaves alive exactly as before.

## The `prevent_decay` setting

Add it inside a leaf's state (for example `state.normal`), next to `spread`:

```json
"prevent_decay": {
    "chance": [
        {
            "biome": [ "minecraft", "terraincontrol" ],
            "min": 80,
            "max": 100
        },
        {
            "min": 50,
            "max": 50
        }
    ],
    "block": "almura:leaf/cherry_tree_fruit_leaf"
},
```

| Field | Required | Meaning |
|---|---|---|
| `chance` | yes | The percentage chance (0–100) that `block` keeps this leaf alive. It uses the same format as `spread.chance`. Entries with `biome` apply only to those biomes. The entry without `biome` is the default for every other biome. When `min` and `max` differ, a value between them is picked at random on each roll. |
| `block` | yes | The block that acts like a log for this leaf when the roll passes. It uses the same format as `spread.block`. |

If either field is missing, `prevent_decay` is ignored.

### How it works

- **When it runs:** a leaf runs a decay check when the blocks around it change, for example when a log is broken.
- **The roll:** on each check, `chance` is rolled **once**, using the biome the leaf is in.
- **If the roll passes:** every matching `block` within 4 blocks counts as a log for that check, and connected leaves stay alive.
- **Logs still come first:** vanilla logs, and any block that sustains leaves, are checked before `prevent_decay`.
- **If the roll fails:** only real logs count. A leaf with no log within reach decays, as it does in vanilla.

## Per-leaf checklist

None of these need a change. The last column is a possible use for `prevent_decay` if you want one. It is not a requirement.

| Leaf config | Has `spread` (spread block) | Required change | Optional `prevent_decay` idea |
|---|---|---|---|
| `afledos_tree_leaf.json` | no | none | none |
| `cherry_tree_leaf.json` | yes (`almura:leaf/cherry_tree_fruit_leaf`) | none | `block`: `almura:leaf/cherry_tree_fruit_leaf` |
| `cherry_tree_fruit_leaf.json` | no | none | none |
| `dark_ephedra_tree_leaf.json` | no | none | none |
| `doku_tree_leaf.json` | no | none | none |
| `fraze_tree_leaf.json` | no | none | none |
| `grape_fruit_tree_leaf.json` | yes (`almura:leaf/grape_fruit_tree_fruit_leaf`) | none | `block`: `almura:leaf/grape_fruit_tree_fruit_leaf` |
| `grape_fruit_tree_fruit_leaf.json` | no | none | none |
| `green_apple_tree_leaf.json` | yes (`almura:leaf/green_apple_tree_fruit_leaf`) | none | `block`: `almura:leaf/green_apple_tree_fruit_leaf` |
| `green_apple_tree_fruit_leaf.json` | no | none | none |
| `juko_tree_leaf.json` | no | none | none |
| `kiwi_tree_leaf.json` | yes (`almura:leaf/kiwi_tree_fruit_leaf`) | none | `block`: `almura:leaf/kiwi_tree_fruit_leaf` |
| `kiwi_tree_fruit_leaf.json` | no | none | none |
| `lemon_tree_leaf.json` | yes (`almura:leaf/lemon_tree_fruit_leaf`) | none | `block`: `almura:leaf/lemon_tree_fruit_leaf` |
| `lemon_tree_fruit_leaf.json` | no | none | none |
| `light_ephedra_tree_leaf.json` | no | none | none |
| `liku_tree_leaf.json` | no | none | none |
| `lime_tree_leaf.json` | yes (`almura:leaf/lime_tree_fruit_leaf`) | none | `block`: `almura:leaf/lime_tree_fruit_leaf` |
| `lime_tree_fruit_leaf.json` | no | none | none |
| `morbus_tree_leaf.json` | no | none | none |
| `orange_tree_leaf.json` | yes (`almura:leaf/orange_tree_fruit_leaf`) | none | `block`: `almura:leaf/orange_tree_fruit_leaf` |
| `orange_tree_fruit_leaf.json` | no | none | none |
| `peach_tree_leaf.json` | yes (`almura:leaf/peach_tree_fruit_leaf`) | none | `block`: `almura:leaf/peach_tree_fruit_leaf` |
| `peach_tree_fruit_leaf.json` | no | none | none |
| `plum_tree_leaf.json` | yes (`almura:leaf/plum_tree_fruit_leaf`) | none | `block`: `almura:leaf/plum_tree_fruit_leaf` |
| `plum_tree_fruit_leaf.json` | no | none | none |
| `walnut_tree_leaf.json` | yes (`almura:leaf/walnut_tree_fruit_leaf`) | none | `block`: `almura:leaf/walnut_tree_fruit_leaf` |
| `walnut_tree_fruit_leaf.json` | no | none | none |

This list was taken from the runtime copy in `run/assets/fs_5`. If the server's master copy has more leaves than this, the same rule applies to them: no change is required.

Fruit leaves are already leaves, so they connect leaves together during the decay scan. Making the fruit leaf a `prevent_decay` block goes further: it lets fruit leaves hold up a tree on their own, with no log nearby.
