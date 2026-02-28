# Aion 5.8 Server Emulator

A community-driven server emulator for the MMORPG **Aion: The Tower of Eternity**, version 5.8.

> This project is **NOT** affiliated with NCSOFT or any official Aion development team.
> Based on [AionEncomBase](https://github.com/MATTYOneInc/AionEncomBase_Java8) with modifications and improvements.

## Architecture

```
Client (Aion 5.8)
    |
    v
Login Server (AL-Login)  ── Authentication, Account Management, Server List
    |
    v
Game Server (AL-Game)     ── World, Combat, AI, Quests, Instances, Services
    |
    v
PostgreSQL Database       ── Player Data, World State, Items, Quests
    |
Commons Library (AL-Commons) ── Network I/O, Database Layer, Configuration, Utilities
```

## Modules

| Module | Description |
|--------|-------------|
| **AL-Commons** | Shared library: NIO networking, database access (DAO), configuration, bytecode enhancement, scheduling |
| **AL-Game** | Core game server: world management, combat engine, AI2, quest engine, geo engine, instances, services |
| **AL-Login** | Login server: authentication, account management, encryption (NCrypt), server list |

## Key Features

- **Combat & Skill Engine** - Full skill system with effects, motion validation, and stat calculations
- **AI2 Engine** - NPC behavior system with scriptable AI handlers
- **Quest Engine** - Comprehensive quest system with scripted events
- **Instance System** - Instanced dungeons and raids with custom mechanics
- **GeoEngine** - Collision detection, terrain validation, and pathfinding (Recast4j NavMesh)
- **Siege System** - Territory control PvP warfare
- **Housing System** - Player housing with auctions and maintenance
- **Legion System** - Guild management and features
- **Broker** - In-game player marketplace
- **Crafting & Enchantment** - Item crafting, enchantment, and armsfusion
- **Event Engine** - Scheduled game events with automatic management
- **Admin Tools** - GM commands, ban system, broadcast, player management

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 25 |
| Build | Apache Maven |
| Database | PostgreSQL 14+ |
| Connection Pool | HikariCP 6.2.1 |
| Logging | SLF4J + Logback 1.5.x |
| Networking | Java NIO |
| Pathfinding | Recast4j (Detour NavMesh) 1.5.7 |
| Bytecode | Javassist 3.30.x |
| Collections | Guava 33.x, Trove4j 3.0.3 |
| Scheduling | Quartz Scheduler |

## Prerequisites

- **JDK 25** or later
- **Apache Maven 3.x**
- **PostgreSQL 14+**
- **2-8 GB RAM** (recommended for game server)

## Build

```bash
# Build all modules
mvn clean package

# Output:
#   AL-Commons/target/AL-Commons-5.8-SNAPSHOT.jar
#   AL-Game/target/AL-Game.zip
#   AL-Login/target/AL-Login.zip
```

## Database Setup

1. Install PostgreSQL and create a database
2. Import the schema:
   ```bash
   psql -U postgres -d aion_gs -f AL-Game/sql/al_server_gs.sql
   ```
3. Configure connection in `AL-Game/config/main/database.properties`

## Running

### Game Server

```bash
# Linux / macOS
./StartGS.sh

# Windows
StartGS.bat

# Manual
java -Xms2048m -Xmx8192m \
  -javaagent:libs/AL-Commons-5.8-SNAPSHOT.jar \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  -cp "libs/*" com.aionemu.gameserver.GameServer
```

### Login Server

```bash
java --add-opens java.base/java.lang=ALL-UNNAMED \
  -cp "libs/*" com.aionemu.loginserver.LoginServer
```

## Configuration

Configuration files are in `AL-Game/config/main/`:

| File | Purpose |
|------|---------|
| `gameserver.properties` | Core server settings |
| `rates.properties` | XP, drop, crafting rates |
| `pvp.properties` | PvP settings |
| `siege.properties` | Siege mechanics |
| `housing.properties` | Housing system |
| `geodata.properties` | Geo data and pathfinding |
| `custom.properties` | Custom gameplay mechanics |

40+ additional property files are available for fine-tuning.

## NPC Navigation (Optional)

For improved NPC pathfinding (adds ~2GB RAM usage):

1. Download [Navmeshes](https://drive.google.com/file/d/1ulkx0TwdDZnFZL5ildkVFtD1WQ3jGA7p/view?usp=sharing)
2. Place the `nav` folder into `AL-Game/data/`
3. Set `gameserver.geo.nav.pathfinding.enable = true` in `geodata.properties`

## Project Structure

```
encom-5.8/
├── AL-Commons/           # Shared utilities and base libraries
│   └── src/              # Network, database, config, callbacks
├── AL-Game/              # Game server (main module)
│   ├── src/              # Game logic (~10,000+ Java files)
│   ├── config/           # Server configuration
│   ├── data/             # Game data (static data, scripts, geo)
│   └── sql/              # Database schema
├── AL-Login/             # Login server
│   └── src/              # Authentication and account management
├── tools/                # Development utilities
│   └── nav-converter/    # NavMesh conversion tool
└── pom.xml               # Maven parent POM
```

## License

MIT License - see [LICENSE](LICENSE) for details.

## Acknowledgments

This project is built upon the work of many contributors from the Aion emulation community, including Aion-Lightning team, Encom team, and numerous individual developers. See the full list of contributors in this file.

## Disclaimer

This software is provided for educational and research purposes. The developers are not responsible for any misuse of this software. Aion is a registered trademark of NCSOFT Corporation.
