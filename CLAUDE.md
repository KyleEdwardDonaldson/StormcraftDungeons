# Stormcraft-Dungeons

**Version**: 0.1.0
**Platform**: Paper 1.21.3, Java 21
**Dependencies**: Stormcraft (required), MythicDungeons (required), Stormcraft-Essence (optional), Stormcraft-Events (optional)

---

## Project Overview

**Stormcraft-Dungeons** bridges the gap between the Stormcraft ecosystem and MythicDungeons, creating storm-themed instanced dungeons that spawn dynamically based on storm activity. Players can enter private dungeon instances during high-intensity storms, facing challenging content for massive essence rewards.

### Core Concept

When storms reach critical intensity, dimensional rifts open near the storm epicenter. These rifts lead to instanced "Storm Rift" dungeons - corrupted pocket dimensions where storm energy has run wild. Players must navigate these dangerous instances, defeat corrupted bosses, and claim legendary loot.

---

## VPS Environment Context

This repository lives on the **Stormcraft production VPS** that hosts the live Minecraft server. The server runs under **Pterodactyl** panel management.

### Build & Deployment Workflow

When building the plugin JAR, **always build directly into the Pterodactyl plugins directory**:

```bash
cd /var/repos/StormcraftDungeons
mvn clean package && cp target/stormcraft-dungeons-*.jar /var/lib/pterodactyl/volumes/31a2482a-dbb7-4d21-8126-bde346cb17db/plugins/
```

After building, restart the Minecraft server via Pterodactyl panel or console command to load the new version.

### Important Notes
- This is a **production environment** - test thoroughly before building
- The Pterodactyl volume path is the live server's plugin directory
- Configuration files persist in `plugins/StormcraftDungeons/` subdirectory
- Requires MythicDungeons to be installed and configured first

---

## Architecture

### Core Systems

#### 1. Portal Management
- **Dynamic Spawning**: Portals appear near storm epicenters when intensity thresholds are met
- **Auto-Cleanup**: Portals despawn when storms end or move too far away
- **Visual Effects**: Portal blocks with particle effects for visibility
- **Multi-Portal Support**: Handle multiple storms spawning multiple portals

#### 2. Entry Requirements System
- **SEL Gating**: Check player's Storm Exposure Level (from Stormcraft-Essence)
- **Essence Cost**: Withdraw essence via Vault economy as entry fee
- **Storm Requirements**: Verify storm intensity and proximity
- **Completion Gates**: Require previous dungeon completions for harder content
- **Permission Checks**: Optional permission-based access control

#### 3. MythicDungeons Integration
- **API Wrapper**: Interface with MythicDungeons API for dungeon control
- **Event Listening**: Monitor dungeon lifecycle events (start, complete, fail)
- **Party Management**: Work with MythicDungeons party system
- **Requirement Injection**: Add custom entry requirements to dungeons

#### 4. Reward Distribution
- **Essence Awards**: Grant essence based on dungeon completion
- **Damage Tracking**: Track boss damage for reward sharing (if integrated with Events)
- **Loot Modifiers**: Apply multipliers based on difficulty, party size, etc.
- **Completion Tracking**: Store player completion data for progression

#### 5. Storm Detection
- **Active Storm Tracking**: Monitor Stormcraft's TravelingStorm instances
- **Intensity Calculation**: Determine if storm meets dungeon spawn requirements
- **Proximity Checks**: Verify players are near qualifying storms
- **Multi-Storm Support**: Handle multiple simultaneous storms

---

## Package Structure

```
dev.ked.stormcraft.dungeons/
├── StormcraftDungeonsPlugin.java    # Main plugin class
├── config/
│   ├── ConfigManager.java           # Config loader and manager
│   └── DungeonConfig.java           # Per-dungeon configuration
├── integration/
│   ├── MythicDungeonsIntegration.java   # MythicDungeons API wrapper
│   ├── StormcraftIntegration.java       # Storm detection and tracking
│   ├── EssenceIntegration.java          # SEL and essence handling
│   └── EventsIntegration.java           # Optional StormcraftEvents link
├── portal/
│   ├── PortalManager.java           # Portal lifecycle management
│   ├── Portal.java                  # Portal data model
│   └── PortalRenderer.java          # Visual effects and blocks
├── requirement/
│   ├── RequirementChecker.java      # Entry requirement validation
│   ├── Requirement.java             # Base requirement interface
│   └── requirements/
│       ├── SELRequirement.java
│       ├── EssenceRequirement.java
│       ├── StormRequirement.java
│       └── CompletionRequirement.java
├── reward/
│   ├── RewardManager.java           # Reward calculation and distribution
│   └── RewardCalculator.java        # Essence/multiplier calculation
├── listener/
│   ├── StormEventListener.java      # Listen for storm spawn/end
│   ├── DungeonEventListener.java    # Listen for dungeon lifecycle
│   └── PortalInteractListener.java  # Handle portal interactions
├── command/
│   ├── DungeonCommand.java          # Main command handler
│   └── DungeonTabCompleter.java     # Tab completion
└── data/
    ├── PlayerData.java              # Player completion tracking
    └── DataManager.java             # Persistence layer
```

---

## Configuration Structure

### config.yml
```yaml
# Main configuration for Stormcraft-Dungeons

portals:
  # How often to check for portal spawning (in ticks)
  check_interval: 100  # 5 seconds

  # Particle effects around portals
  particles:
    enabled: true
    type: PORTAL
    count: 20
    radius: 2.0

  # Portal block structure
  structure:
    material: NETHER_PORTAL
    width: 3
    height: 3

  # Auto-cleanup settings
  remove_on_storm_end: true
  remove_when_too_far: true
  max_distance_from_storm: 300

storms:
  # Global storm requirements
  check_interval: 60  # Check every 60 seconds
  max_portals: 5      # Max portals active at once

# Dungeon-specific configurations
dungeons:
  stormcore_depths:
    enabled: true
    display_name: "&b&lStormcore Depths"

    # Entry requirements
    requirements:
      min_sel: 5
      essence_cost: 500
      min_storm_intensity: 40
      max_distance_from_storm: 200
      permission: "stormcraft.dungeons.stormcore"

    # Rewards
    rewards:
      essence_base: 1500
      essence_variance: 500  # 1000-2000 range
      completion_bonus: 0.1  # 10% bonus for first completion

    # Portal spawning
    portal:
      enabled: true
      spawn_chance: 0.8  # 80% chance to spawn portal

  tempest_labyrinth:
    enabled: true
    display_name: "&5&lTempest Labyrinth"

    requirements:
      min_sel: 15
      essence_cost: 2000
      min_storm_intensity: 60
      required_completions:
        stormcore_depths: 3  # Must complete Stormcore 3 times

    rewards:
      essence_base: 6500
      essence_variance: 1500
      completion_bonus: 0.15

    portal:
      enabled: true
      spawn_chance: 0.5

  storm_sanctum:
    enabled: true
    display_name: "&c&l⚡ Storm Titan's Sanctum ⚡"

    requirements:
      min_sel: 25
      essence_cost: 10000
      min_storm_intensity: 90
      required_completions:
        tempest_labyrinth: 5
      required_item:
        material: NETHER_STAR
        display_name: "&c&lStorm Titan Core"
        lore:
          - "&7The heart of a Storm Titan"

    rewards:
      essence_base: 35000
      essence_variance: 15000
      completion_bonus: 0.2

    portal:
      enabled: true
      spawn_chance: 0.3  # Rare spawn
```

### messages.yml
```yaml
# Player-facing messages (supports MiniMessage format)

portal:
  spawned: "&6[Dungeons] &fA &b{dungeon} &fportal has opened near the storm!"
  interact:
    success: "&a✓ Entering {dungeon}..."
    failed: "&c✗ You cannot enter this dungeon!"
    requirements: "&e⚠ Requirements:"

requirements:
  sel: "&c✗ Requires SEL {required} &7(You have: {current})"
  essence: "&c✗ Requires {required} essence &7(You have: {current})"
  storm: "&c✗ Must be near a storm (intensity {required}+)"
  completion: "&c✗ Must complete {dungeon} {required} times &7(You have: {current})"
  permission: "&c✗ You don't have permission to access this dungeon"

rewards:
  essence: "&a+ {amount} Essence"
  completion_bonus: "&e+ {amount} Essence &7(First Completion Bonus!)"
  dungeon_complete: "&a&l✓ Dungeon Complete! &a{essence} essence earned"

commands:
  list: "&6[Dungeons] &fActive Portals:"
  list_entry: "&e• {dungeon} &7at &f{x}, {y}, {z} &7({distance}m away)"
  list_empty: "&6[Dungeons] &7No active portals"
  stats: "&6[Dungeons] &fYour Statistics:"
  stats_entry: "&e• {dungeon}: &f{completions} completions"
```

---

## Dungeon Definitions

Dungeons are created and configured in MythicDungeons directly. This plugin handles the **integration layer** only.

### Required MythicDungeons Setup

For each dungeon, create the dungeon world and configure it in MythicDungeons:

```bash
# In-game commands to create dungeons
/md create stormcore_depths classic
/md create tempest_labyrinth procedural
/md create storm_sanctum classic

# Set dungeon properties
/md edit stormcore_depths
# Use in-game GUI to configure
```

### Dungeon World Requirements

1. **Stormcore Depths**
   - Type: Classic linear
   - Size: ~100x50x100 blocks
   - Duration: 5-10 minutes
   - Difficulty: Easy (solo/duo)

2. **Tempest Labyrinth**
   - Type: Procedural branching
   - Rooms: 15-25 rooms
   - Duration: 15-20 minutes
   - Difficulty: Medium (3-5 players)

3. **Storm Titan's Sanctum**
   - Type: Classic epic
   - Size: ~200x100x200 blocks
   - Duration: 30-45 minutes
   - Difficulty: Hard (5-10 players)

---

## Integration Points

### Stormcraft Integration

```java
// Get active storms
List<TravelingStorm> storms = stormcraftIntegration.getActiveStorms();

// Check storm intensity
int intensity = stormcraftIntegration.getStormIntensity(storm);

// Check if player is near storm
boolean near = stormcraftIntegration.isPlayerNearStorm(player, storm, 200);
```

### Stormcraft-Essence Integration

```java
// Check player SEL
int sel = essenceIntegration.getPlayerSEL(player);

// Withdraw essence as entry cost
boolean success = essenceIntegration.withdrawEssence(player, cost);

// Award essence on completion
essenceIntegration.awardEssence(player, amount);
```

### MythicDungeons Integration

```java
// Check if player can enter
boolean canEnter = mythicDungeonsIntegration.canEnterDungeon(player, "stormcore_depths");

// Open dungeon GUI
mythicDungeonsIntegration.openDungeonGUI(player, "stormcore_depths");

// Listen for completion
@EventHandler
public void onDungeonComplete(DungeonCompleteEvent event) {
    // Award rewards
}
```

---

## Commands

### Player Commands
- `/dungeon` or `/dg` - Show help menu
- `/dungeon list` - List active portal locations
- `/dungeon stats` - Show your dungeon completions
- `/dungeon nearest` - Find nearest active portal

### Admin Commands
- `/dungeon reload` - Reload configuration
- `/dungeon spawn <dungeon>` - Manually spawn a portal
- `/dungeon remove <dungeon>` - Remove a specific portal
- `/dungeon clear` - Remove all portals
- `/dungeon debug` - Toggle debug mode

---

## Permissions

### Player Permissions
- `stormcraft.dungeons.use` - Access dungeon system (default: true)
- `stormcraft.dungeons.stormcore` - Enter Stormcore Depths
- `stormcraft.dungeons.labyrinth` - Enter Tempest Labyrinth
- `stormcraft.dungeons.sanctum` - Enter Storm Titan's Sanctum

### Admin Permissions
- `stormcraft.dungeons.admin` - All admin commands
- `stormcraft.dungeons.reload` - Reload config
- `stormcraft.dungeons.spawn` - Spawn portals manually
- `stormcraft.dungeons.bypass` - Bypass all requirements

---

## Event Flow

### Portal Spawning
```
1. Storm reaches required intensity
   ↓
2. Check if dungeon is enabled
   ↓
3. Roll random chance for portal spawn
   ↓
4. Find safe spawn location near storm
   ↓
5. Create portal structure
   ↓
6. Spawn particle effects
   ↓
7. Announce to nearby players
```

### Player Entry
```
1. Player right-clicks portal
   ↓
2. Check all requirements (SEL, essence, storm, etc)
   ↓
3. If failed: Show requirement messages
   ↓
4. If passed: Withdraw essence cost
   ↓
5. Open MythicDungeons GUI
   ↓
6. Player enters dungeon instance
```

### Dungeon Completion
```
1. MythicDungeons fires completion event
   ↓
2. Calculate essence rewards
   ↓
3. Apply multipliers (first time, party size, etc)
   ↓
4. Award essence to all participants
   ↓
5. Track completion for progression
   ↓
6. Send completion messages
```

---

## Performance Considerations

### Portal Management
- **Async Storm Checking**: Check storms on async scheduler to avoid main thread lag
- **Cached Storm Data**: Cache storm intensity calculations (refresh every 5s)
- **Limited Portals**: Max 5 portals active to prevent particle lag
- **Cleanup Task**: Remove invalid portals every 30 seconds

### Requirement Checking
- **Cache SEL Values**: Don't query Essence plugin every check
- **Batch Checks**: Validate all requirements at once
- **Early Exit**: Return as soon as one requirement fails

### Reward Distribution
- **Async Calculation**: Calculate rewards off main thread
- **Batch Awards**: Award essence to all players in one transaction
- **Lazy Loading**: Only load player data when needed

---

## Testing Checklist

### Portal System
- [ ] Portal spawns when storm reaches intensity threshold
- [ ] Portal despawns when storm ends
- [ ] Portal despawns when storm moves too far
- [ ] Multiple portals can exist simultaneously
- [ ] Particle effects render correctly
- [ ] Portal structure generates properly

### Requirements
- [ ] SEL requirement blocks under-leveled players
- [ ] Essence cost withdraws correctly
- [ ] Storm proximity check works
- [ ] Completion requirement validates properly
- [ ] Permission check works

### Integration
- [ ] Stormcraft storm detection works
- [ ] Essence integration awards/withdraws correctly
- [ ] MythicDungeons GUI opens properly
- [ ] Dungeon completion event fires

### Rewards
- [ ] Base essence amount awarded correctly
- [ ] Variance applied properly
- [ ] First completion bonus works
- [ ] Party members all receive rewards

---

## Known Limitations

1. **Requires MythicDungeons Premium** - Free version has limited features
2. **Portal Spawn Locations** - May spawn in non-ideal locations (cliffs, water)
3. **Cross-World Support** - Currently assumes all storms in same world
4. **Party System** - Uses MythicDungeons parties, not external party plugins

---

## Future Expansion

### Phase 2 Features
- [ ] Custom loot tables via MythicDungeons integration
- [ ] Leaderboard system for fastest completions
- [ ] Daily/weekly challenges with bonus rewards
- [ ] Difficulty modifiers (nightmare mode, etc.)
- [ ] Dungeon preview system (show dungeon before entering)

### Phase 3 Features
- [ ] Seasonal dungeons with unique themes
- [ ] Cross-server dungeon support
- [ ] Custom achievements and titles
- [ ] Dungeon crafting (combine keys to access secret dungeons)

---

## Troubleshooting

### Portals Not Spawning
1. Check storm intensity: `/storm` to see current intensity
2. Verify dungeon is enabled in config
3. Check max portals limit not reached
4. Ensure spawn chance roll succeeded (increase to 1.0 for testing)

### Cannot Enter Dungeon
1. Check SEL requirement: `/essence` to see your SEL
2. Verify essence balance: Check if you have enough essence
3. Confirm storm proximity: Must be within configured distance
4. Check permissions: Ensure player has dungeon permission

### Rewards Not Awarded
1. Check Vault economy is installed
2. Verify Stormcraft-Essence is installed (if using essence rewards)
3. Ensure dungeon completed successfully (not failed/abandoned)
4. Check console for errors

---

**Last Updated**: 2025-10-01
**Plugin Version**: 0.1.0
**Author**: Claude Code
