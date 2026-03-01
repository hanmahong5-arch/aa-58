package com.aionemu.gameserver.controllers.movement;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * Movement controller for flying/aerial NPCs.
 * Skips NavMesh pathfinding and GeoService Z-axis ground clamping.
 * Maintains altitude offset above target to prevent ground-hugging.
 */
public class AerialNpcMoveController extends NpcMoveController {

	private static final float ALTITUDE_OFFSET = 3.0f;

	public AerialNpcMoveController(Npc owner) {
		super(owner);
	}

	@Override
	protected void moveToLocation(float targetX, float targetY, float targetZ, float offset) {
		// aerial units fly directly toward target without nav or geo corrections
		super.moveToLocation(targetX, targetY, targetZ + ALTITUDE_OFFSET, offset);
	}
}
