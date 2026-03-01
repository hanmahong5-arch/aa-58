package com.aionemu.loginserver.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.loginserver.dao.PlayerTransferDAO;
import com.aionemu.loginserver.service.ptransfer.PlayerTransferTask;

/**
 * PostgreSQL PlayerTransfer DAO implementation
 * 
 * @author
 */
public class PostgreSQLPlayerTransferDAO extends PlayerTransferDAO {

    private static final Logger log = LoggerFactory.getLogger(PostgreSQLPlayerTransferDAO.class);

    @Override
    public ArrayList<PlayerTransferTask> getNew() {
        ArrayList<PlayerTransferTask> list = new ArrayList<>();
        String query = "SELECT * FROM player_transfers WHERE \"status\" = ?";
        
        try (Connection con = DatabaseFactory.getConnection();
             PreparedStatement st = con.prepareStatement(query)) {
            
            st.setInt(1, 0);
            
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    PlayerTransferTask task = new PlayerTransferTask();
                    task.id = rs.getInt("id");
                    task.sourceServerId = (byte) rs.getShort("source_server");
                    task.targetServerId = (byte) rs.getShort("target_server");
                    task.sourceAccountId = rs.getInt("source_account_id");
                    task.targetAccountId = rs.getInt("target_account_id");
                    task.playerId = rs.getInt("player_id");
                    list.add(task);
                }
            }
        } catch (SQLException e) {
            log.error("Can't select new player transfers", e);
        }

        return list;
    }

    @Override
    public boolean update(final PlayerTransferTask task) {
        StringBuilder query = new StringBuilder("UPDATE player_transfers SET status = ?, comment = ?");
        
        switch (task.status) {
            case PlayerTransferTask.STATUS_ACTIVE:
                query.append(", time_performed = NOW()");
                break;
            case PlayerTransferTask.STATUS_DONE:
            case PlayerTransferTask.STATUS_ERROR:
                query.append(", time_done = NOW()");
                break;
        }
        
        query.append(" WHERE id = ?");
        
        try (Connection con = DatabaseFactory.getConnection();
             PreparedStatement st = con.prepareStatement(query.toString())) {
            
            st.setByte(1, task.status);
            st.setString(2, task.comment);
            st.setInt(3, task.id);
            
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Can't update player transfer task: " + task.id, e);
        }
        
        return false;
    }
}