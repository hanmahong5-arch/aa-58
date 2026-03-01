package com.aionemu.gameserver.world.geo.nav;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

import org.recast4j.detour.NavMesh;
import org.recast4j.detour.io.MeshSetReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;

/**
 * Lazy-loading cache for recast4j Detour NavMesh data.
 * Reads standard Detour binary format from {GEO_NAV_RECAST_DATA_DIR}/{worldId}.bin.
 * Falls back to null (which triggers old NavService fallback) if file is absent or corrupt.
 *
 * Coordinate convention (global):
 *   Aion  = Z-up right-hand system
 *   Detour = Y-up system
 *   Conversion: detour(x, z, -y) <-> aion(x, y, z)
 *   Applied in RecastNavService at all pos array entry/exit points.
 */
public final class RecastNavData {

	private static final Logger log = LoggerFactory.getLogger(RecastNavData.class);

	private final ConcurrentHashMap<Integer, NavMesh> meshCache = new ConcurrentHashMap<>();

	private RecastNavData() {
	}

	/**
	 * Retrieve (or lazily load) the NavMesh for the given worldId.
	 * @return NavMesh or null if data is unavailable
	 */
	public NavMesh getNavMesh(int worldId) {
		return meshCache.computeIfAbsent(worldId, this::loadNavMesh);
	}

	private NavMesh loadNavMesh(int worldId) {
		Path navFile = Paths.get(GeoDataConfig.GEO_NAV_RECAST_DATA_DIR, worldId + ".bin");
		if (!Files.exists(navFile)) {
			return null; // no recast data -> fallback to legacy NavService
		}
		try {
			byte[] rawData = Files.readAllBytes(navFile);
			ByteBuffer buf = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN);
			MeshSetReader reader = new MeshSetReader();
			NavMesh mesh = reader.read(buf, NavMesh.DT_MAX_AREAS);
			log.info("Loaded recast4j NavMesh for worldId {} ({} bytes)", worldId, rawData.length);
			return mesh;
		} catch (Exception e) {
			log.warn("Failed to load recast4j NavMesh for worldId {}: {}", worldId, e.getMessage());
			return null;
		}
	}

	/**
	 * Pre-check if the recast data directory exists and is accessible.
	 * Called during server startup.
	 */
	public boolean validateDataDir() {
		Path dir = Paths.get(GeoDataConfig.GEO_NAV_RECAST_DATA_DIR);
		if (!Files.isDirectory(dir)) {
			log.warn("Recast NavMesh data directory does not exist: {}", dir.toAbsolutePath());
			return false;
		}
		log.info("Recast NavMesh data directory validated: {}", dir.toAbsolutePath());
		return true;
	}

	/**
	 * Startup preload: validate data directory and log available navmesh files.
	 */
	public void preload() {
		Path dir = Paths.get(GeoDataConfig.GEO_NAV_RECAST_DATA_DIR);
		if (!Files.isDirectory(dir)) {
			log.warn("Recast NavMesh data directory not found: {} — recast pathfinding will fall back to legacy NavService",
				dir.toAbsolutePath());
			return;
		}
		try (var stream = Files.list(dir)) {
			long count = stream.filter(p -> p.toString().endsWith(".bin")).count();
			log.info("Recast NavMesh data directory: {} ({} navmesh files)", dir.toAbsolutePath(), count);
		} catch (IOException e) {
			log.warn("Failed to scan recast data directory: {}", e.getMessage());
		}
	}

	/**
	 * Evict a specific world from cache (e.g., on instance shutdown).
	 */
	public void evict(int worldId) {
		meshCache.remove(worldId);
	}

	public static RecastNavData getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static final class SingletonHolder {
		static final RecastNavData INSTANCE = new RecastNavData();
	}
}
