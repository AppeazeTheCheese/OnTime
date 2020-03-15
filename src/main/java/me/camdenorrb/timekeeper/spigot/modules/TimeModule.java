package me.camdenorrb.timekeeper.spigot.modules;

import me.camdenorrb.jcommons.base.ModuleBase;
import me.camdenorrb.jcommons.utils.TryUtils;
import me.camdenorrb.timekeeper.TimeKeeperSpigot;
import me.camdenorrb.timekeeper.utils.SqlUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;


/**
 * Handles the keeping of time
 */
public final class TimeModule implements ModuleBase, Listener {

	private boolean isEnabled;

	private final TimeKeeperSpigot plugin;

	// UUID -> (Server Name -> Join Time in milliseconds)
	// "Bungee" will be the server name for when they join Bungee
	private final Map<UUID, Map<String, Long>> serverJoinTime = new HashMap<>();


	// Could use Foreign keys to reduce this a shit ton, UUIDS and ServerNames
	private static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS TimeKeeperSessions(playerUUID CHAR(36) NOT NULL, serverName VARCHAR(255) NOT NULL, joinTime BIGINT(100) NOT NULL, quitTime BIGINT(100) NOT NULL)";
	private static final String INSERT_SQL = "INSERT INTO TimeKeeperSessions(playerUUID, serverName, joinTime, quitTime) VALUES (?, ?, ?, ?)";
	private static final String SELECT_SQL = "SELECT joinTime, quitTime FROM TimeKeeperSessions WHERE playerUUID=? AND serverName=? AND quitTime > ?";


	public TimeModule(final TimeKeeperSpigot plugin) {
		this.plugin = plugin;
	}


	@Override
	public void enable() {

		assert !isEnabled;

		SqlUtils.useStatement(plugin.getHikariDataSource(), CREATE_SQL, PreparedStatement::execute);
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

		isEnabled = true;
	}

	@Override
	public void disable() {

		assert isEnabled;

		saveTimeData();
		serverJoinTime.clear();
		HandlerList.unregisterAll(this);

		isEnabled = false;
	}



	@Override
	public boolean isEnabled() {
		return isEnabled;
	}


	@EventHandler
	public void onJoin(final PlayerLoginEvent event) {

		final UUID uuid = event.getPlayer().getUniqueId();

		serverJoinTime
			.computeIfAbsent(uuid, (it) -> new HashMap<>())
			.put(plugin.getServer().getName(), System.currentTimeMillis());
	}

	@EventHandler
	public void onQuit(final PlayerQuitEvent event) {

		final UUID uuid = event.getPlayer().getUniqueId();
		final Map<String, Long> joinTimes = serverJoinTime.get(uuid);
		final long joinTime = joinTimes.remove(plugin.getServer().getName());

		if (joinTimes.isEmpty()) {
			serverJoinTime.remove(uuid);
		}

		SqlUtils.useStatement(plugin.getHikariDataSource(), INSERT_SQL, (statement) -> {

			statement.setString(1, uuid.toString());
			statement.setString(2, plugin.getServer().getName());
			statement.setLong(3, joinTime);
			statement.setLong(4, System.currentTimeMillis());

			statement.execute();
		});
	}


	// Should call from another thread
	public long getPlayTime(final UUID targetUUID, final String serverName, final Timespan timespan) {

		final AtomicLong onlineTime = new AtomicLong();

		final long searchTime = timespan.getStartEpoch();

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


		final Long joinTime = serverJoinTime.computeIfAbsent(targetUUID, (it) -> new HashMap<>()).get(serverName);

		if (joinTime != null) {
			onlineTime.getAndAdd(System.currentTimeMillis() - joinTime);
		}

		return onlineTime.get();
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
	public enum Timespan {

		TODAY(() -> LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000),

		THIS_WEEK(() -> {

			final LocalDate localDate = LocalDate.now();
			final LocalDateTime today = localDate.atStartOfDay();

			final long startOfMonth = today.minusDays(localDate.getDayOfMonth() - 1).toEpochSecond(ZoneOffset.UTC) * 1000;

			return Long.max(today.minusDays(localDate.getDayOfWeek().getValue() - 1).toEpochSecond(ZoneOffset.UTC) * 1000, startOfMonth);
		}),

		THIS_MONTH(() -> {

			final LocalDate localDate = LocalDate.now();
			final LocalDateTime today = localDate.atStartOfDay();

			return today.minusDays(localDate.getDayOfMonth() - 1).toEpochSecond(ZoneOffset.UTC) * 1000;
		}),

		ALL(() -> 0L);


		private final Supplier<Long> milli;


		Timespan(final Supplier<Long> milli) {
			this.milli = milli;
		}


		public long getStartEpoch() {
			return milli.get();
		}
	}


	/*
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

	}*/

}
