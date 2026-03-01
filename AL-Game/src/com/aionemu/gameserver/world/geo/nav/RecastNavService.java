package com.aionemu.gameserver.world.geo.nav;

import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.Result;
import org.recast4j.detour.StraightPathItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;

import java.util.List;

/**
 * Recast4j-based navigation service. Drop-in replacement for NavService API.
 *
 * Coordinate convention (applied at all boundaries):
 *   Aion  -> Detour: detourPos = { aionX, aionZ, -aionY }
 *   Detour -> Aion:  aionPos  = { detourX, -detourZ, detourY }
 */
public final class RecastNavService {

	private static final Logger log = LoggerFactory.getLogger(RecastNavService.class);

	// poly search half-extents (detour Y-up coords): search +-2 in X/Z, +-4 in Y(height)
	private static final float[] HALF_EXTENTS = { 2.0f, 4.0f, 2.0f };
	private static final DefaultQueryFilter FILTER = new DefaultQueryFilter();
	private static final int MAX_STRAIGHT = 64;

	private final RecastNavData navData = RecastNavData.getInstance();

	private RecastNavService() {
	}

	/**
	 * Navigate creature to target creature.
	 * @return array of waypoints [x,y,z] in Aion coordinates, or null on failure
	 */
	public float[][] navigateToTarget(Creature owner, Creature target) {
		if (owner == null || target == null) return null;
		if (owner.getLifeStats().isAlreadyDead()) return null;
		if (owner.getWorldId() != target.getWorldId()) return null;
		return navigateInternal(owner.getWorldId(),
			owner.getX(), owner.getY(), owner.getZ(),
			target.getX(), target.getY(), target.getZ());
	}

	/**
	 * Navigate creature to a specific world location.
	 * @return array of waypoints [x,y,z] in Aion coordinates, or null on failure
	 */
	public float[][] navigateToLocation(Creature owner, float x, float y, float z) {
		if (owner == null) return null;
		if (owner.getLifeStats().isAlreadyDead()) return null;
		return navigateInternal(owner.getWorldId(),
			owner.getX(), owner.getY(), owner.getZ(),
			x, y, z);
	}

	/**
	 * Check if creature can pull target (navmesh continuity check).
	 */
	public boolean canPullTarget(Creature creature, Creature target) {
		if (!GeoDataConfig.GEO_NAV_RECAST_ENABLE) return true;
		if (target.isFlying()) return true;

		NavMesh mesh = navData.getNavMesh(creature.getWorldId());
		if (mesh == null) return true; // no data -> allow

		NavMeshQuery query = new NavMeshQuery(mesh);
		float[] startDetour = aionToDetour(creature.getX(), creature.getY(), creature.getZ());
		float[] endDetour = aionToDetour(target.getX(), target.getY(), target.getZ());

		Result<FindNearestPolyResult> startResult = query.findNearestPoly(startDetour, HALF_EXTENTS, FILTER);
		if (startResult.failed() || startResult.result.getNearestRef() == 0) return false;

		Result<FindNearestPolyResult> endResult = query.findNearestPoly(endDetour, HALF_EXTENTS, FILTER);
		if (endResult.failed() || endResult.result.getNearestRef() == 0) return false;

		Result<List<Long>> pathResult = query.findPath(
			startResult.result.getNearestRef(), endResult.result.getNearestRef(),
			startDetour, endDetour, FILTER);

		return pathResult.succeeded() && pathResult.result != null && !pathResult.result.isEmpty();
	}

	/**
	 * Internal pathfinding: findNearestPoly -> findPath -> findStraightPath -> convert back.
	 */
	private float[][] navigateInternal(int worldId,
			float sx, float sy, float sz,
			float ex, float ey, float ez) {

		NavMesh mesh = navData.getNavMesh(worldId);
		if (mesh == null) return null;

		NavMeshQuery query = new NavMeshQuery(mesh);

		// convert Aion coords -> Detour coords
		float[] startDetour = aionToDetour(sx, sy, sz);
		float[] endDetour = aionToDetour(ex, ey, ez);

		// 1. Find nearest polygon for start
		Result<FindNearestPolyResult> startResult = query.findNearestPoly(startDetour, HALF_EXTENTS, FILTER);
		if (startResult.failed() || startResult.result.getNearestRef() == 0) {
			return null;
		}

		// 2. Find nearest polygon for end
		Result<FindNearestPolyResult> endResult = query.findNearestPoly(endDetour, HALF_EXTENTS, FILTER);
		if (endResult.failed() || endResult.result.getNearestRef() == 0) {
			return null;
		}

		// 3. Find polygon path (returns Result<List<Long>>)
		Result<List<Long>> pathResult = query.findPath(
			startResult.result.getNearestRef(), endResult.result.getNearestRef(),
			startResult.result.getNearestPos(), endResult.result.getNearestPos(),
			FILTER);

		if (pathResult.failed() || pathResult.result == null || pathResult.result.isEmpty()) {
			return null;
		}

		// 4. Smooth to straight path waypoints (findStraightPath takes List<Long>)
		Result<List<StraightPathItem>> straightResult = query.findStraightPath(
			startResult.result.getNearestPos(), endResult.result.getNearestPos(),
			pathResult.result, MAX_STRAIGHT, 0);

		if (straightResult.failed() || straightResult.result == null || straightResult.result.isEmpty()) {
			// fallback: return direct endpoint
			return new float[][] { { ex, ey, ez } };
		}

		// 5. Convert Detour waypoints back to Aion coordinates
		List<StraightPathItem> items = straightResult.result;
		// skip the first point (current position), start from index 1
		int startIdx = items.size() > 1 ? 1 : 0;
		float[][] result = new float[items.size() - startIdx][];
		for (int i = startIdx; i < items.size(); i++) {
			float[] pos = items.get(i).getPos();
			result[i - startIdx] = detourToAion(pos[0], pos[1], pos[2]);
		}
		return result.length > 0 ? result : null;
	}

	// --- Coordinate conversion helpers ---
	// Aion = Z-up: (x, y, z)  ->  Detour = Y-up: (x, z, -y)

	static float[] aionToDetour(float ax, float ay, float az) {
		return new float[] { ax, az, -ay };
	}

	static float[] detourToAion(float dx, float dy, float dz) {
		return new float[] { dx, -dz, dy };
	}

	public static RecastNavService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static final class SingletonHolder {
		static final RecastNavService INSTANCE = new RecastNavService();
	}
}
