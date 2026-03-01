package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.postgresql.*;

/**
 * Registers all PostgreSQL DAO implementations for the Game Server.
 * Replaces the former ScriptManager-based dynamic discovery.
 *
 * All registrations are wrapped in a fail-fast block: if any single
 * registration throws, the entire server startup is aborted to prevent
 * running with a partially-initialized DAO layer.
 */
public final class GameDAORegistry {

    public static void init() {
        try {
            DAOManager.registerDAO(new PostgreSQLAbyssLandingDAO());
            DAOManager.registerDAO(new PostgreSQLAbyssRankDAO());
            DAOManager.registerDAO(new PostgreSQLAbyssSpecialLandingDAO());
            DAOManager.registerDAO(new PostgreSQLAnnouncementsDAO());
            DAOManager.registerDAO(new PostgreSQLBaseDAO());
            DAOManager.registerDAO(new PostgreSQLBlockListDAO());
            DAOManager.registerDAO(new PostgreSQLBrokerDAO());
            DAOManager.registerDAO(new PostgreSQLChallengeTasksDAO());
            DAOManager.registerDAO(new PostgreSQLCraftCooldownsDAO());
            DAOManager.registerDAO(new PostgreSQLEventItemsDAO());
            DAOManager.registerDAO(new PostgreSQLF2pDAO());
            DAOManager.registerDAO(new PostgreSQLFriendListDAO());
            DAOManager.registerDAO(new PostgreSQLGuideDAO());
            DAOManager.registerDAO(new PostgreSQLHouseBidsDAO());
            DAOManager.registerDAO(new PostgreSQLHouseObjectCooldownsDAO());
            DAOManager.registerDAO(new PostgreSQLHouseScriptsDAO());
            DAOManager.registerDAO(new PostgreSQLHousesDAO());
            DAOManager.registerDAO(new PostgreSQLInGameShopDAO());
            DAOManager.registerDAO(new PostgreSQLInventoryDAO());
            DAOManager.registerDAO(new PostgreSQLItemCooldownsDAO());
            DAOManager.registerDAO(new PostgreSQLItemStoneListDAO());
            DAOManager.registerDAO(new PostgreSQLLadderDAO());
            DAOManager.registerDAO(new PostgreSQLLegionDAO());
            DAOManager.registerDAO(new PostgreSQLLegionMemberDAO());
            DAOManager.registerDAO(new PostgreSQLMailDAO());
            DAOManager.registerDAO(new PostgreSQLMotionDAO());
            DAOManager.registerDAO(new PostgreSQLOldNamesDAO());
            DAOManager.registerDAO(new PostgreSQLOutpostDAO());
            DAOManager.registerDAO(new PostgreSQLPetitionDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerAppearanceDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerAtreianBestiaryDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerBindPointDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerCooldownsDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerCreativityPointsDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerEffectsDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerEmotionListDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerEventsWindowDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerLifeStatsDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerLunaShopDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerMacrossesDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerMinionsDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerNpcFactionsDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerPasskeyDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerPassportsDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerPetsDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerPunishmentsDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerQuestListDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerRecipesDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerRegisteredItemsDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerSettingsDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerShugoSweepDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerSkillListDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerSkillSkinListDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerStigmasEquippedDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerThievesDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerTitleListDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerTransfoDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerVarsDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerWardrobeDAO());
            DAOManager.registerDAO(new PostgreSQLPortalCooldownsDAO());
            DAOManager.registerDAO(new PostgreSQLRewardServiceDAO());
            DAOManager.registerDAO(new PostgreSQLSeasonRankingDAO());
            DAOManager.registerDAO(new PostgreSQLServerVariablesDAO());
            DAOManager.registerDAO(new PostgreSQLSiegeDAO());
            DAOManager.registerDAO(new PostgreSQLSurveyControllerDAO());
            DAOManager.registerDAO(new PostgreSQLTaskFromDBDAO());
            DAOManager.registerDAO(new PostgreSQLTownDAO());
            DAOManager.registerDAO(new PostgreSQLVeteranRewardsDAO());
            DAOManager.registerDAO(new PostgreSQLWeddingDAO());
        } catch (Exception e) {
            throw new Error("GameDAORegistry initialization failed — aborting server startup", e);
        }
    }

    private GameDAORegistry() {
    }
}
