package me.camdenorrb.timekeeper.module;

import me.camdenorrb.timekeeper.TimeKeeper;
import me.camdenorrb.timekeeper.module.base.ModuleBase;
import me.camdenorrb.timekeeper.utils.SqlUtils;
import me.camdenorrb.timekeeper.utils.TryUtils;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.PreparedStatement;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Handles the keeping of time
 */
public final class TimeModule implements ModuleBase, Listener {

	private boolean isEnabled;

	private final TimeKeeper plugin;

	// UUID -> (Server Name -> Join Time in milliseconds)
	// "Bungee" will be the server name for when they join Bungee
	private final Map<UUID, Map<String, Long>> serverJoinTime = new HashMap<>();


	private static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS Session (playerUUID CHAR(36) NOT NULL, serverName VARCHAR(255) NOT NULL, joinTime BIGINT(100) NOT NULL, quitTime BIGINT(100) NOT NULL)";
	private static final String INSERT_SQL = "INSERT INTO Session (playerUUID, serverName, joinTime, quitTime) VALUES (?, ?, ?, ?)";
	private static final String SELECT_SQL = "SELECT joinTime, quitTime FROM Session WHERE playerUUID=? AND serverName=? AND quitTime > ?";


	public TimeModule(final TimeKeeper plugin) {
		this.plugin = plugin;
	}


	@Override
	public void enable() {

		assert !isEnabled;

		SqlUtils.useStatement(plugin.getHikariDataSource(), CREATE_SQL, PreparedStatement::execute);
		plugin.getProxy().getPluginManager().registerListener(plugin, this);

		isEnabled = true;
	}

	@Override
	public void disable() {

		assert isEnabled;

		saveTimeData();
		serverJoinTime.clear();
		plugin.getProxy().getPluginManager().unregisterListener(this);

		isEnabled = false;
	}



	@Override
	public boolean isEnabled() {
		return isEnabled;
	}


	@EventHandler
	public void onBungeeJoin(final PostLoginEvent event) {

		final UUID uuid = event.getPlayer().getUniqueId();

		serverJoinTime
			.computeIfAbsent(uuid, (it) -> new HashMap<>())
			.put("Bungee", System.currentTimeMillis());
	}

	@EventHandler
	public void onBungeeQuit(final PlayerDisconnectEvent event) {

		final UUID uuid = event.getPlayer().getUniqueId();
		final long joinTime = serverJoinTime.get(event.getPlayer().getUniqueId()).get("Bungee");

		SqlUtils.useStatement(plugin.getHikariDataSource(), INSERT_SQL, (statement) -> {

			statement.setString(1, uuid.toString());
			statement.setString(2, "Bungee");
			statement.setLong(3, joinTime);
			statement.setLong(4, System.currentTimeMillis());

			statement.execute();
		});
	}

	@EventHandler
	public void onServerJoin(final ServerConnectEvent event) {

		final UUID uuid = event.getPlayer().getUniqueId();

		serverJoinTime
			.computeIfAbsent(uuid, (it) -> new HashMap<>())
			.put(event.getTarget().getName(), System.currentTimeMillis());
	}

	@EventHandler
	public void onServerQuit(final ServerDisconnectEvent event) {

		final UUID uuid = event.getPlayer().getUniqueId();
		final String serverName = event.getTarget().getName();
		final long joinTime = serverJoinTime.get(event.getPlayer().getUniqueId()).get("Bungee");

		SqlUtils.useStatement(plugin.getHikariDataSource(), INSERT_SQL, (statement) -> {

			statement.setString(1, uuid.toString());
			statement.setString(2, serverName);
			statement.setLong(3, joinTime);
			statement.setLong(4, System.currentTimeMillis());

			statement.execute();
		});
	}

	// Should call from another thread
	public long getPlayTime(final UUID targetUUID, final String serverName, final Interval interval) {

		final AtomicLong onlineTime = new AtomicLong();

		final long searchTime = System.currentTimeMillis() - interval.getMilli();

		SqlUtils.useStatement(plugin.getHikariDataSource(), SELECT_SQL, (statement) -> {

			statement.setString(1, targetUUID.toString());
			statement.setString(2, serverName);
			statement.setLong(3, searchTime);

			TryUtils.attemptOrPrintErr(statement::executeQuery, (resultSet) -> {
				while (resultSet.next()) {
					final long joinTime = resultSet.getLong(1);
					final long quitTime = resultSet.getLong(2);
					onlineTime.getAndAdd(quitTime - joinTime);
				}
			});
		});

		return TimeUnit.MILLISECONDS.toHours(onlineTime.get());
	}


	private void saveTimeData() {
		serverJoinTime.forEach((uuid, entries) ->
			entries.forEach((serverName, joinTime) ->
				SqlUtils.useStatement(plugin.getHikariDataSource(), INSERT_SQL, (statement) -> {
					statement.setString(1, uuid.toString());
					statement.setString(2, serverName);
					statement.setLong(3, joinTime);
					statement.setLong(4, System.currentTimeMillis());
					statement.execute();
				})
			)
		);
	}


	// All time = All this combined
	public enum Interval {

		SECOND(1000),
		MINUTE(SECOND.milli * 60),
		HOUR(MINUTE.milli * 60),
		DAY(HOUR.milli * 24),
		WEEK(DAY.milli * 7),
		MONTH(DAY.milli * YearMonth.now().lengthOfMonth()),
		ALL(Long.MAX_VALUE);


		private final long milli;

		Interval(final long milli) {
			this.milli = milli;
		}

		public long getMilli() {
			return milli;
		}

	}

	public final class Session {

		private final UUID playerUUID;

		private final String serverName;

		private final long joinTime, quitTime;


		public Session(final UUID playerUUID, final String serverName, final long joinTime, final long quitTime) {
			this.playerUUID = playerUUID;
			this.serverName = serverName;
			this.joinTime = joinTime;
			this.quitTime = quitTime;
		}


		public UUID getPlayerUUID() {
			return playerUUID;
		}

		public String getServerName() {
			return serverName;
		}

		public long getJoinTime() {
			return joinTime;
		}

		public long getQuitTime() {
			return quitTime;
		}

	}

}
