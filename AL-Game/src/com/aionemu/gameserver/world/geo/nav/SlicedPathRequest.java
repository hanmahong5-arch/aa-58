package com.aionemu.gameserver.world.geo.nav;

import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.Result;
import org.recast4j.detour.StraightPathItem;
import org.recast4j.detour.Status;

import java.util.List;

/**
 * Sliced (multi-frame) pathfinding request to prevent main-thread blocking.
 * Each tick, call {@link #advance(int)} with an iteration budget.
 * When {@link #isDone()} returns true, call {@link #getResult()} for the waypoints.
 *
 * Coordinate convention: all input/output in Aion coordinates.
 * Internal computation uses Detour Y-up system.
 */
public class SlicedPathRequest {

	public enum State {
		PENDING, COMPUTING, COMPLETE, FAILED
	}

	private static final float[] HALF_EXTENTS = { 2.0f, 4.0f, 2.0f };
	private static final DefaultQueryFilter FILTER = new DefaultQueryFilter();
	private static final int MAX_STRAIGHT = 64;

	private State state = State.PENDING;
	private final NavMeshQuery query;
	private final float[] startDetour;
	private final float[] endDetour;
	private final float endAionX, endAionY, endAionZ;
	private long startRef;
	private long endRef;
	private float[][] result;

	/**
	 * Create a sliced path request.
	 * @param worldId map id for NavMesh lookup
	 * @param sx start X in Aion coordinates
	 * @param sy start Y in Aion coordinates
	 * @param sz start Z in Aion coordinates
	 * @param ex end X in Aion coordinates
	 * @param ey end Y in Aion coordinates
	 * @param ez end Z in Aion coordinates
	 */
	public SlicedPathRequest(int worldId, float sx, float sy, float sz, float ex, float ey, float ez) {
		this.endAionX = ex;
		this.endAionY = ey;
		this.endAionZ = ez;

		NavMesh mesh = RecastNavData.getInstance().getNavMesh(worldId);
		if (mesh == null) {
			state = State.FAILED;
			query = null;
			startDetour = null;
			endDetour = null;
			return;
		}

		query = new NavMeshQuery(mesh);
		startDetour = RecastNavService.aionToDetour(sx, sy, sz);
		endDetour = RecastNavService.aionToDetour(ex, ey, ez);

		// resolve start/end polys
		Result<FindNearestPolyResult> sResult = query.findNearestPoly(startDetour, HALF_EXTENTS, FILTER);
		Result<FindNearestPolyResult> eResult = query.findNearestPoly(endDetour, HALF_EXTENTS, FILTER);

		if (sResult.failed() || sResult.result.getNearestRef() == 0 ||
		    eResult.failed() || eResult.result.getNearestRef() == 0) {
			state = State.FAILED;
			return;
		}

		startRef = sResult.result.getNearestRef();
		endRef = eResult.result.getNearestRef();

		// initSlicedFindPath returns Status directly
		Status initStatus = query.initSlicedFindPath(startRef, endRef,
			sResult.result.getNearestPos(), eResult.result.getNearestPos(), FILTER, 0);
		if (initStatus.isFailed()) {
			state = State.FAILED;
			return;
		}
		state = State.COMPUTING;
	}

	/**
	 * Advance the sliced pathfinding computation by up to maxIter iterations.
	 * @return true if computation is complete (success or failure)
	 */
	public boolean advance(int maxIter) {
		if (state != State.COMPUTING) return isDone();

		Result<Integer> updateResult = query.updateSlicedFindPath(maxIter);
		if (updateResult.failed()) {
			state = State.FAILED;
			return true;
		}

		if (updateResult.status.isInProgress()) {
			return false; // still computing
		}

		// finalize - returns Result<List<Long>>
		Result<List<Long>> pathResult = query.finalizeSlicedFindPath();
		if (pathResult.failed() || pathResult.result == null || pathResult.result.isEmpty()) {
			state = State.FAILED;
			return true;
		}

		// smooth to straight path (findStraightPath takes List<Long>)
		Result<List<StraightPathItem>> straightResult = query.findStraightPath(
			startDetour, endDetour, pathResult.result, MAX_STRAIGHT, 0);

		if (straightResult.failed() || straightResult.result == null || straightResult.result.isEmpty()) {
			result = new float[][] { { endAionX, endAionY, endAionZ } };
		} else {
			List<StraightPathItem> items = straightResult.result;
			int startIdx = items.size() > 1 ? 1 : 0;
			result = new float[items.size() - startIdx][];
			for (int i = startIdx; i < items.size(); i++) {
				float[] pos = items.get(i).getPos();
				result[i - startIdx] = RecastNavService.detourToAion(pos[0], pos[1], pos[2]);
			}
		}
		state = (result != null && result.length > 0) ? State.COMPLETE : State.FAILED;
		return true;
	}

	public float[][] getResult() {
		return result;
	}

	public boolean isDone() {
		return state == State.COMPLETE || state == State.FAILED;
	}

	public boolean isSuccess() {
		return state == State.COMPLETE;
	}

	public State getState() {
		return state;
	}
}
