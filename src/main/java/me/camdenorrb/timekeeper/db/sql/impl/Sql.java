package me.camdenorrb.timekeeper.db.sql.impl;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.camdenorrb.timekeeper.base.Connectable;

import java.io.File;


// A SQL client
public final class Sql implements Connectable {

	private boolean isConnected;

	private HikariDataSource dataSource;

	private final HikariConfig hikariConfig;


	public Sql(final SqlConfig config) {

		final HikariConfig hikariConfig = new HikariConfig();

		hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getHost() + ':' + config.getPort() + '/' + config.getBase() + "?useSSL=false");
		hikariConfig.setUsername(config.getUser());
		hikariConfig.setPassword(config.getPass());
		hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
		hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
		hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

		this.hikariConfig = hikariConfig;
	}

	public Sql(final HikariConfig config) {
		this.hikariConfig = config;
	}


	@Override
	public void attach() {

		assert !isConnected;

		this.dataSource = new HikariDataSource(hikariConfig);

		isConnected = true;
	}

	@Override
	public void detach() {

		assert isConnected;

		this.dataSource = null;

		isConnected = false;
	}


	@Override
	public boolean isConnected() {
		return isConnected;
	}


	public HikariDataSource getDataSource() {
		return dataSource;
	}


	public static Sql fromOrMake(final File configFile, final Gson gson) {
		return new Sql(SqlConfig.fromOrMake(configFile, gson));
	}


	class Table<T> {

		public Table(final Class<T> clazz) {
			// TODO: Cache non-transient fields
		}


	}


}
