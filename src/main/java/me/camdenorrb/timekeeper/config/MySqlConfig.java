package me.camdenorrb.timekeeper.config;

import com.google.gson.Gson;
import me.camdenorrb.timekeeper.utils.GsonUtils;

import java.io.File;


public final class MySqlConfig {

	private final int port;

	private final String user, pass, host, database;


	public MySqlConfig() {
		this.user = "username";
		this.pass = "password";
		this.host = "127.0.0.1";
		this.port = 3306;
		this.database = "databaseName";
	}

	public MySqlConfig(final String user, final String pass, final String host, final int port, final String database) {
		this.user = user;
		this.pass = pass;
		this.host = host;
		this.port = port;
		this.database = database;
	}


	public int getPort() {
		return port;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}

	public String getHost() {
		return host;
	}

	public String getDatabase() {
		return database;
	}


	public static MySqlConfig fromOrMake(final File file, final Gson gson) {
		return GsonUtils.fromJsonOrMake(gson, file, MySqlConfig.class, MySqlConfig::new);
	}

}
