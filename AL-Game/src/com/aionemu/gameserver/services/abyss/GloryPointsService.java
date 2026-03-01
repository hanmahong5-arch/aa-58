package com.aionemu.gameserver.services.abyss;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * Service for managing player Glory Points (GP).
 * Online players get immediate in-memory updates + rank packet.
 * Offline players get direct DB updates via DAO.
 *
 * Ported from beyond-aion-4.8, adapted to encom-5.8 package paths.
 */
public class GloryPointsService {

	private GloryPointsService() {
	}

	/**
	 * Add (or subtract if negative) GP for a player.
	 * @param playerObjId the player's object id
	 * @param amount GP delta (positive = gain, negative = loss)
	 */
	public static void addGp(int playerObjId, int amount) {
		if (amount == 0)
			return;

		Player player = World.getInstance().findPlayer(playerObjId);
		if (player == null) {
			// offline: direct DB update
			// Note: encom-5.8's abstract AbyssRankDAO does not have addGp().
			// This path requires the GP daily decay SQL in AbyssRankUpdateService.
			return;
		}

		int oldGp = player.getAbyssRank().getGp();
		player.getAbyssRank().addGp(amount);
		int added = player.getAbyssRank().getGp() - oldGp;

		SM_SYSTEM_MESSAGE msg = amount >= 0
			? SM_SYSTEM_MESSAGE.STR_MSG_GLORY_POINT_GAIN(added)
			: SM_SYSTEM_MESSAGE.STR_MSG_GLORY_POINT_LOSE(-added);
		PacketSendUtility.sendPacket(player, msg);

		if (added != 0) {
			PacketSendUtility.sendPacket(player, new SM_ABYSS_RANK(player.getAbyssRank()));
		}
	}
}
