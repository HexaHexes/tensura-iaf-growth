# Tensura - Ice and Fire Growth

A NeoForge mod for the **Tensura RPG** pack (MC 1.21.1) that does two things: it
raises Ice and Fire's dragon growth cap from 128 days up to 4096, so a hatchling
keeps growing for as long as you keep feeding it, and it gives Ice and Fire
equipment a full evolution progression along Tensura's own EP ladder, including
**branching evolutions**: one item can forge into several different sets,
rolled at random when it evolves.

Ice and Fire gear starts on the vanilla progression, at the same early pace as
leather and stone gear, but the vanilla opening forks. Either branch of the
vanilla line can cross into this mod's copper ladder and from there into
Tensura's own metals ladder, so every rung climbs where Tensura gear of that
tier climbs. Each rung is engraved the way Tensura engraves its own equipment
at that power level.

## Dragon growth

- The growth cap is raised. Ice and Fire stops a tamed dragon growing at 128
  days of age; this mod widens that ceiling up to 4096, so a hatchling keeps
  growing for as long as you keep feeding it. The default is unchanged:
  dragons behave exactly as before until the cap is raised.
- The ladder stretches with the cap. Everything that decides when a dragon
  reaches its next stage is scaled to the cap, so raising it does not skip a
  stage or compress one. The same progression simply takes longer; the
  endpoints are identical and only the timing moves.
- **Growth stage is the EP lever.** How much EP a dragon earns is set by its
  stage: 15% as a hatchling, then 30%, 50%, 75%, and the full rate once fully
  grown. Growing one up is the main way to make it strong.
- A dragon's EP is pinned to a growth curve. The total EP (aura + magicule) is
  no longer a random roll at spawn. It is pinned, by age, to a stage-banded
  curve this mod ships as its own config (see the configuration section
  below): 10,000 at hatch, then 100k / 200k / 350k / 550k / 800k at the end of
  each growth stage. The split is always 1 aura : 9 magicule, so a hatchling
  reads as a hatchling and a fully grown dragon reads as the apex predator it
  is. The pin runs on every load and every growth day, so a dragon's EP is
  always its age's curve value.
- The pin works as a floor. EP a dragon earns on top of the curve (kill
  top-ups) survives up to a ceiling, and anything above the ceiling is clipped
  back down, so a whelp can never be inflated past its stage and a max-growth
  dragon tops out at its curve's value plus what it has banked. On first load
  after 1.5.0, existing dragons are snapped onto the curve once, so dragons
  inflated by older versions come back down on their own.
- Naming survives the pin. Gains naming grants a dragon (subdue, evolve, and
  the endow mode's 9x EP raise) are carried by the dragon's max-attribute
  bases, which the pin reads as its ceiling. Endowed and evolved dragons keep
  their named EP past the stage caps, through every load.
- Dragons are excluded from TensuraRPGSystem's +1%-HP-per-level status layer,
  so a dragon's HP, attack and armor are the pure Ice and Fire growth curve.
  Every other mob keeps the layer unchanged.
- A fully grown dragon hits exactly 10,000 HP. With the recommended
  `maxHealth` in `iaf-common.json`, max-growth stage-5 dragons land exactly at
  10,000 HP on the pure growth curve, with no banked bonus needed. The cap
  stays the cap no matter how much EP they have earned.
- Dragons fight with their element. A dragon's melee hits carry its natural
  element: fire dragons burn, ice dragons chill, lightning dragons shock.
  Resistances and weaknesses apply the way they do to any other elemental
  attack.
- Dragons gain a secondary element. Fire dragons are granted light
  manipulation, ice dragons water manipulation, and lightning dragons wind
  manipulation, alongside their existing breath and resistances.

## Mob strength bands

Every Ice and Fire mob's magicule and aura band is rebalanced so it sits where
its threat level belongs: death worms and sirens at the low end, the dread
ramp climbing form by form, cyclops and sea serpents in the middle, and all
three dragons at the top of the ladder. Spiritual health and each mob's
abilities are unchanged.

## Armor chain

`Leather / Sheep Disguise -> Copper -> Silver -> Troll Leather ->
Death Worm Chitin -> Sea Serpent Scale -> Dragon Scale ->
Dragon Steel or Mithril`

The vanilla opening forks instead of stepping straight on: at 550 EP, leather
armor rolls between chainmail, Ice and Fire copper and Tensura's monster
leather (rank D). The chainmail branch continues on vanilla's own iron ->
diamond -> netherite line, and netherite then forges into Tensura's low
magisteel at 12,000 EP for armor and 10,000 for tools (so both netherite and
magisteel keep a band worth wearing), riding the metals spine from there.
Monster leather climbs Tensura's own ranks and then flows into Tensura's
creature sets (see the creature ladder below). Only the copper branch joins
the ladder below.

Copper does not commit to one silver: it rolls at random into either Ice and
Fire silver (climbing the chain above) or Tensura's silver, which forges the
same way (see the Tensura silver and mithril section below).

| Step | Evolves at | Outcome |
|---|---|---|
| Leather armor | 550 | **Random**: Chainmail, Copper, or Monster Leather (rank D) |
| Sheep Disguise | 550 | Copper |
| Copper | 3,200 | **Random**: Ice and Fire Silver or Tensura Silver |
| Silver | 7,800 | **Random** of the three troll leather sets (mountain / forest / frost) |
| Troll Leather | 18,000 | The paired death worm chitin: mountain -> red, forest -> yellow, frost -> white |
| Death Worm Chitin | 52,000 | **Random** of the seven sea serpent (Tide Guardian) sets |
| Sea Serpent Scale | 225,000 | **Random** of the twelve dragon scale sets |
| Dragon Scale | 750,000 | **Random**: dragon steel of that dragon's element or mithril armor |
| Mithril armor | 1,000,000 | Adamantite armor |
| Adamantite armor | 1,250,000 | Hihiirokane armor |
| Dragon Steel / Hihiirokane | Top of the ladder | No further evolution: both keep earning EP |

All three death worm chitin sets are the same tier, so any of them can reach any
of the seven sea serpent sets, and any sea serpent set can reach any of the
twelve dragon scale sets. Dragon scale armor then rolls between the dragon
steel set of its own element and Tensura's mithril armor, which forges on
through adamantite into hihiirokane. The fire dragons are red, green, bronze and
gray; the ice ones are blue, white, sapphire and silver; the lightning ones are
electric, amethyst, copper and black.

## Creature ladder

`Monster Leather -> Ant Carapace -> Armorsaurus -> Serpent Scalemail or
Knight Spider Carapace -> Charybdis Scalemail -> Dragon Steel or Mithril`

Tensura's hide, scale and carapace sets are woven into one flow that Monster
Leather feeds. Monster Leather C forks at 5,000 EP into Monster Leather B (the
rank ladder as before) or the Ant Carapace, and the creature line runs from
there. Monster Leather Special A, the rank ladder's top and once a dead end,
now hands off at 225,000 EP into the same creature sets, so both paths split
at Monster Leather C and merge again at Serpent Scalemail and Knight Spider
Carapace.

| Step | Evolves at | Outcome |
|---|---|---|
| Monster Leather C | 5,000 | **Random**: Monster Leather B, or Ant Carapace |
| Ant Carapace | 6,000 | **Random** of the two armorsaurus sets (plain or scalemail) |
| Armorsaurus | 9,000 | **Random**: Serpent Scalemail or Knight Spider Carapace |
| Monster Leather Special A | 225,000 | **Random**: Serpent Scalemail or Knight Spider Carapace |
| Serpent / Knight Spider | 500,000 | Charybdis Scalemail |
| Charybdis Scalemail | 750,000 | **Random**: dragon steel (any element) or mithril armor |

Serpent Scalemail and Knight Spider Carapace still earn their set-wide
attribute bumps as they climb, now at 280,000, 360,000 and 440,000 EP, late
enough that a piece arriving from Special A earns every one of them. Charybdis
Scalemail hits the same 750,000 fork as dragon scale armor, so every organic
line (troll leather, monster leather, creature carapace) ends on the metals
spine, and every metal line ends at hihiirokane.

## Tools and weapons chain

`Stone -> Copper -> Silver -> Dragon Bone -> Dragon Steel (or Mithril)`

Just like leather armor, the vanilla opening forks here: at 500 EP, every stone
tool (sword, axe, pickaxe, shovel, hoe) rolls between iron and Ice and Fire
copper. The iron branch continues on vanilla's own iron -> diamond -> netherite
line; only the copper branch joins the ladder below.

| Step | Evolves at | Outcome |
|---|---|---|
| Stone tools | 500 | **Random**: Iron or Copper |
| Copper | 3,200 | **Random**: Ice and Fire Silver or Tensura Silver |
| Silver sword / axe / pickaxe / shovel / hoe | 52,000 | Dragon Bone |
| Dragon Bone sword | 225,000 | **Random**: blooded (elemental) dragon bone sword or mithril sword |
| Dragon Bone axe / pickaxe / shovel / hoe | 225,000 | **Random**: dragon steel of one of the three elements or mithril tool |
| Blooded Dragon Bone sword | 750,000 | Dragon steel sword of the matching element |
| Dragon steel tools | Top of the ladder | Keep earning EP from 225,000 onward |
| Dragon steel sword | Top of the ladder | Keeps earning EP from 750,000 onward |

Both dragon bone forks roll 50/50 between the two branches: the three element
skins of the existing branch share one half of the roll, mithril the other, no
matter how many skins a branch carries. A sword that takes the old branch gets
its extra blooded tier at 225,000 and forges on to 750,000; the other tools
hand straight off at 225,000, so both ladders reach dragon steel together.

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
the 18,000 / 52,000 / 225,000 / 750,000 rungs. The mithril bands keep the rate
of the rung they sit on: weapons and tools earn at 0.025 across their
225,000 -> 500,000 band, armor at 0.03 across 750,000 -> 1,000,000, and the
re-banded adamantite armor at the top rate of 0.04 across 1,000,000 ->
1,250,000. The creature ladder keeps Tensura's own rates for every set it
re-gates: 0.005 on Monster Leather C and the Ant Carapace, 0.015 on the
armorsaurus, serpent and knight spider sets, and 0.025 on Special A and
Charybdis Scalemail. Ladder tops that keep earning do so at 0.04 for dragon
steel, 0.025 for the Blade of the Dreaded Queen, the Dread Queen's Staff and
the red death worm gauntlet, and 0.003 for gear with no ladder at all.

## Other gear

- **Dread line:** Dread Thrall's Sword -> Dread Knight's Sword -> Blade of the
  Dreaded Queen, at 18,000 and 52,000 EP.
- **Death worm gauntlets:** yellow -> white -> red, at 18,000 and 52,000 EP.
- **Troll weapons** (axes, hammers, trunk and column clubs) stay usable
  endgame and keep earning EP.
- **Uniques** (ghost sword, lich staff, tide trident, pixie wand, siren flute,
  cockatrice scepter, cyclops eye, gorgon head, hippocampus slapper,
  hippogryph sword, fishing spear, stymphalian dagger, amphithere macuahuitl,
  blindfold, earplugs, the dread queen's staff and the dragon flute) are
  terminal and keep earning EP, so they stay usable at endgame.

## Tensura gear gap fixes

A few Tensura items had no working progression anywhere; this mod plugs them:

- **Silver hoe, pickaxe and shovel** now evolve like every other Tensura silver
  item with an Ice and Fire counterpart: at 52,000 EP into the dragon bone tool
  of the same name, engraved with Holy Coat I.
- **Kunai** evolves into the pure magisteel kunai at 52,000 EP, exactly where
  Tensura's own pure magisteel tier begins.
- **Goblin club** and **severer blade** have no upgrade line: they stay usable
  endgame and keep earning EP.

## Tensura silver and mithril

Tensura's own silver gear rejoins the Ice and Fire chain instead of running
beside it, and mithril now enters the ladder as a dragon bone or dragon scale
fork outcome rather than a tier of its own:

| Step | Evolves at | Outcome |
|---|---|---|
| Tensura silver armor | 7,800 | **Random** of the three troll leather sets, same as Ice and Fire silver |
| Tensura silver sword / axe / hoe / pickaxe / shovel | 52,000 | Dragon Bone of the same name |
| Tensura silver great sword, katana, kodachi, long sword, odachi, scythe, short sword, sickle, spear, tachi | 225,000 | The matching mithril weapon |
| Mithril weapons and tools | 500,000 | Adamantite weapon or tool |
| Mithril armor | 1,000,000 | Adamantite armor |
| Adamantite armor | 1,250,000 | Hihiirokane armor |
| Adamantite weapons and tools | Tensura's own ladder (225,000 to 750,000) | Hihiirokane |

The ten exotic silver weapons with no Ice and Fire counterpart hold their silver
tier all the way to the 225,000 dragon bone fork gate and step straight into
mithril there; the sword, axe and the three tools step onto dragon bone at
52,000 like Ice and Fire silver does, and mithril is then one of the fork
outcomes they roll for at 225,000. Mithril weapons and tools floor at that same
225,000 and forge on to 500,000, inside the adamantite band Tensura ships
untouched; mithril armor floors at the 750,000 dragon scale fork and forges on
to 1,000,000, where this mod re-bands Tensura's adamantite armor to accept the
carry instead of blowing straight through it. Mithril gear keeps Tensura's
Holy Coat II engraving.

## Engravings

Evolving grants Tensura enchantment engravings the way Tensura does on its
own gear:

| Tier | Engraving |
|---|---|
| Silver weapons and silver armor | Holy Coat I |
| Tensura mithril gear | Holy Coat II |
| Blooded dragon bone swords | Elemental Boost I |
| Sea serpent (Tide Guardian) armor | Breathing Support I |
| Dragon scale armor | Elemental Resistance I |
| Dragon steel weapons and tools | Tsukumogami I |
| Dragon steel armor | Holy Coat II |

## Movesets

I&F weapons are given BetterCombat movesets matching their class: claymores for
the dread blades, rapiers for the ghost sword, heavy axes for the axes, spears
and tridents for the polearms, staves for the casters, fists for the gauntlets.
These files only take effect when BetterCombat is installed; without it they
are ignored.

## Configuration - the dragon EP curve

Since 1.5.0, the curve a dragon's EP is pinned to lives in this mod's own
config, `config/tensura_iaf_growth-common.toml`, section `[dragon_ep]`. The
six values are the curve's vertices, the EP at hatch and at the end of each
growth stage, and the pin interpolates a straight line between them, so the
curve is correct at any growth-cap stretch:

| Key | Default | Meaning |
|---|---|---|
| `hatchEpFloor` | 10,000 | EP at day 0 (1,000 aura + 9,000 magicule) |
| `stage1EpCap` | 100,000 | EP at the end of stage 1 |
| `stage2EpCap` | 200,000 | EP at the end of stage 2 |
| `stage3EpCap` | 350,000 | EP at the end of stage 3 |
| `stage4EpCap` | 550,000 | EP at the end of stage 4 |
| `stage5EpCap` | 800,000 | EP at full growth (stage 5) and the cap below naming |

The line is the total EP (`aura + magicule`); aura always takes a tenth and
magicule nine tenths. The values are read live, so a world reload picks them
up, and they are validated to be non-decreasing (a stage cap below the one
before it would make the line run backwards). Broken values are clamped into
order with one warning in the log.

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
| Optional | TensuraRPGSystem | Dragons are excluded from its +1% HP-per-level status layer when it is present |
| Optional | BetterCombat | Only needed for the weapon movesets |

## Credits

- Built on [Tensura: Reincarnated](https://www.curseforge.com/minecraft/mc-mods/tensura)
  and [Tensura: Gear Evolution](https://www.curseforge.com/minecraft/mc-mods/tensura-gear-evolution).
- All Ice and Fire items belong to Ice and Fire: Community Edition.
- Movesets follow BetterCombat.

## License

GNU Lesser General Public License v2.1 - see [`LICENSE`](LICENSE).
Copyright (c) 2026 Bitwise Madness.

## AI disclosure

Parts of this project were written with the assistance of AI coding tools.
AI-assisted code, data and documentation ship only after human review by the
maintainers.
