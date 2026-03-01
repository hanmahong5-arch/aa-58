package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.templates.rewards.RewardEntryItem;

import java.util.ArrayList;

public abstract class RewardServiceDAO implements DAO {
	@Override
	public final String getClassName() {
		return RewardServiceDAO.class.getName();
	}

	public abstract ArrayList<RewardEntryItem> getAvailable(int playerId);

	public abstract void uncheckAvailable(ArrayList<Integer> ids);

	public abstract void setUpdateDown(int unique);

	public abstract boolean setUpdate(int unique);
}