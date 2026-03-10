# Encom 5.8 - Java 25 Compilation Report

## Date: 2026-03-07

## Build Result: SUCCESS

All three modules compile cleanly on Java 25 with zero errors.

```
Reactor Summary for encom-server 5.8-SNAPSHOT:
  encom-server (parent)     SUCCESS [  0.753 s]
  Encom Commons             SUCCESS [ 10.980 s]
  Encom Game Server         SUCCESS [ 44.783 s]
  Encom Login Server        SUCCESS [  2.578 s]
  Total time: 59.297 s
```

## Build Environment

- **Java**: OpenJDK 25.0.1 (Eclipse Temurin-25.0.1+8-LTS)
- **Maven**: Apache Maven 3.9.11
- **OS**: Windows Server 2019 / CYGWIN_NT-10.0-17763
- **Compiler Plugin**: maven-compiler-plugin 3.14.1
- **Target Release**: Java 25 (`maven.compiler.release=25`)

## Code Changes Required: 1 (discovered during server boot)

### Fix 1: SiegeService.java — ConcurrentModificationException on startup

**File**: `AL-Game/src/com/aionemu/gameserver/services/SiegeService.java`
**Line**: 383-386

**Before**:
```java
public void deSpawnNpcs(int siegeLocationId) {
    Collection<SiegeNpc> siegeNpcs = World.getInstance().getLocalSiegeNpcs(siegeLocationId);
    for (SiegeNpc npc : siegeNpcs) {
        npc.getController().onDelete();
    }
}
```

**After**:
```java
public void deSpawnNpcs(int siegeLocationId) {
    List<SiegeNpc> siegeNpcs = new java.util.ArrayList<>(World.getInstance().getLocalSiegeNpcs(siegeLocationId));
    for (SiegeNpc npc : siegeNpcs) {
        npc.getController().onDelete();
    }
}
```

**Reason**: `onDelete()` modifies the underlying live collection while iteration was in progress, causing `ConcurrentModificationException`. Fixed by creating a defensive copy snapshot before iteration.

## Server Boot Results (Story 1.3)

| Component | Status | Port | Startup Time |
|-----------|--------|------|-------------|
| Login Server | RUNNING | 2106 (client), 9014 (GS) | 2 seconds |
| Game Server | RUNNING | 7777 (client) | 77 seconds |

**Gameserver #1 authenticated and online as of 2026-03-08.**

## Build Artifacts

| Module | Artifact | Size |
|--------|----------|------|
| AL-Commons | `AL-Commons-5.8-SNAPSHOT.jar` | 217 KB |
| AL-Game | `AL-Game.zip` (distribution) | 71 MB |
| AL-Login | `AL-Login.zip` (distribution) | 9 MB |

## Compiler Warnings Summary

Total warnings: **229** (none block the build)

### Warning Categories

1. **Unchecked cast warnings** — Generic type erasure in collections (e.g., raw type casts in NpcMoveController, SpawnsData2)
2. **Orphan Javadoc comments** — Javadoc comments not attached to any declaration (AL-Login: ~60 files affected)
3. **Serializable field warnings** — Non-transient fields in serializable classes (LastUsedCache)
4. **Deprecation usage** — Use of deprecated APIs (Item.java, PingPongThread.java, GarbageCollector.java)
5. **Narrowing conversion** — Implicit long-to-short conversion (NpcEquippedGear.java)

### Lint Configuration

Warnings are suppressed in the POM via:
```
-Xlint:all,-deprecation,-unchecked,-rawtypes,-this-escape,-preview
```

These warnings are informational and do not affect functionality.

## Key Dependencies (Already Java 25 Compatible)

| Library | Version | Status |
|---------|---------|--------|
| HikariCP | 6.2.1 | Compatible |
| PostgreSQL JDBC | 42.7.4 | Compatible |
| Guava | 33.4.8-jre | Compatible |
| Javassist | 3.30.2-GA | Compatible |
| Logback | 1.5.21 | Compatible |
| Quartz | 2.5.1 | Compatible |
| JAXB API | 2.3.1 | Compatible (external dep) |
| JAXB Impl | 2.3.9 | Compatible |
| recast4j | 1.5.7 | Compatible |

## Runtime Notes

The startup scripts (`StartGS.sh`, `StartLS.sh`) include necessary `--add-opens` flags for Java module system compatibility:
- `--add-opens java.base/java.lang=ALL-UNNAMED`
- `--add-opens java.base/java.lang.reflect=ALL-UNNAMED`
- `--add-opens java.base/java.io=ALL-UNNAMED`
