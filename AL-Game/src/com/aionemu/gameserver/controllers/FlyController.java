package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.TransformType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * Manages player flight: flying, gliding, and state transitions.
 * Upgraded with beyond-aion-4.8 improvements:
 *   - Zone-based fly/glide validation (FLY / NO_FLY zones)
 *   - Deva-only flight restriction
 *   - Transform (polymorph) flight restriction
 *   - Cooldown hack prevention
 *   - FP reduce/restore lifecycle
 *
 * @author ATracer, upgraded from 4.8
 */
public class FlyController {

	private static final long FLY_REUSE_TIME = 10000;
	private Player player;
	private final ActionObserver glideObserver = new ActionObserver(ObserverType.ABNORMALSETTED) {
		@Override
		public void abnormalsetted(AbnormalState state) {
			if ((state.getId() & AbnormalState.CANT_MOVE_STATE.getId()) > 0 && !player.isInvulnerableWing()) {
				player.getFlyController().onStopGliding(true);
			}
		}
	};

	public FlyController(Player player) {
		this.player = player;
	}

	public void onStopGliding(boolean removeWings) {
		if (player.isInState(CreatureState.GLIDING)) {
			player.unsetState(CreatureState.GLIDING);

			if (player.isInState(CreatureState.FLYING)) {
				player.setFlyState(1);
				player.getLifeStats().triggerFpReduce();
			} else {
				player.setFlyState(0);
				player.getLifeStats().triggerFpRestore();
				if (removeWings) {
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.LAND, 0, 0), true);
				}
			}

			player.getObserveController().removeObserver(glideObserver);
			player.getGameStats().updateStatsAndSpeedVisually();
		}
	}

	/**
	 * End all flying states.
	 * Called by CM_EMOTION (pageDown), teleport, or FP=0.
	 */
	public void endFly(boolean forceEndFly) {
		if (player.isInState(CreatureState.FLYING) || player.isInState(CreatureState.GLIDING)) {
			player.unsetState(CreatureState.FLYING);
			player.unsetState(CreatureState.GLIDING);
			player.unsetState(CreatureState.FLOATING_CORPSE);
			player.setFlyState(0);

			player.getGameStats().updateStatsAndSpeedVisually();
			if (forceEndFly && player.isSpawned()) {
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.LAND, 0, 0), true);
			}

			player.getObserveController().removeObserver(glideObserver);
			player.getLifeStats().triggerFpRestore();
		}
	}

	/**
	 * Start flying. Called by CM_EMOTION when pageUp or fly button pressed.
	 * @return false if flight is restricted
	 */
	public boolean startFly() {
		if (!canFly()) {
			return false;
		}

		// cooldown hack prevention
		if (player.getFlyReuseTime() > System.currentTimeMillis()) {
			AuditLogger.info(player, "Possible fly cooldown hack. Remaining: "
				+ ((player.getFlyReuseTime() - System.currentTimeMillis()) / 1000) + "s");
			return false;
		}
		player.setFlyReuseTime(System.currentTimeMillis() + FLY_REUSE_TIME);

		player.setState(CreatureState.FLYING);
		if (player.isInPlayerMode(PlayerMode.RIDE)) {
			player.setState(CreatureState.FLOATING_CORPSE);
		}
		player.setFlyState(1);
		player.getLifeStats().triggerFpReduce();
		player.getGameStats().updateStatsAndSpeedVisually();

		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_EMOTE2, 0, 0), true);
		return true;
	}

	/**
	 * Switch to gliding mode.
	 * Called by CM_MOVE with VALIDATE_GLIDE movement type.
	 */
	public boolean switchToGliding() {
		if (player.isInState(CreatureState.GLIDING) || !player.canPerformMove()) {
			return false;
		}
		if (!canGlide()) {
			return false;
		}

		if (player.getFlyState() == 0) {
			// gliding from standing: apply cooldown
			if (player.getFlyReuseTime() > System.currentTimeMillis()) {
				return false;
			}
			player.setFlyReuseTime(System.currentTimeMillis() + FLY_REUSE_TIME);
			player.getLifeStats().triggerFpReduce();
		}

		player.setState(CreatureState.GLIDING);
		player.setFlyState(2);

		player.getObserveController().addObserver(glideObserver);
		player.getGameStats().updateStatsAndSpeedVisually();
		return true;
	}

	// --- Validation methods from 4.8 ---

	private boolean canFly() {
		// Deva check: in 5.8 all characters level >= 10 are Daeva
		if (player.getLevel() < 10) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GLIDE_ONLY_DEVA_CAN);
			return false;
		}
		if (player.getAccessLevel() < AdminConfig.GM_FLIGHT_FREE
			&& !player.isInsideZoneType(ZoneType.FLY)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FLYING_FORBIDDEN_HERE);
			return false;
		}
		if (player.getEffectController().isAbnormalSet(AbnormalState.NOFLY)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANT_FLY_NOW_DUE_TO_NOFLY);
			return false;
		}
		if (player.getTransformModel().isActive()
			&& player.getTransformModel().getType() != TransformType.PC) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FLY_CANNOT_FLY_POLYMORPH_STATUS);
			return false;
		}
		return player.getStore() == null;
	}

	private boolean canGlide() {
		if (player.getLevel() < 10) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GLIDE_ONLY_DEVA_CAN);
			return false;
		}
		if (player.getTransformModel().isActive()
			&& player.getTransformModel().getType() != TransformType.PC) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GLIDE_CANNOT_GLIDE_POLYMORPH_STATUS);
			return false;
		}
		return true;
	}
}
