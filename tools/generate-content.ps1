<#
.SYNOPSIS
    Generates every JSON file in the Tensura - Ice and Fire datapack from the
    chain tables below, and validates the result.

.DESCRIPTION
    This script is the single source of truth for the pack. Value tuning happens
    here, not by hand-editing JSON - regenerate after any change.

    Run without arguments to write the files, or with -Check to verify that the
    committed files are byte-identical to what the tables produce (CI uses -Check).

    Two datapack directories are produced:

      data/gearevolution/gear_existence/iceandfire/   EP + one evolution target
                                                      (+ Tensura engravings)
      data/gearevolution/addon_chains/iceandfire/     EP + evolution / branch
                                                      options (random rolls)
      data/iceandfire/weapon_attributes/              BetterCombat movesets

    Gear Evolution only honours gear_existence overrides for items it already has
    a built-in chain for, and it only ever reads a single evolution target from a
    file - branching lives exclusively in addon_chains, which also works for items
    Gear Evolution does not know (troll leather, tide guardian). An item that is
    driven by addon_chains gets a gear_existence file only when it needs a Tensura
    engraving, and that file carries maxEP 0 so Tensura never fights the addon
    over EP or the evolution target.
#>
[CmdletBinding()]
param(
    [switch]$Check
)

$ErrorActionPreference = 'Stop'
[System.Threading.Thread]::CurrentThread.CurrentCulture = [System.Globalization.CultureInfo]::InvariantCulture

$RepoRoot = Split-Path -Parent $PSScriptRoot
$Namespace = 'iceandfire'

# ---------------------------------------------------------------------------
# Chain tables
# ---------------------------------------------------------------------------

$Slots = @('helmet', 'chestplate', 'leggings', 'boots')
$Tools = @('sword', 'axe', 'pickaxe', 'shovel', 'hoe')

# Dragon scale armor (12 colors) grouped by the dragon element that forges it.
$ScaleColorsByElement = [ordered]@{
    fire      = @('red', 'green', 'bronze', 'gray')
    ice       = @('blue', 'white', 'sapphire', 'silver')
    lightning = @('electric', 'amethyst', 'copper', 'black')
}

# The seven sea serpent (tide guardian) scale sets.
$TideColors = @('blue', 'bronze', 'deepblue', 'green', 'purple', 'red', 'teal')

# Troll leather -> death worm chitin pairing (mountain / forest / frost).
$TrollToChitin = [ordered]@{
    mountain = 'red'
    forest   = 'yellow'
    frost    = 'white'
}

# EP gates. These are Tensura's own ladder values, so Ice and Fire gear climbs at
# the same pace as Tensura gear (6000 / 10000 / 18000 / 45000 / 52000 / 80000 /
# 225000 / 750000).
$Gate = [ordered]@{
    SheepToCopper   = 6000    # sheep disguise -> copper
    CopperToSilver  = 10000   # copper -> silver (armor)
    SilverToTroll   = 18000   # silver -> troll leather (branch)
    TrollToChitin   = 45000   # troll -> death worm chitin
    ChitinToTide    = 52000   # chitin -> sea serpent scale (branch)
    TideToScale     = 80000   # sea serpent -> dragon scale (branch)
    ScaleToSteel    = 225000  # dragon scale -> dragon steel
    Terminal        = 750000  # top of the ladder, keeps earning

    CopperToSilverTool = 6000     # copper -> silver (tools/weapons)
    SilverToDragonbone = 18000    # silver -> dragon bone
    DragonboneBranch   = 52000    # dragon bone -> element / dragon steel
    DreadKnight        = 18000    # dread thrall -> dread knight
    DreadQueen         = 52000    # dread knight -> dread queen
    GauntletWhite      = 18000    # death worm gauntlet -> white
    GauntletRed        = 52000    # white -> red
}

$EpGainEvolving = 0.02
$EpGainTerminal = 0.003

# Engravings granted by Tensura when the item is created, mirroring what Tensura
# puts on its own gear at the same rung (silver = holy coat I, mithril tier =
# holy coat II, hihiirokane tier = tsukumogami).
$EngravingHolyCoat1 = [ordered]@{ 'tensura:holy_coat' = 1 }
$EngravingHolyCoat2 = [ordered]@{ 'tensura:holy_coat' = 2 }
$EngravingElementalBoost = [ordered]@{ 'tensura:elemental_boost' = 1 }
$EngravingElementalResist = [ordered]@{ 'tensura:elemental_resistance' = 1 }
$EngravingBreathing = [ordered]@{ 'tensura:breathing_support' = 1 }
$EngravingTsukumogami = [ordered]@{ 'tensura:tsukumogami' = 1 }

# Unique Ice and Fire weapons and accessories with no upgrade line: they stay
# usable endgame and keep earning EP.
$TerminalUniques = @(
    'amphithere_macuahuitl',
    'blindfold',
    'cockatrice_scepter',
    'cyclops_eye',
    'earplugs',
    'fishing_spear',
    'ghost_sword',
    'gorgon_head',
    'hippocampus_slapper',
    'hippogryph_sword',
    'lich_staff',
    'pixie_wand',
    'siren_flute',
    'stymphalian_bird_dagger',
    'stymphalian_feather_bundle',
    'tide_trident'
)

# BetterCombat movesets: I&F weapons inherit the moveset of a Tensura-style
# weapon class. Only parents Tensura itself ships are used.
$MovesetParents = [ordered]@{
    'bettercombat:sword'     = @(
        'copper_sword', 'silver_sword', 'dragonbone_sword',
        'dragonbone_sword_fire', 'dragonbone_sword_ice', 'dragonbone_sword_lightning',
        'dragonsteel_fire_sword', 'dragonsteel_ice_sword', 'dragonsteel_lightning_sword',
        'hippogryph_sword', 'amphithere_macuahuitl'
    )
    'bettercombat:claymore'  = @('dread_sword', 'dread_knight_sword', 'dread_queen_sword')
    'bettercombat:rapier'    = @('ghost_sword')
    'bettercombat:heavy_axe' = @(
        'copper_axe', 'silver_axe', 'dragonbone_axe',
        'dragonsteel_fire_axe', 'dragonsteel_ice_axe', 'dragonsteel_lightning_axe'
    )
    'bettercombat:pickaxe'   = @(
        'copper_pickaxe', 'silver_pickaxe', 'dragonbone_pickaxe',
        'dragonsteel_fire_pickaxe', 'dragonsteel_ice_pickaxe', 'dragonsteel_lightning_pickaxe'
    )
    'bettercombat:fist'      = @(
        'deathworm_gauntlet_yellow', 'deathworm_gauntlet_white', 'deathworm_gauntlet_red',
        'hippocampus_slapper'
    )
    'bettercombat:spear'     = @('fishing_spear')
    'bettercombat:trident'   = @('tide_trident')
    'bettercombat:staff'     = @('lich_staff', 'dread_queen_staff', 'cockatrice_scepter')
    'bettercombat:dagger'    = @('stymphalian_bird_dagger')
}

# ---------------------------------------------------------------------------
# Emitters
# ---------------------------------------------------------------------------

$GearExistence = [ordered]@{}   # relative path -> json text
$AddonChains = [ordered]@{}     # relative path -> json text
$WeaponAttributes = [ordered]@{} # relative path -> json text

function Format-JsonNumber([double]$Value) {
    return $Value.ToString('G', [System.Globalization.CultureInfo]::InvariantCulture)
}

function Join-JsonProperties([string[]]$Properties) {
    return "{`n" + (($Properties | ForEach-Object { '  ' + $_ }) -join ",`n") + "`n}`n"
}

# gear_existence file. $MaxEP / $EpGain / $Evolution / $Engravings may be $null.
function Add-GearExistence {
    param(
        [string]$Item,
        [int]$MinEP,
        [object]$MaxEP,
        [object]$EpGain,
        [object]$Evolution,
        [object]$Engravings
    )
    $props = @()
    $props += '"type": "{0}"' -f $Item
    $props += '"minEP": {0}' -f $MinEP
    if ($null -ne $MaxEP) { $props += '"maxEP": {0}' -f $MaxEP }
    if ($null -ne $EpGain) { $props += '"epGain": {0}' -f (Format-JsonNumber $EpGain) }
    if ($Engravings) {
        $pairs = @()
        foreach ($key in $Engravings.Keys) {
            $pairs += '    "{0}": {1}' -f $key, $Engravings[$key]
        }
        $props += '"engravings": {' + "`n" + ($pairs -join ",`n") + "`n" + '  }'
    }
    if ($Evolution) { $props += '"evolution": "{0}"' -f $Evolution }

    $path = 'data/{0}/gear_existence/{1}/{2}.json' -f 'gearevolution', $Namespace, ($Item -replace '^.*:', '')
    if ($GearExistence.Contains($path)) { throw "Duplicate gear_existence entry: $path" }
    $GearExistence[$path] = Join-JsonProperties $props
}

# addon_chains file (branching lives here; works for items Gear Evolution does
# not know as well as for built-in ones).
function Add-AddonChain {
    param(
        [string]$Item,
        [int]$MinEP,
        [int]$MaxEP,
        [double]$EpGain,
        [string]$Evolution,
        [string[]]$Options
    )
    if (-not $Evolution -and -not $Options) { throw "addon chain $Item needs an evolution or options" }
    if ($Evolution -and $Options) { throw "addon chain $Item must not set both" }

    $props = @()
    $props += '"item": "{0}"' -f $Item
    $props += '"minEP": {0}' -f $MinEP
    $props += '"maxEP": {0}' -f $MaxEP
    $props += '"epGain": {0}' -f (Format-JsonNumber $EpGain)
    if ($Evolution) {
        $props += '"evolution": "{0}"' -f $Evolution
    } else {
        $items = @($Options | ForEach-Object { '    "{0}"' -f $_ })
        $props += '"evolutionOptions": [' + "`n" + ($items -join ",`n") + "`n" + '  ]'
    }

    $path = 'data/gearevolution/addon_chains/{0}/{1}.json' -f $Namespace, ($Item -replace '^.*:', '')
    if ($AddonChains.Contains($path)) { throw "Duplicate addon chain entry: $path" }
    $AddonChains[$path] = Join-JsonProperties $props
}

function Add-Moveset {
    param([string]$Item, [string]$Parent)
    $path = 'data/iceandfire/weapon_attributes/{0}.json' -f $Item
    if ($WeaponAttributes.Contains($path)) { throw "Duplicate moveset entry: $path" }
    $WeaponAttributes[$path] = Join-JsonProperties @(('"{0}": "{1}"' -f 'parent', $Parent))
}

function Id([string]$Path) { return '{0}:{1}' -f $Namespace, $Path }

# ---------------------------------------------------------------------------
# Chain 1 - armor: sheep disguise -> copper -> silver -> troll leather ->
# death worm chitin -> sea serpent scale -> dragon scale -> dragon steel
# ---------------------------------------------------------------------------

foreach ($slot in $Slots) {
    # Sheep disguise -> copper
    Add-GearExistence -Item (Id "sheep_$slot") -MinEP 0 -MaxEP $Gate.SheepToCopper -EpGain $EpGainEvolving `
        -Evolution (Id "armor_copper_metal_$slot")

    # Copper -> silver
    Add-GearExistence -Item (Id "armor_copper_metal_$slot") -MinEP 0 -MaxEP $Gate.CopperToSilver -EpGain $EpGainEvolving `
        -Evolution (Id "armor_silver_metal_$slot")

    # Silver -> branch into the three troll leather sets. The engraving lives in
    # gear_existence (Tensura reads it) with maxEP 0 so Tensura does not manage
    # EP or a target here; the addon chain is authoritative for both.
    Add-GearExistence -Item (Id "armor_silver_metal_$slot") -MinEP 0 -MaxEP 0 -EpGain 0 `
        -Engravings $EngravingHolyCoat1
    Add-AddonChain -Item (Id "armor_silver_metal_$slot") -MinEP 0 -MaxEP $Gate.SilverToTroll -EpGain $EpGainEvolving `
        -Options @($TrollToChitin.Keys | ForEach-Object { Id "${_}_troll_leather_$slot" })

    # Troll leather -> its paired death worm chitin (deterministic pairing).
    foreach ($troll in $TrollToChitin.Keys) {
        Add-AddonChain -Item (Id "${troll}_troll_leather_$slot") -MinEP 0 -MaxEP $Gate.TrollToChitin -EpGain $EpGainEvolving `
            -Evolution (Id "deathworm_$($TrollToChitin[$troll])_$slot")
    }

    # Death worm chitin (all three, one tier) -> any of the seven sea serpent sets.
    foreach ($chitin in @('yellow', 'white', 'red')) {
        Add-AddonChain -Item (Id "deathworm_${chitin}_$slot") -MinEP 0 -MaxEP $Gate.ChitinToTide -EpGain $EpGainEvolving `
            -Options @($TideColors | ForEach-Object { Id "tide_${_}_$slot" })
    }

    # Sea serpent scale -> any of the twelve dragon scale sets.
    foreach ($tide in $TideColors) {
        Add-GearExistence -Item (Id "tide_${tide}_$slot") -MinEP 0 -MaxEP 0 -EpGain 0 `
            -Engravings $EngravingBreathing
        Add-AddonChain -Item (Id "tide_${tide}_$slot") -MinEP 0 -MaxEP $Gate.TideToScale -EpGain $EpGainEvolving `
            -Options @(foreach ($element in $ScaleColorsByElement.Keys) {
                foreach ($color in $ScaleColorsByElement[$element]) { Id "armor_${color}_$slot" }
            })
    }

    # Dragon scale -> the dragon steel set of its element (deterministic).
    foreach ($element in $ScaleColorsByElement.Keys) {
        foreach ($color in $ScaleColorsByElement[$element]) {
            Add-GearExistence -Item (Id "armor_${color}_$slot") -MinEP 0 -MaxEP $Gate.ScaleToSteel -EpGain $EpGainEvolving `
                -Engravings $EngravingElementalResist -Evolution (Id "dragonsteel_${element}_$slot")
        }
    }

    # Dragon steel: top of the ladder, keeps earning, engraved like mithril.
    foreach ($element in $ScaleColorsByElement.Keys) {
        Add-GearExistence -Item (Id "dragonsteel_${element}_$slot") -MinEP 0 -MaxEP $Gate.Terminal `
            -EpGain $EpGainTerminal -Engravings $EngravingHolyCoat2
    }
}

# ---------------------------------------------------------------------------
# Chain 2 - tools and weapons: copper -> silver -> dragon bone ->
# blooded dragon bone (swords) -> dragon steel
# ---------------------------------------------------------------------------

foreach ($tool in $Tools) {
    # Copper -> silver
    Add-GearExistence -Item (Id "copper_$tool") -MinEP 0 -MaxEP $Gate.CopperToSilverTool -EpGain $EpGainEvolving `
        -Evolution (Id "silver_$tool")

    # Silver -> dragon bone (deterministic), with the holy coat engraving.
    Add-GearExistence -Item (Id "silver_$tool") -MinEP 0 -MaxEP $Gate.SilverToDragonbone -EpGain $EpGainEvolving `
        -Engravings $EngravingHolyCoat1 -Evolution (Id "dragonbone_$tool")

    if ($tool -eq 'sword') {
        # Dragon bone sword -> a blooded (elemental) dragon bone sword, rolled at
        # random; each blooded sword then forges into its matching dragon steel.
        Add-AddonChain -Item (Id 'dragonbone_sword') -MinEP 0 -MaxEP $Gate.DragonboneBranch -EpGain $EpGainEvolving `
            -Options @(foreach ($element in $ScaleColorsByElement.Keys) { Id "dragonbone_sword_$element" })

        foreach ($element in $ScaleColorsByElement.Keys) {
            Add-GearExistence -Item (Id "dragonbone_sword_$element") -MinEP 0 -MaxEP $Gate.ScaleToSteel `
                -EpGain $EpGainEvolving -Engravings $EngravingElementalBoost `
                -Evolution (Id "dragonsteel_${element}_sword")
        }
    } else {
        # Tools have no blooded tier: dragon bone rolls straight into one of the
        # three dragon steel elements.
        Add-AddonChain -Item (Id "dragonbone_$tool") -MinEP 0 -MaxEP $Gate.DragonboneBranch -EpGain $EpGainEvolving `
            -Options @(foreach ($element in $ScaleColorsByElement.Keys) { Id "dragonsteel_${element}_$tool" })
    }

    # Dragon steel tools and weapons: terminal, engraved like hihiirokane.
    foreach ($element in $ScaleColorsByElement.Keys) {
        Add-GearExistence -Item (Id "dragonsteel_${element}_$tool") -MinEP 0 -MaxEP $Gate.Terminal `
            -EpGain $EpGainTerminal -Engravings $EngravingTsukumogami
    }
}

# Dragon bone bow: no dragon steel bow exists, so it tops out on its own.
Add-GearExistence -Item (Id 'dragonbone_bow') -MinEP 0 -MaxEP $Gate.Terminal -EpGain $EpGainTerminal

# Death worm gauntlets: yellow -> white -> red, then terminal.
Add-GearExistence -Item (Id 'deathworm_gauntlet_yellow') -MinEP 0 -MaxEP $Gate.GauntletWhite -EpGain $EpGainEvolving `
    -Evolution (Id 'deathworm_gauntlet_white')
Add-GearExistence -Item (Id 'deathworm_gauntlet_white') -MinEP 0 -MaxEP $Gate.GauntletRed -EpGain $EpGainEvolving `
    -Evolution (Id 'deathworm_gauntlet_red')
Add-GearExistence -Item (Id 'deathworm_gauntlet_red') -MinEP 0 -MaxEP $Gate.Terminal -EpGain $EpGainTerminal

# Dread line: Dread Thrall's Sword -> Dread Knight's Sword -> Blade of the
# Dreaded Queen.
Add-GearExistence -Item (Id 'dread_sword') -MinEP 0 -MaxEP $Gate.DreadKnight -EpGain $EpGainEvolving `
    -Evolution (Id 'dread_knight_sword')
Add-GearExistence -Item (Id 'dread_knight_sword') -MinEP 0 -MaxEP $Gate.DreadQueen -EpGain $EpGainEvolving `
    -Evolution (Id 'dread_queen_sword')
Add-GearExistence -Item (Id 'dread_queen_sword') -MinEP 0 -MaxEP $Gate.Terminal -EpGain $EpGainTerminal
Add-GearExistence -Item (Id 'dread_queen_staff') -MinEP 0 -MaxEP $Gate.Terminal -EpGain $EpGainTerminal

# Uniques without an upgrade line stay terminal.
foreach ($unique in $TerminalUniques) {
    Add-GearExistence -Item (Id $unique) -MinEP 0 -MaxEP $Gate.Terminal -EpGain $EpGainTerminal
}

# Movesets
foreach ($parent in $MovesetParents.Keys) {
    foreach ($item in $MovesetParents[$parent]) {
        Add-Moveset -Item $item -Parent $parent
    }
}

# ---------------------------------------------------------------------------
# Validation
# ---------------------------------------------------------------------------

function Test-ItemName([string]$Name) {
    return $Name -match '^iceandfire:[a-z0-9_]+$'
}

$GeneratedItems = New-Object 'System.Collections.Generic.HashSet[string]'
foreach ($path in $GearExistence.Keys) {
    $name = ($path -split '/')[-1] -replace '\.json$', ''
    [void]$GeneratedItems.Add((Id $name))
}
foreach ($path in $AddonChains.Keys) {
    $name = ($path -split '/')[-1] -replace '\.json$', ''
    [void]$GeneratedItems.Add((Id $name))
}

# Every evolution target must itself be a generated item (catches typos).
$Errors = @()
$TargetPattern = '"(?:evolution)": "(.+?)"|"evolutionOptions": \[([^\]]*)\]'
foreach ($set in @($GearExistence, $AddonChains)) {
    foreach ($path in $set.Keys) {
        foreach ($match in [regex]::Matches($set[$path], $TargetPattern)) {
            $targets = @()
            if ($match.Groups[1].Value) { $targets += $match.Groups[1].Value }
            foreach ($m in [regex]::Matches($match.Groups[2].Value, '"(.+?)"')) { $targets += $m.Groups[1].Value }
            foreach ($target in $targets) {
                if (-not $GeneratedItems.Contains($target)) {
                    $Errors += "$path -> unknown evolution target '$target'"
                }
            }
        }
    }
}

# File names must be clean (the old hand-made pack shipped a file with a space
# before .json, which silently broke that entry).
foreach ($set in @($GearExistence, $AddonChains, $WeaponAttributes)) {
    foreach ($path in $set.Keys) {
        $name = ($path -split '/')[-1]
        if ($name -notmatch '^[a-z0-9_]+\.json$') {
            $Errors += "bad file name: $path"
        }
    }
}

# Every item Gear Evolution ships a built-in Ice and Fire chain for must be
# covered by a gear_existence or an addon_chains file, or its built-in chain
# (with Gear Evolution's own thresholds) would silently win.
$BuiltinItems = @()
$BuiltinItems += 'amphithere_macuahuitl'
foreach ($element in $ScaleColorsByElement.Keys) {
    foreach ($color in $ScaleColorsByElement[$element]) {
        foreach ($slot in $Slots) { $BuiltinItems += "armor_${color}_$slot" }
    }
}
foreach ($slot in $Slots) { $BuiltinItems += "armor_copper_metal_$slot" }
foreach ($slot in $Slots) { $BuiltinItems += "armor_silver_metal_$slot" }
$BuiltinItems += @('blindfold', 'cockatrice_scepter')
foreach ($tool in $Tools) { $BuiltinItems += "copper_$tool" }
$BuiltinItems += 'cyclops_eye'
foreach ($color in @('yellow', 'white', 'red')) {
    $BuiltinItems += "deathworm_gauntlet_$color"
    foreach ($slot in $Slots) { $BuiltinItems += "deathworm_${color}_$slot" }
}
foreach ($tool in $Tools) { $BuiltinItems += "dragonbone_$tool" }
$BuiltinItems += 'dragonbone_bow'
foreach ($element in $ScaleColorsByElement.Keys) {
    $BuiltinItems += "dragonbone_sword_$element"
    foreach ($tool in $Tools) { $BuiltinItems += "dragonsteel_${element}_$tool" }
    foreach ($slot in $Slots) { $BuiltinItems += "dragonsteel_${element}_$slot" }
}
$BuiltinItems += @('dread_knight_sword', 'dread_queen_staff', 'dread_queen_sword', 'dread_sword')
$BuiltinItems += @('earplugs', 'fishing_spear', 'ghost_sword', 'gorgon_head', 'hippocampus_slapper',
    'hippogryph_sword', 'lich_staff', 'pixie_wand')
foreach ($slot in $Slots) { $BuiltinItems += "sheep_$slot" }
foreach ($tool in $Tools) { $BuiltinItems += "silver_$tool" }
$BuiltinItems += @('siren_flute', 'stymphalian_bird_dagger', 'stymphalian_feather_bundle', 'tide_trident')

foreach ($item in $BuiltinItems) {
    if (-not $GeneratedItems.Contains((Id $item))) {
        $Errors += "Gear Evolution ships a built-in chain for '$item' but this pack generates no file for it"
    }
}

# Structural spot checks - the counts a future edit can silently break.
$ScaleFiles = @($GearExistence.Keys | Where-Object { $_ -match '/armor_(red|green|bronze|gray|blue|white|sapphire|silver|electric|amethyst|copper|black)_(helmet|chestplate|leggings|boots)\.json$' })
if ($ScaleFiles.Count -ne 48) { $Errors += "expected 48 dragon scale gear_existence files, found $($ScaleFiles.Count)" }
$TideAddon = @($AddonChains.Keys | Where-Object { $_ -match '/tide_' })
if ($TideAddon.Count -ne 28) { $Errors += "expected 28 tide addon chains, found $($TideAddon.Count)" }
$TrollAddon = @($AddonChains.Keys | Where-Object { $_ -match '/(mountain|forest|frost)_troll_leather_' })
if ($TrollAddon.Count -ne 12) { $Errors += "expected 12 troll addon chains, found $($TrollAddon.Count)" }

# Everything must parse as JSON.
foreach ($set in @($GearExistence, $AddonChains, $WeaponAttributes)) {
    foreach ($path in $set.Keys) {
        try {
            $null = $set[$path] | ConvertFrom-Json
        } catch {
            $Errors += "$path does not parse as JSON: $($_.Exception.Message)"
        }
    }
}

if ($Errors.Count -gt 0) {
    $Errors | ForEach-Object { Write-Error $_ -ErrorAction Continue }
    exit 1
}

# ---------------------------------------------------------------------------
# Write / check
# ---------------------------------------------------------------------------

$AllFiles = @{}
foreach ($key in $GearExistence.Keys) { $AllFiles[$key] = $GearExistence[$key] }
foreach ($key in $AddonChains.Keys) { $AllFiles[$key] = $AddonChains[$key] }
foreach ($key in $WeaponAttributes.Keys) { $AllFiles[$key] = $WeaponAttributes[$key] }

$Encoding = [System.Text.UTF8Encoding]::new($false)
$Failure = $false

if ($Check) {
    # Committed files must match, and no stray generated JSON may exist.
    foreach ($path in $AllFiles.Keys) {
        $full = Join-Path $RepoRoot $path
        if (-not (Test-Path $full)) {
            Write-Error "missing: $path"
            $Failure = $true
            continue
        }
        $existing = [System.IO.File]::ReadAllText($full)
        if ($existing -ne $AllFiles[$path]) {
            Write-Error "stale: $path (regenerate with tools/generate-content.ps1)"
            $Failure = $true
        }
    }
    foreach ($root in @('data/gearevolution', 'data/iceandfire')) {
        $fullRoot = Join-Path $RepoRoot $root
        if (-not (Test-Path $fullRoot)) { continue }
        Get-ChildItem $fullRoot -Recurse -Filter '*.json' | ForEach-Object {
            $rel = $_.FullName.Substring($RepoRoot.Length + 1) -replace '\\', '/'
            if (-not $AllFiles.Contains($rel)) {
                Write-Error "stray file (not produced by the generator): $rel"
                $Failure = $true
            }
        }
    }
} else {
    foreach ($path in $AllFiles.Keys) {
        $full = Join-Path $RepoRoot $path
        $dir = Split-Path -Parent $full
        if (-not (Test-Path $dir)) { [void](New-Item -ItemType Directory -Path $dir -Force) }
        [System.IO.File]::WriteAllText($full, $AllFiles[$path], $Encoding)
    }
}

if ($Failure) { exit 1 }

Write-Output ("gear_existence: {0}" -f $GearExistence.Count)
Write-Output ("addon_chains:   {0}" -f $AddonChains.Count)
Write-Output ("movesets:       {0}" -f $WeaponAttributes.Count)
if ($Check) {
    Write-Output 'Check passed: committed files match the generator.'
} else {
    Write-Output ('Wrote {0} files.' -f $AllFiles.Count)
}
