package com.aionemu.loginserver.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.loginserver.dao.AccountPlayTimeDAO;
import com.aionemu.loginserver.model.AccountTime;

/**
 * PostgreSQL Account PlayTime DAO implementation
 * 
 * @author
 */
public class PostgreSQLAccountPlayTimeDAO extends AccountPlayTimeDAO {
    
    private static final Logger log = LoggerFactory.getLogger(PostgreSQLAccountPlayTimeDAO.class);

    @Override
    public boolean update(final Integer accountId, final AccountTime accountTime) {
        String query = "INSERT INTO account_playtime (\"account_id\", \"accumulated_online\") VALUES (?, ?) " + "ON CONFLICT (account_id) DO UPDATE SET accumulated_online = account_playtime.accumulated_online + EXCLUDED.accumulated_online";
        
        try (Connection con = DatabaseFactory.getConnection();
             PreparedStatement st = con.prepareStatement(query)) {
            
            st.setInt(1, accountId);
            st.setLong(2, accountTime.getAccumulatedOnlineTime());
            
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Can't update playtime for account: " + accountId, e);
        }
        
        return false;
    }
}