# Agent rules and conventions - Tensura - Ice and Fire

**Repo:** `HexaHexes/tensura-ice-and-fire` · **Pack id:** `tensura-ice-and-fire` · **Branch:** `main`
**Platform:** Minecraft 1.21.1 / NeoForge 21.1 / pack_format 48 · **Artifact:** a zip of `pack.mcmeta` + `data/`

A pure datapack (no Java, no Gradle) that gives Ice and Fire: Community Edition
equipment an evolution progression on Tensura's own EP ladder, with branching
evolutions driven by Tensura: Gear Evolution's addon chains. Everything under
`data/` is generated.

**Dependencies**
- Required: Tensura: Reincarnated (`tensura`), Tensura: Gear Evolution
  (`gearevolution`), Ice and Fire: Community Edition (`iceandfire`), Paxi.
- Optional: BetterCombat - only the `data/iceandfire/weapon_attributes/` files
  need it; without the mod they are inert and must never cause a load error.

**Test:** `pwsh tools/generate-content.ps1 -Check` - regenerates the whole pack
in memory and fails on any drift, unknown evolution target, uncovered built-in
item, bad file name or unparseable JSON. This is the only headless check; the
branching behaviour itself is only provable in-game (see Gotchas).

**Single source of truth:** `tools/generate-content.ps1` produces every file
under `data/`. **Value tuning happens in the generator, not by hand-editing
JSON** - rerun it after any change. The EP gate table (`$Gate`), the branch
tables (`$ScaleColorsByElement`, `$TideColors`, `$TrollToChitin`), the engraving
table and the moveset table (`$MovesetParents`) all live there.

**Gotchas (do not regress them)**
- **Two directories, two parsers, one file name.** `data/gearevolution/gear_existence/`
  is read by both Gear Evolution and Tensura; `data/gearevolution/addon_chains/`
  is read only by Gear Evolution. A `gear_existence` file carries exactly one
  `evolution`; branching exists **only** in `addon_chains` (`evolutionOptions`).
- **Addon chains win.** When an item has an addon chain, Gear Evolution uses it
  in preference to both the built-in chain and the `gear_existence` override for
  EP and the evolution target. So a branch point ships a full addon chain and,
  only when it needs an engraving, a **companion `gear_existence` file with
  `maxEP: 0`** - that zeroes out Tensura's EP/evolution writes (Tensura only
  acts when `maxEP > 0`) while still applying `engravings`, which Tensura reads
  outside that guard. Never give a branch point a non-zero `maxEP` in
  `gear_existence`: Tensura would pin a single evolution target and fight the
  random branch.
- **Gear Evolution ignores `gear_existence` overrides for items it has no
  built-in chain for.** Troll leather and tide guardian armor are not built-in,
  so they can only evolve through `addon_chains`. The generator's built-in
  coverage check fails the build if a built-in item loses its file (which would
  silently revert that item to Gear Evolution's own thresholds).
- **`branchingDefaultStrategy = "weighted_random"` is a config file, not data.**
  The datapack cannot set it. At the default `"first"` the chains still work but
  every branch resolves to the first listed option. The README documents this;
  keep it accurate.
- Engraving ids are real Tensura enchantments (`tensura:holy_coat`,
  `tensura:elemental_boost`, `tensura:elemental_resistance`,
  `tensura:breathing_support`, `tensura:tsukumogami`). Verify any new id against
  Tensura's `data/tensura/enchantment/` before shipping - an unknown id fails
  Tensura's datapack registry load for the whole pack.
- File names are asserted to match `^[a-z0-9_]+\.json$`: the previous
  hand-written pack shipped `dragonscale_gray_helmet .json` (trailing space),
  which silently broke that one entry.
- `evolution` targets are asserted to be items this pack generates, but the
  generator cannot see the game's item registry. Item ids come from Ice and Fire
  CE 2.0-beta.15 (`IafItems`, `TrollType`, `SeaSerpentType.initArmors`,
  `IafDragonColors`); re-check them against the jar when the mod updates.

---

## Branch sync (do this first)

Before starting any new development in this repo:

1. `git status` - confirm what uncommitted work exists. Never disturb or sweep it.
2. `git pull --ff-only` - sync with the remote. A fast-forward is the normal case,
   because we push straight to the shared branch.
3. If the pull would **not** fast-forward (the branch has diverged), or if it would
   collide with local uncommitted work, **stop and ask** rather than merging, rebasing
   or stashing on your own.

## Scope doctrine

- **One datapack.** No KubeJS scripts, no Java.
- **Data-driven over code** - prefer JSON/datapack content to code when both work.
- **I&F-native chains only.** Evolution targets stay Ice and Fire items; this pack
  deliberately does not cross into Tensura items (that is the `Tensura - Ice and
  Fire` pack's job to differ from, and it predates this repo).

## Verification

- Run `pwsh tools/generate-content.ps1 -Check` before pushing; it must print
  `Check passed`. CI runs the same command plus a JSON parse sweep.
- Branching and engravings cannot be proven headless. The honest scope is
  "generator check green + the user's in-game check" - say so rather than
  overclaiming.
- Never verify against a live CurseForge instance.

## Build environment

- Windows, PowerShell 5.1 compatible: the generator must keep running under
  plain `powershell` as well as `pwsh` (CI uses `pwsh`, local runs may use 5.1).
- No Gradle, no JDK.

## PowerShell quirks (this org is developed on Windows)

- A `git push` "error" in PowerShell is usually stderr passthrough - success is the
  `hash..hash  branch -> branch` line, not the absence of red text.
- `New-Object Type(args)` breaks on arithmetic - use `::new()` or a parenthesized
  argument.
- **Never write files with `Set-Content -Encoding UTF8`** - PowerShell 5.1 writes a
  UTF-8 BOM that breaks YAML and Minecraft's JSON readers. The generator uses
  `[System.IO.File]::WriteAllText($path, $text, [System.Text.UTF8Encoding]::new($false))`.
  Re-check for BOMs after any script-written edit.
- `[ordered]` dictionaries expose `Contains`, not `ContainsKey`, on PowerShell 5.1.

## Git & release

- Remote is SSH: `git@github.com:HexaHexes/<name>.git`.
- Push only after checks are green **and** the user has given the go.
- Never sweep the user's uncommitted work into a commit - commit only what is intended.
- Commit subjects: concise, imperative.
- **Version bump on finish:** always bump the version when a new feature or bugfix
  is finished. The version lives at the end of the `pack.mcmeta` description
  (`... - vX.Y.Z`, optionally `-alpha.N` / `-beta.N`).
- **One feature or bugfix per branch:** develop each on its own feature branch and
  merge it into main/master through a pull request with a **squash merge**. The PR
  body must detail the work done, following `.github/pull_request_template.md`.

## House style

- **No icons** unless the user explicitly asks. Icons are being removed across all repos.
- **No emojis, ever.** Prohibited in all code, docs, configs, and commit messages.
- READMEs are **player-facing only**: pitch, mechanics in plain terms, in-game usage,
  required/optional dependencies, credits, license. No build internals, no class names.
- All repos are **LGPL v2.1**, copyright Bitwise Madness. `NOTICE` / attribution files
  are mandatory and must never be deleted.
- Deploy to the Tensura RPG instance **only** when the user asks.

## Naming

- **Repo and folder:** lowercase, hyphenated (`tensura-ice-and-fire`).
- Keep the repo name, folder name and pack description aligned.
- **The template (`neoforge-mod-template`) is canonical for org-wide rules.** When a
  rule is added or changed in any repo's `agents/AGENTS.md`, mirror the change into
  the template so new repos inherit it.

## CI & release model

- `ci.yml` runs two jobs: **validate** (`tools/generate-content.ps1 -Check` plus a
  JSON parse sweep over `data/`) and **version-bump** (a beta or stable bump must
  change `pack.mcmeta` and nothing else; an `-alpha` bump may bundle work; a PR
  labelled `breaking` forces the next clean bump to raise the major or minor version).
- `version-bump.yml` opens an isolated version-bump PR (only `pack.mcmeta` changes).
- `release.yml` runs on pushes to the default branch: when `pack.mcmeta` carries a
  version that has no `vX.Y.Z` tag yet, it zips `pack.mcmeta` + `data/` and
  publishes the zip as a GitHub release for that tag.
- Alpha/nightly publishing is opt-in via the `ALPHA_RELEASES` repo variable and is
  **off** by default.
