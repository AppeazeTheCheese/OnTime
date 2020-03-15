package me.camdenorrb.timekeeper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.camdenorrb.timekeeper.bungee.commands.OnTimeCmd;
import me.camdenorrb.timekeeper.bungee.modules.NameModule;
import me.camdenorrb.timekeeper.bungee.modules.TimeModule;
import me.camdenorrb.timekeeper.config.SqlConfig;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public final class TimeKeeperBungee extends Plugin {

	private HikariDataSource hikariDataSource;


	private final TimeModule timeModule = new TimeModule(this);

	private final NameModule nameModule = new NameModule(this);

	private final Executor threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private final Gson gson = new GsonBuilder().disableHtmlEscaping().enableComplexMapKeySerialization().setPrettyPrinting().create();


	@Override
	public void onLoad() {

		final SqlConfig mysqlConfig = SqlConfig.fromOrMake(new File(getDataFolder(), "mysqlConfig.json"), gson);

		final HikariConfig hikariConfig = new HikariConfig();

		System.out.println(mysqlConfig.getBase());

		hikariConfig.setJdbcUrl("jdbc:mysql://" + mysqlConfig.getHost() + ':' + mysqlConfig.getPort() + '/' + mysqlConfig.getBase() + "?useSSL=false");
		hikariConfig.setUsername(mysqlConfig.getUser());
		hikariConfig.setPassword(mysqlConfig.getPass());
		hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
		hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
		hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

		hikariDataSource = new HikariDataSource(hikariConfig);
	}

	@Override
	public void onEnable() {

		nameModule.enable();
		timeModule.enable();

		getProxy().getPluginManager().registerCommand(this, new OnTimeCmd(this));
	}

	@Override
	public void onDisable() {

		nameModule.disable();
		timeModule.disable();

		hikariDataSource.close(); // Needs to close last
	}


	public NameModule getNameModule() {
		return nameModule;
	}

	public TimeModule getTimeModule() {
		return timeModule;
	}

	public Executor getThreadPool() {
		return threadPool;
	}

	public HikariDataSource getHikariDataSource() {
		return hikariDataSource;
	}

}
