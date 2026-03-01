package com.aionemu.loginserver.dao;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.loginserver.dao.postgresql.*;

/**
 * Registers all PostgreSQL DAO implementations for the Login Server.
 * Replaces the former ScriptManager-based dynamic discovery.
 *
 * All registrations are wrapped in a fail-fast block: if any single
 * registration throws, the entire server startup is aborted to prevent
 * running with a partially-initialized DAO layer.
 */
public final class LoginDAORegistry {

    public static void init() {
        try {
            DAOManager.registerDAO(new PostgreSQLAccountDAO());
            DAOManager.registerDAO(new PostgreSQLAccountPlayTimeDAO());
            DAOManager.registerDAO(new PostgreSQLAccountTimeDAO());
            DAOManager.registerDAO(new PostgreSQLBannedIpDAO());
            DAOManager.registerDAO(new PostgreSQLBannedMacDAO());
            DAOManager.registerDAO(new PostgreSQLGameServersDAO());
            DAOManager.registerDAO(new PostgreSQLPlayerTransferDAO());
            DAOManager.registerDAO(new PostgreSQLPremiumDAO());
            DAOManager.registerDAO(new PostgreSQLSvStatsDAO());
            DAOManager.registerDAO(new PostgreSQLTaskFromDBDAO());
        } catch (Exception e) {
            throw new Error("LoginDAORegistry initialization failed — aborting server startup", e);
        }
    }

    private LoginDAORegistry() {
    }
}
