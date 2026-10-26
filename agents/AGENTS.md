# Agent rules and conventions - Tensura - Ice and Fire Growth

**Repo:** `tensura-iaf-growth` · **Mod ID:** `tensura_iaf_growth` · **Branch:** `main`
**Platform:** Minecraft 1.21.1 / NeoForge 21.1.259 / JDK 21 · **Jar:** `build/libs/tensura_iaf_growth-<version>.jar`

A NeoForge addon for the Tensura RPG pack. It stretches Ice and Fire's dragon
growth (wider age cap, stage-scaled EP), tags dragon melee with the natural
element, grants each dragon a secondary element, and ships a rebalanced
datapack - gear chains, entity existence bands, BetterCombat movesets - inside
its own jar.

**Dependencies**
- Required: Tensura: Reincarnated (`tensura`), ManasCore (`manascore`), Ice and
  Fire CE (`iceandfire`), Tensura: Ice and Fire (`tensura_iaf`).
- Optional: Tensura: Gear Evolution (`gearevolution`) - the gear chains do not
  run without it; Mysticism (`mysticism`) - source of the light manipulation
  skill fire dragons are granted; BetterCombat - only the `weapon_attributes`
  files need it.
- Install on client **and** server: growth stage drives the render size, so a
  client without the mod shows the wrong dragon.

**Test:** `./gradlew build` (compiles everything, including mixins) and
`./gradlew runData` (boots the full mod stack and regenerates the datapack).
CI additionally asserts that `src/generated/resources` is unchanged by a fresh
`runData`. The growth timing, branching rolls and elemental melee are only
provable in-game - say so rather than overclaiming.

**Single source of truth:** `src/main/java/org/bitwisemadness/tensuriafgrowth/datagen/`
produces every datapack file. **Value tuning happens in those providers, not by
hand-editing JSON** - run `./gradlew runData` afterwards and commit
`src/generated/resources`. The EP gate table, branch tables, engraving table and
moveset table all live in `RebalanceContent`; the magicule/aura bands live in
`EntityExistenceContent`. There is no script form of this: a PowerShell
generator used to exist and has been deleted deliberately.

---

## Gotchas (do not regress them)

- **Six paths, three consumers.**
  `data/gearevolution/gear_existence/iceandfire/` is read by Gear Evolution
  *and* by Tensura's `gear_existence` datapack registry; the same registry also
  reads `data/iceandfire/gear_existence/` (namespace `iceandfire`), which is
  where the `tensura_iaf` addon ships its own files and therefore where this
  mod must write shadow copies or the addon's thresholds would win, and
  `data/tensura/gear_existence/` (namespace `tensura`), where our six gap-fix
  files live;
  `data/gearevolution/addon_chains/iceandfire/` is read only by Gear Evolution;
  and exactly one file sits at `data/gearevolution/gear_existence/tensura/` -
  our `short_bow.json`, a same-path override of Gear Evolution's own rung (see
  the bow carve-outs). A `gear_existence` file carries exactly one `evolution`;
  branching exists **only** in `addon_chains` (`evolutionOptions`).
- **The same item legitimately appears under both gear_existence paths**, so
  Tensura sees two entries for it. Keep their values identical: engravings are
  applied with `stack.enchant` (idempotent) but differing `maxEP`/`epGain`
  would race. The datagen's path-prefix and set assertions exist to catch this.
- **`type` and `item` must carry a real namespace.** Outside Tensura's folder
  that means `iceandfire:`, with one carve-out: the short_bow override's `type`
  is `tensura:short_bow`, because that file replaces Gear Evolution's file for
  a Tensura item. Files under `data/tensura/gear_existence/` must carry
  `tensura:` ids instead. An unqualified id parses as `minecraft:<name>` and
  matches no item, silently. Validated.
- **Addon chains win.** When an item has an addon chain, Gear Evolution uses it
  in preference to both the built-in chain and the `gear_existence` override for
  EP and the evolution target. So a branch point ships a full addon chain and,
  only when it needs an engraving, a **companion `gear_existence` file with
  `maxEP: 0`** - that zeroes out Tensura's EP/evolution writes (Tensura only
  acts when `maxEP > 0`) while still applying `engravings`, which Tensura reads
  outside that guard. Never give a branch point a non-zero `maxEP` in
  `gear_existence`: Tensura would pin a single evolution target and fight the
  random branch.
- **EP gates follow one ladder: vanilla first, then Tensura's metals spine.**
  The armor ladder runs 550 / 3,200 / 7,800 (vanilla-paced opening) and then
  18,000 / 52,000 / 225,000 / 750,000 (Tensura's low / high / pure magisteel,
  adamantite). Tools climb 3,200 / 52,000 / 750,000; swords climb 3,200 /
  18,000 / 225,000 / 750,000 because the blooded rung gives them an extra
  step. All gates are constants in `RebalanceContent` and `epGainFor(gate)`
  derives every climb rate from them: 0.003 below 18,000, then 0.01 / 0.02 /
  0.025 / 0.03 across the spine. Validation asserts strict ascent on every
  ladder and that all three dragonsteel ladders land on 750,000.
- **A terminal omits `maxEP`; inert means `maxEP: 0`.** Top-of-ladder gear
  (dragon steel, the Blade of the Dreaded Queen and Dread Queen's Staff, the
  red gauntlet, uniques, troll weapons) ships `minEP` + `epGain` and no
  `maxEP`/`evolution` - `minEP` is where it starts earning from, and the
  codec's 2,000,000 default is the ceiling. Terminal rates: 0.04 (ladder top
  at 750,000), 0.025 (singular ladders handing off at 52,000), 0.003 (no
  ladder at all). An explicit `maxEP: 0` is the opposite state: inert,
  chain-driven, never a target. Both are validated.
- **Gear Evolution ignores `gear_existence` overrides for items it has no
  built-in chain for.** It ships 141 built-in Ice and Fire chains; every one of
  them must be covered by a file of ours or its own thresholds silently win.
  Seventeen are neutralised with an inert file (`maxEP: 0`) at Gear Evolution's
  own resource path - death worm armor in all three chitin colours (12) and the
  five dragonbone tools - because the addon_chains ladder drives them instead.
  The built-in coverage check fails the build if any of the 141 loses coverage.
  Validation pins the shipped counts: 171 `data/gearevolution` gear files (154
  ours + 17 neutralised), 151 addon shadows, 6 Tensura gap-fix files, 61
  chains, 37 movesets, 24 entity files - 450 in total.
- **The bow ladder is the one cross-namespace chain, and it has two
  carve-outs.** The chain is `bow (700) -> short_bow (3,600) -> dragonbone_bow
  (7,200) -> long_bow (12,500) -> war_bow (16,000)`, slotted by damage and
  draw speed; only the two middle rungs are ours. Carve-out one: our
  `short_bow` file lives at Gear Evolution's `tensura/` path with
  `type: tensura:short_bow` (the namespace check and the `GE_GEAR` prefix
  check both exempt exactly that path). Carve-out two:
  `iceandfire:dragonbone_bow` evolves into `tensura:long_bow`, grandfathered
  in `checkTarget` from both paths its file ships under. `short_bow` is in
  `NOT_IN_ADDON` because the addon ships no file for it. Nothing else in the
  `iceandfire` ladder crosses namespaces.
- **The six files under `data/tensura/gear_existence/` are the only writes
  into Tensura's namespace.** Three are same-path overrides of Tensura's inert
  `silver_hoe`/`silver_pickaxe`/`silver_shovel` (Gear Evolution has no chain for
  them either, so they were dead): each clones Gear Evolution's silver rung for
  every other Tensura tool - 3,800 @ 0.003 into `tensura:mithril_<tool>`,
  Holy Coat I kept. Two are terminals nothing else tracks (`goblin_club`,
  `severer_blade`: no `maxEP`, 0 / 0.003). One is `tensura:kunai` ->
  `tensura:pure_magisteel_kunai` at 52,000 @ 0.02: Tensura's own
  high_magisteel gear evolves into pure magisteel at 52,000 and
  `pure_magisteel_kunai` starts earning there, so the hand-off is exact.
  Targets inside that folder may be Tensura items this pack does not generate -
  `checkTarget` grandfathers them by path - so every evolving pair there is
  spot-checked at literal values. `dragon_flute` joins the terminal uniques
  (0 / 0.003) and therefore `NOT_IN_ADDON`.
- **`branchingDefaultStrategy = "weighted_random"` is a config file, not data.**
  The mod cannot set it. At the default `"first"` the chains still work but
  every branch resolves to the first listed option. The README documents this;
  keep it accurate.
- Engraving ids are real Tensura enchantments (`tensura:holy_coat`,
  `tensura:elemental_boost`, `tensura:elemental_resistance`,
  `tensura:breathing_support`, `tensura:tsukumogami`). Verify any new id against
  Tensura's `data/tensura/enchantment/` before shipping - an unknown id fails
  Tensura's datapack registry load for the whole pack.
- Skill ids granted in `entity_existence` are checked against
  `data/tensura/tags/manascore_skill/skills/elemental_manipulation.json` in the
  Tensura and Mysticism jars (`tensura:water_manipulation`,
  `tensura:wind_manipulation`, `mysticism:light_manipulation`). Tensura
  registers skills in code, not data, so there is no datapack file to glob -
  read the tags.
- `entity_existence` aura semantics: `min_aura`/`max_aura` default to 50 when
  omitted, which is what the flat-50 rows want. `spiritualHealth` and ability
  lists are verbatim from the addon apart from the dragons' secondary element.
  Rows absent from the rebalance table stay verbatim (dread_lich among them -
  its 140000-155000 band is deliberate, it is not a typo).
- File names are asserted to match `^[a-z0-9_]+\.json$`: the previous
  hand-written pack shipped `dragonscale_gray_helmet .json` (trailing space),
  which silently broke that one entry.
- `evolution` targets are asserted to be items this pack generates, but datagen
  cannot see the game's item registry. Item ids come from Ice and Fire CE
  2.0-beta.15 (`IafItems`, `TrollType`, `SeaSerpentType.initArmors`,
  `IafDragonColors`); re-check them against the jar when the mod updates.
- **Mixins are the mechanism, not a shortcut.** `IafCommonConfigDragonMixin`
  widens the age cap range, `DragonBaseEntityGrowthMixin` stretches the growth
  hooks, `EnergyHelperMixin` and `AttributeHandlerMixin` force mastery. All are
  `remap = false` with `defaultRequire 1`; `@ModifyConstant` applies to every
  match in the method it is on, so check the target method's literals before
  adding one. At the default age cap of 128 every growth hook is an identity
  transform - that is the regression test: defaults must be unchanged.
- Datagen does not load `EnergyHelper`, so `EnergyHelperMixin` is not proven by
  `runData`. Runtime mixin application has been verified once with
  `mixin.debug.export=true` (`run/.mixin.out/class/`); re-run that rather than
  assuming.
- `src/generated/resources` is **committed** (it is the shipped datapack);
  `src/generated/resources/.cache/` is gitignored. Never hand-edit it - the CI
  drift check will reject the change.

---

## Branch sync (do this first)

Before starting any new development in this repo:

1. `git status` - confirm what uncommitted work exists. Never disturb or sweep it.
2. `git pull --ff-only` - sync with the remote. A fast-forward is the normal case,
   because history on `main` is never rewritten.
3. If the pull would **not** fast-forward (the branch has diverged), or if it would
   collide with local uncommitted work, **stop and ask** rather than merging, rebase
   or stashing on your own.

**Remote:** `github.com/HexaHexes/tensura-iaf-growth` (public, default branch
`main`). Direct pushes to `main` are blocked by branch rulesets - land changes
through a PR. Push anything only after checks are green and the user has given
the go.

## Scope doctrine

- **One mod + bundled datapack.** No KubeJS scripts, no second generator.
- **Data-driven over code** - prefer datapack JSON (via the datagen providers)
  to Java when both work. The growth/stage/element behaviour is code because no
  datapack reaches it.
- **I&F-native chains only.** Evolution targets stay Ice and Fire items, with
  two sanctioned exceptions: `iceandfire:dragonbone_bow` hands off to
  `tensura:long_bow` so the vanilla bow line can slot through Ice and Fire and
  back into Tensura (see the bow carve-outs), and the six gap-fix files under
  `data/tensura/gear_existence/` climb Tensura's own ladder (silver tools to
  mithril, kunai to pure magisteel). Do not add further cross-namespace
  targets or files without an explicit decision.
- **Mixins are sanctioned here** (widening I&F's config range and stretching its
  growth hooks have no public API). They are not a general licence: the org rule
  "public API only, no mixins" still applies to any new addon repo.

## Verification

- Run `./gradlew build` and `./gradlew runData` before pushing; `runData` must
  print `BUILD SUCCESSFUL` with its provider counts and leave
  `git status --porcelain -- src/generated/resources` empty.
- Branching, engravings, growth timing and elemental melee cannot be proven
  headless. The honest scope is "build green + datagen drift clean + the user's
  in-game check" - say so rather than overclaiming.
- Never verify against a live CurseForge instance.

## Build environment

- JDK 21 (`JAVA_HOME` -> JDK 21); the toolchain targets Java 21.
- The Gradle wrapper is committed; Gradle is **not** on PATH - use
  `.\gradlew.bat` from the repo root. Local runs use `--no-daemon` and take
  roughly 12 s (`build`) to 30 s (`runData`).
- **Dependencies resolve through Maven, never local folders.** Compile-only and
  runtime mods use pinned CurseMaven coordinates
  (`curse.maven:<slug>-<projectId>:<fileId>`, repository
  `https://cursemaven.com`); runtime mod closures are staged into `run/mods` by
  the `prepareRunMods` task. Gitignored `libs/` staging is retired: CI must
  resolve every dependency from a clean checkout. Pin the exact version the repo
  was verified against; bump pins deliberately, never implicitly.
- On `run/` file-lock errors, kill any stale `java` process and retry.

## PowerShell quirks (this org is developed on Windows)

- A `git push` "error" in PowerShell is usually stderr passthrough - success is the
  `hash..hash  branch -> branch` line, not the absence of red text.
- `New-Object Type(args)` breaks on arithmetic - use `::new()` or a parenthesized
  argument.
- **Never write files with `Set-Content -Encoding UTF8`** - PowerShell 5.1 writes a
  UTF-8 BOM that breaks Gradle's parser and YAML. Use
  `[System.IO.File]::WriteAllText($path, $text)` (no BOM). Re-check for BOMs after
  any script-written edit.
- There are no repo-owned `.ps1` scripts any more. One-off inspection commands are
  fine; anything that *generates shipped content* belongs in the datagen providers.

## Git & release

- Push only after checks are green **and** the user has given the go. `main`
  accepts changes only through a squash PR (branch rulesets enforce it).
- Never sweep the user's uncommitted work into a commit - commit only what is intended.
- Commit subjects: concise, imperative. No emojis, ever.
- **Version bump on finish:** always bump `mod_version` in `gradle.properties`
  when a new feature or bugfix is finished.
- **One feature or bugfix per branch:** develop each on its own feature branch and
  merge it into main through a pull request with a **squash merge**. The PR body
  must detail the work done, following `.github/pull_request_template.md`.

## House style

- **No icons** unless the user explicitly asks. Icons are being removed across all repos.
- **No emojis, ever.** Prohibited in all code, docs, configs, and commit messages.
- READMEs are **player-facing only**: pitch, mechanics in plain terms, in-game usage,
  required/optional dependencies, credits, license. No build internals, no class names.
- All repos are **LGPL v2.1**, copyright Bitwise Madness. `NOTICE` / attribution files
  are mandatory and must never be deleted.
- Deploy to the Tensura RPG instance **only** when the user asks.

## Naming

- **Repo and folder:** lowercase, hyphenated (`tensura-iaf-growth`).
- **`mod_id`:** lowercase with underscores (`tensura_iaf_growth`), matching the
  `@Mod` constant, `settings.gradle` and `archivesName`.
- **Java base package:** `org.bitwisemadness.tensuriafgrowth` - repo name
  lowercased, separators stripped. The Maven `group` in `build.gradle` matches.
- Keep repo name, folder name, `mod_id`, and the Java package aligned; the rename
  checklist in the template's `README.md` walks the whole set.
- **The template (`neoforge-mod-template`) is canonical for org-wide rules.** When a
  rule is added or changed in any repo's `agents/AGENTS.md`, mirror the change into
  the template so new repos inherit it.

## CI & release model

- `ci.yml` runs two jobs: **build** (JDK 21, `./gradlew build runData`, then the
  datagen drift assertion over `src/generated/resources` and a check that the jar
  actually contains the datapack, plus a jar upload) and **version-bump** (a beta or
  stable bump must change `gradle.properties` and nothing else; an `-alpha` bump may
  bundle work; a PR labelled `breaking` forces the next clean bump to raise the major
  or minor version). This repo has no JUnit or GameTest suite, so it does not use the
  template's `junit`/`gametest` jobs - the drift check is this repo's non-vacuous test.
- **Branch protection:** two active repository rulesets, identical in shape to
  every other HexaHexes repo: `protect-main` (squash-only PRs,
  `required_status_checks` with strict up-to-date policy on `build` and
  `version-bump`, no fast-forward pushes, no branch deletion) and
  `require-review-from-write-access` (one approving review; the three org
  maintainers bypass). If a job in `ci.yml` is ever renamed, update the
  required contexts to match or every PR blocks.
- `version-bump.yml` opens an isolated version-bump PR (only `gradle.properties`
  changes).
- `release.yml` runs on pushes to the default branch: when `gradle.properties`
  carries a version that has no `vX.Y.Z` tag yet, it builds the jar and publishes
  it as a GitHub release for that tag. Alpha/nightly publishing is opt-in via the
  `ALPHA_RELEASES` repo variable and is **off** by default.
- CurseForge publishing (`curseforge.yml`) is **deferred** until the mod has a
  CurseForge listing and the `CURSEFORGE_PROJECT_ID` / `CURSEFORGE_API_KEY`
  secrets. Do not add it speculatively.
