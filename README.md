# Loot All

<img width="768" height="456" alt="Loot All Banner" src="https://github.com/user-attachments/assets/75fb9464-ed5a-4dd9-8ec0-dffa9be2454f" />


Loot All allows you to loot all nearby containers around you with a single button.

By pressing the keybind (default `-`) all nearby loot containers are instantly emptied into your inventory or storage system.

- Loot chests, barrels, and any other loot-table containers in a configurable radius.
- Fully Lootr compatible.
- Auto-loot mode that clears nearby containers on a timer, hands-free.
- Text on screen shows how many items were collected and how many containers were looted.

## Loot Transfer

Look at any storage block or item in your inventory and press `=` to set it as your transfer target. From then on, everything you loot is routed directly into that storage, across any distance and even dimensions.

- Bind a block or an item — a chest, drawer, ME terminal, backpack, wireless grid, anything!
- Works from any distance and any dimension (configurable).
- Items are never voided. Anything that doesn't fit overflows to your inventory, then the ground.
- HUD text tells you where your loot went: *"Transferred to ..."*.

## Mod Support

Loot Transfer works with any block or item that uses a standard inventory. Anything else is shipped with dedicated support.

| Mod | Supported Targets |
| --- | --- |
| Applied Energistics 2 | ME Network |
| Refined Storage | Network + Wireless Grid |
| Mekanism | QIO Dashboard / Drive Array + Portable QIO Dashboard |
| Tom's Simple Storage | Storage Terminal + Inventory Connector |
| Simple Storage Network | Master / Request Table |
| Pretty Pipes | Item Terminal |
| ProjectE | EMC — Klein Stars, Transmutation Table & Tablet |

## Configuration

- **range** — How far to search for containers (default 20).
- **includeMinecarts** — Loot minecarts with loot tables.
- **autoLooting** & **autoLootingTimer** — Loot nearby containers on a timer.
- **excludeBlockedContainers** — Skip chests that are blocked from opening.
- **feedbackMessage** & **playSound** — Toggle the feedback and sound.
- **enableLootingTransfer** — Turn the transfer system on/off.
- **maxLootTransferDistance**, **transferRequireSameDimension**, **transferRequireLoadedChunk** — Transfer configs.

## Item Filters

Choose what gets picked up. These filters apply to everything.

- **skipList** — Items to never loot. Each entry can be:
  - `modid:item` — a single item (e.g. `minecraft:stick`)
  - `#modid:tag` — an item tag (e.g. `#minecraft:stairs`)
  - `@modid` — every item from a mod (e.g. `@alexsmobs`)
    - skipListMode:
- **BLACKLIST (default)** - loot everything except what's in skipList.
- **WHITELIST** - loot only what's in skipList.
- **skipArmorAndTools** — Skip all armor, tools, and weapons.
- **skipNonStackable** — Skip all items that only stack to 1.
- **skipUnenchantedGear** — Skip armor, tools, and weapons *unless* they're enchanted.
- **rarityFilterMode** & **rarityList** — Filter by rarity tier. Set the mode to `ONLY` (loot only the listed tiers) or `SKIP` (loot everything except them).

## Game Stages Support

You can gate Loot All's abilities with the [Game Stages](https://www.curseforge.com/minecraft/mc-mods/game-stages) mod.

## Default Keybinds

- `-` — Loot nearby containers.
- `=` — Set & clear transfer target.
