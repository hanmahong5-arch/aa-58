package com.aionemu.commons.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Database connection factory using HikariCP connection pool.
 */
public class DatabaseFactory {

	private static final Logger log = LoggerFactory.getLogger(DatabaseFactory.class);
	private static HikariDataSource dataSource;
	private static String databaseName;
	private static int databaseMajorVersion;
	private static int databaseMinorVersion;

	public static synchronized void init() {
		if (dataSource != null) {
			return;
		}

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(DatabaseConfig.DATABASE_URL);
		config.setUsername(DatabaseConfig.DATABASE_USER);
		config.setPassword(DatabaseConfig.DATABASE_PASSWORD);
		config.setMaximumPoolSize(DatabaseConfig.DATABASE_CONNECTIONS_MAX);
		config.setConnectionTimeout(DatabaseConfig.DATABASE_TIMEOUT);

		try {
			dataSource = new HikariDataSource(config);
		} catch (Exception e) {
			log.error("Error while creating DB Connection pool", e);
			throw new Error("DatabaseFactory not initialized!", e);
		}

		try (Connection c = getConnection()) {
			DatabaseMetaData dmd = c.getMetaData();
			databaseName = dmd.getDatabaseProductName();
			databaseMajorVersion = dmd.getDatabaseMajorVersion();
			databaseMinorVersion = dmd.getDatabaseMinorVersion();
		} catch (Exception e) {
			log.error("Error validating database connection (check JDBC URL and credentials)", e);
			throw new Error("DatabaseFactory not initialized!");
		}

		log.info("Successfully connected to database");
	}

	public static Connection getConnection() throws SQLException {
		if (dataSource == null) {
			throw new SQLException(
					"DatabaseFactory not initialized — call init() before getConnection()");
		}
		Connection con = dataSource.getConnection();
		if (!con.getAutoCommit()) {
			log.error("Connection obtained from database factory should be in auto-commit mode. Forcing auto-commit to true.");
			con.setAutoCommit(true);
		}
		return con;
	}

	public int getActiveConnections() {
		return dataSource.getHikariPoolMXBean().getActiveConnections();
	}

	public int getIdleConnections() {
		return dataSource.getHikariPoolMXBean().getIdleConnections();
	}

	public static synchronized void shutdown() {
		try {
			if (dataSource != null) {
				dataSource.close();
			}
		} catch (Exception e) {
			log.warn("Failed to shutdown DatabaseFactory", e);
		}
		dataSource = null;
	}

	public static void close(PreparedStatement st, Connection con) {
		close(st);
		close(con);
	}

	public static void close(PreparedStatement st) {
		if (st != null) {
			try {
				if (!st.isClosed()) {
					st.close();
				}
			} catch (SQLException e) {
				log.error("Can't close Prepared Statement", e);
			}
		}
	}

	public static void close(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				log.error("DatabaseFactory: Failed to close database connection!", e);
			}
		}
	}

	public static String getDatabaseName() {
		return databaseName;
	}

	public static int getDatabaseMajorVersion() {
		return databaseMajorVersion;
	}

	public static int getDatabaseMinorVersion() {
		return databaseMinorVersion;
	}

	private DatabaseFactory() {
	}
}
