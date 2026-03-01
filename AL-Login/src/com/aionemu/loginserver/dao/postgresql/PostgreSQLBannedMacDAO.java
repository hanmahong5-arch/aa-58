package com.aionemu.loginserver.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.loginserver.dao.BannedMacDAO;
import com.aionemu.loginserver.model.base.BannedMacEntry;

/**
 * PostgreSQL BannedMac DAO implementation
 * 
 * @author
 */
public class PostgreSQLBannedMacDAO extends BannedMacDAO {

    private static Logger log = LoggerFactory.getLogger(PostgreSQLBannedMacDAO.class);

    @Override
    public Map<String, BannedMacEntry> load() {
        Map<String, BannedMacEntry> map = new ConcurrentHashMap<>();
        String query = "SELECT \"address\", \"time\", \"details\" FROM \"banned_mac\"";
        
        try (Connection con = DatabaseFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String address = rs.getString("address");
                map.put(address, new BannedMacEntry(address, rs.getTimestamp("time"), rs.getString("details")));
            }
        } catch (SQLException e) {
            log.error("Error loading banned MAC addresses", e);
        }
        
        return map;
    }

    @Override
    public boolean update(BannedMacEntry entry) {
        String query = "INSERT INTO \"banned_mac\" (\"address\", \"time\", \"details\") VALUES (?, ?, ?) ON CONFLICT (address) DO UPDATE SET \"time\"=EXCLUDED.\"time\", details=EXCLUDED.details";
        
        try (Connection con = DatabaseFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setString(1, entry.getMac());
            ps.setTimestamp(2, entry.getTime());
            ps.setString(3, entry.getDetails());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error storing BannedMacEntry " + entry.getMac(), e);
        }
        
        return false;
    }

    @Override
    public boolean remove(String address) {
        String query = "DELETE FROM \"banned_mac\" WHERE address = ?";
        
        try (Connection con = DatabaseFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setString(1, address);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error removing BannedMacEntry " + address, e);
        }
        
        return false;
    }

    @Override
    public void cleanExpiredBans() {
        String query = "DELETE FROM \"banned_mac\" WHERE \"time\" < CURRENT_DATE";
        
        try (Connection con = DatabaseFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                log.info("Cleaned " + deleted + " expired MAC bans");
            }
        } catch (SQLException e) {
            log.error("Error cleaning expired MAC bans", e);
        }
    }
}