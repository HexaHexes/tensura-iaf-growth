# Tensura - Ice and Fire

A Paxi datapack for the **Tensura RPG** pack (MC 1.21.1) that gives Ice and Fire
equipment a full evolution progression along Tensura's own EP ladder, including
**branching evolutions** - one item can forge into several different sets, rolled
at random when it evolves.

Ice and Fire gear climbs at exactly the same pace as Tensura gear, using
Tensura's own EP thresholds, and each rung is engraved the way Tensura engraves
its own equipment at that power level.

## Armor chain

`Sheep Disguise -> Copper -> Silver -> Troll Leather -> Death Worm Chitin ->
Sea Serpent Scale -> Dragon Scale -> Dragon Steel`

| Step | Evolves at | Outcome |
|---|---|---|
| Sheep Disguise | 6,000 | Copper |
| Copper | 10,000 | Silver |
| Silver | 18,000 | **Random** of the three troll leather sets (mountain / forest / frost) |
| Troll Leather | 45,000 | The paired death worm chitin: mountain -> red, forest -> yellow, frost -> white |
| Death Worm Chitin | 52,000 | **Random** of the seven sea serpent (Tide Guardian) sets |
| Sea Serpent Scale | 80,000 | **Random** of the twelve dragon scale sets |
| Dragon Scale | 225,000 | Dragon steel of that dragon's element (fire / ice / lightning) |
| Dragon Steel | 750,000 | Top of the ladder - keeps earning EP |

All three death worm chitin sets are the same tier, so any of them can reach any
of the seven sea serpent sets, and any sea serpent set can reach any of the
twelve dragon scale sets. Dragon scale armor then always forges into the dragon
steel set of its own element: red, green, bronze and gray are fire dragons;
blue, white, sapphire and silver are ice; electric, amethyst, copper and black
are lightning.

## Tools and weapons chain

`Copper -> Silver -> Dragon Bone -> (Blooded Dragon Bone) -> Dragon Steel`

| Step | Evolves at | Outcome |
|---|---|---|
| Copper | 6,000 | Silver |
| Silver | 18,000 | Dragon Bone |
| Dragon Bone sword | 52,000 | **Random** blooded (elemental) dragon bone sword |
| Dragon Bone axe / pickaxe / shovel / hoe | 52,000 | **Random** dragon steel of one of the three elements |
| Blooded Dragon Bone sword | 225,000 | Dragon steel sword of the matching element |
| Dragon Steel | 750,000 | Top of the ladder - keeps earning EP |

The dragon bone bow tops out on its own, since there is no dragon steel bow.

## Other gear

- **Dread line:** Dread Thrall's Sword -> Dread Knight's Sword -> Blade of the
  Dreaded Queen, at 18,000 and 52,000 EP.
- **Death worm gauntlets:** yellow -> white -> red, at 18,000 and 52,000 EP.
- **Uniques** (ghost sword, lich staff, tide trident, pixie wand, siren flute,
  cockatrice scepter, cyclops eye, gorgon head, hippocampus slapper,
  hippogryph sword, fishing spear, stymphalian dagger, amphithere macuahuitl,
  blindfold, earplugs and the dread queen's staff) are terminal and keep
  earning EP so they stay usable at endgame.

## Engravings

Evolving grants Tensura enchantment engravings the way Tensura does it on its
own gear:

| Tier | Engraving |
|---|---|
| Silver weapons and silver armor | Holy Coat I |
| Blooded dragon bone swords | Elemental Boost I |
| Sea serpent (Tide Guardian) armor | Breathing Support I |
| Dragon scale armor | Elemental Resistance I |
| Dragon steel weapons and tools | Tsukumogami I |
| Dragon steel armor | Holy Coat II |

## Movesets

I&F weapons are given BetterCombat movesets matching their class - claymores for
the dread blades, rapiers for the ghost sword, heavy axes for the axes, spears
and tridents for the polearms, staves for the casters, fists for the gauntlets.
These files only take effect when BetterCombat is installed; without it they are
ignored.

## Required configuration for random branches

Branching is implemented through Gear Evolution's addon chains, and the mod
picks which branch to take using its own strategy setting. For a genuinely
random roll, set this in `config/gearevolution-common.toml` (or the in-game
Cloth Config screen) on **every** server and client:

```toml
branchingDefaultStrategy = "weighted_random"
```

Left at the default `"first"`, every branch resolves to the first listed option
instead (the chains still work, but the roll is fixed). The other modes are
`deterministic_random` (one fixed result per item type) and `family_preference`
(always the first option whose gear family is enabled).

## Dependencies

| Requirement | Notes |
|---|---|
| Tensura: Reincarnated | Provides the EP and evolution systems this pack writes into |
| Tensura: Gear Evolution | Reads the chain and addon chain files; required for branching |
| Ice and Fire: Community Edition | Every file targets the `iceandfire` item ids - meaningless without it |
| Paxi | Auto-loads the pack from `config/paxi/datapacks/` |
| BetterCombat (optional) | Only needed for the weapon movesets |

## Tuning

Thresholds, branch outcomes, engravings and movesets are all generated from
`tools/generate-content.ps1`. Change the values there and rerun the script; it
refuses to write a file whose evolution target does not exist and verifies that
every item Gear Evolution knows about is covered.

## Credits

- Built on [Tensura: Reincarnated](https://www.curseforge.com/minecraft/mc-mods/tensura)
  and [Tensura: Gear Evolution](https://www.curseforge.com/minecraft/mc-mods/tensura-gear-evolution).
- All Ice and Fire items belong to Ice and Fire: Community Edition.
- Movesets follow BetterCombat.

## License

GNU Lesser General Public License v2.1 - see [`LICENSE`](LICENSE).
Copyright (c) 2026 Bitwise Madness.
