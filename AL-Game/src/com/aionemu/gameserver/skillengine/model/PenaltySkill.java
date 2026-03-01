package com.aionemu.gameserver.skillengine.model;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * A penalty skill that applies effects without property checks or cast animations.
 * Used for debuffs triggered by game mechanics (e.g., chain skill failure penalties).
 */
public class PenaltySkill extends Skill {

	public PenaltySkill(SkillTemplate skillTemplate, Creature effector, int skillLevel) {
		super(skillTemplate, effector, skillLevel, effector, null);
	}

	@Override
	public boolean useSkill() {
		return super.useWithoutPropSkill();
	}
}
