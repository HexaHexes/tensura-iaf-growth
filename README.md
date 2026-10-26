# Tensura - Ice and Fire Growth

A NeoForge mod for the **Tensura RPG** pack (MC 1.21.1) that does two things: it
lets Ice and Fire dragons grow all the way up instead of stopping short, and it
gives Ice and Fire equipment a full evolution progression along Tensura's own EP
ladder, including **branching evolutions** - one item can forge into several
different sets, rolled at random when it evolves.

Ice and Fire gear starts on the vanilla progression - the same early pace as
leather, iron and diamond gear - and from there joins Tensura's own metals
ladder, so every rung climbs where Tensura gear of that tier climbs. Each rung
is engraved the way Tensura engraves its own equipment at that power level.

## Dragon growth

- **The growth cap is raised.** Ice and Fire stops a tamed dragon growing at 128
  days of age; this mod widens that ceiling (up to 4096), so a hatchling keeps
  growing for as long as you keep feeding it. The default is unchanged - dragons
  behave exactly as before until the cap is raised.
- **The ladder stretches with the cap.** Everything that decides when a dragon
  reaches its next stage is scaled to the cap, so raising it does not skip a
  stage or compress one: the same progression simply takes longer. Endpoints are
  identical, only the timing moves.
- **Growth stage is the EP lever.** How much EP a dragon earns is set by its
  stage: 15% as a hatchling, then 30%, 50%, 75%, and the full rate once fully
  grown. Growing one up is the main way to make it strong.
- **Dragons fight with their element.** A dragon's melee hits carry its natural
  element - fire dragons burn, ice dragons chill, lightning dragons shock - so
  resistances and weaknesses apply the way they do to any other elemental attack.
- **Dragons gain a secondary element.** Fire dragons are granted light
  manipulation, ice dragons water manipulation, lightning dragons wind
  manipulation, alongside their existing breath and resistances.

## Mob strength bands

Every Ice and Fire mob's magicule and aura band is rebalanced so it sits where
its threat level belongs - death worms and sirens at the low end, the dread
ramp climbing form by form, cyclops and sea serpents in the middle, and all
three dragons at the top of the ladder. Spiritual health and each mob's
abilities are unchanged.

## Armor chain

`Sheep Disguise -> Copper -> Silver -> Troll Leather -> Death Worm Chitin ->
Sea Serpent Scale -> Dragon Scale -> Dragon Steel`

| Step | Evolves at | Outcome |
|---|---|---|
| Sheep Disguise | 550 | Copper |
| Copper | 3,200 | Silver |
| Silver | 7,800 | **Random** of the three troll leather sets (mountain / forest / frost) |
| Troll Leather | 18,000 | The paired death worm chitin: mountain -> red, forest -> yellow, frost -> white |
| Death Worm Chitin | 52,000 | **Random** of the seven sea serpent (Tide Guardian) sets |
| Sea Serpent Scale | 225,000 | **Random** of the twelve dragon scale sets |
| Dragon Scale | 750,000 | Dragon steel of that dragon's element (fire / ice / lightning) |
| Dragon Steel | Top of the ladder | Keeps earning EP from 750,000 onward |

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
| Copper | 3,200 | Silver |
| Silver sword | 18,000 | Dragon Bone |
| Silver axe / pickaxe / shovel / hoe | 52,000 | Dragon Bone |
| Dragon Bone sword | 225,000 | **Random** blooded (elemental) dragon bone sword |
| Dragon Bone axe / pickaxe / shovel / hoe | 750,000 | **Random** dragon steel of one of the three elements |
| Blooded Dragon Bone sword | 750,000 | Dragon steel sword of the matching element |
| Dragon Steel | Top of the ladder | Keeps earning EP from 750,000 onward |

Swords join the ladder a rung earlier than the other tools because they carry
the extra blooded tier in between; both ladders still finish together at
750,000.

## Bow chain

`Bow -> Short Bow -> Dragon Bone Bow -> Long Bow -> War Bow`

| Step | Evolves at | Outcome |
|---|---|---|
| Bow | 700 | Short Bow |
| Short Bow | 3,600 | Dragon Bone Bow |
| Dragon Bone Bow | 7,200 | Tensura's Long Bow |
| Long Bow | 12,500 | War Bow |
| War Bow | Top of the ladder | Keeps earning EP, capped at 16,000 |

The dragon bone bow is slotted between Tensura's short and long bow by damage
and draw speed, so the vanilla bow line flows through Ice and Fire and straight
back into Tensura's own bows.

## EP rates

How fast a rung earns EP follows its gate: a steady trickle on the vanilla
rungs and the whole bow ladder (0.003), then 0.01 / 0.02 / 0.025 / 0.03 across
the 18,000 / 52,000 / 225,000 / 750,000 rungs. Ladder tops that keep earning do
so at 0.04 for dragon steel, 0.025 for the Blade of the Dreaded Queen, the
Dread Queen's Staff and the red death worm gauntlet, and 0.003 for gear with no
ladder at all.

## Other gear

- **Dread line:** Dread Thrall's Sword -> Dread Knight's Sword -> Blade of the
  Dreaded Queen, at 18,000 and 52,000 EP.
- **Death worm gauntlets:** yellow -> white -> red, at 18,000 and 52,000 EP.
- **Troll weapons** (axes, hammers, trunk and column clubs) stay usable
  endgame and keep earning EP.
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

## Installing it

Drop the jar into the `mods` folder - **on the server and on every client**.
Growth stage decides how large a dragon renders, so a client without the mod
would show the wrong size.

The gear, evolution and mob data all ship inside the jar; there is nothing to
extract and no separate datapack to install.

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

| | Mod | Why |
|---|---|---|
| Required | Minecraft 1.21.1, NeoForge | - |
| Required | Tensura: Reincarnated | Provides the EP, evolution and existence systems this mod writes into |
| Required | ManasCore | Tensura's library - the skill and attribute APIs |
| Required | Ice and Fire: Community Edition | The dragons this mod stretches, and the gear it rebalances |
| Required | Tensura: Ice and Fire (compat addon) | Registers the Ice and Fire gear and entity data this mod rebalances |
| Optional | Tensura: Gear Evolution | Reads the chain files; without it the gear chains do not run |
| Optional | Mysticism | Provides the light manipulation skill fire dragons are granted |
| Optional | BetterCombat | Only needed for the weapon movesets |

## Credits

- Built on [Tensura: Reincarnated](https://www.curseforge.com/minecraft/mc-mods/tensura)
  and [Tensura: Gear Evolution](https://www.curseforge.com/minecraft/mc-mods/tensura-gear-evolution).
- All Ice and Fire items belong to Ice and Fire: Community Edition.
- Movesets follow BetterCombat.

## License

GNU Lesser General Public License v2.1 - see [`LICENSE`](LICENSE).
Copyright (c) 2026 Bitwise Madness.
