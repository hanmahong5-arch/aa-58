package com.aionemu.commons.configs;

import com.aionemu.commons.configuration.Property;

/**
 * Database configuration for HikariCP connection pool.
 */
public class DatabaseConfig {

	@Property(key = "database.url", defaultValue = "jdbc:postgresql://localhost:5432/aion_uni")
	public static String DATABASE_URL;

	@Property(key = "database.user", defaultValue = "root")
	public static String DATABASE_USER;

	@Property(key = "database.password", defaultValue = "")
	public static String DATABASE_PASSWORD;

	@Property(key = "database.connections.max", defaultValue = "10")
	public static int DATABASE_CONNECTIONS_MAX;

	@Property(key = "database.timeout", defaultValue = "30000")
	public static long DATABASE_TIMEOUT;
}
