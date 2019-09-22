package me.camdenorrb.timekeeper.module;

import com.sxtanna.database.task.builder.Create;
import com.sxtanna.database.task.builder.Insert;
import com.sxtanna.database.task.builder.Select;
import com.sxtanna.database.type.base.SqlObject;
import me.camdenorrb.timekeeper.TimeKeeper;
import me.camdenorrb.timekeeper.module.base.ModuleBase;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Handles the keeping of time
 */
public final class TimeModule implements ModuleBase, Listener {

	private boolean isEnabled = false;

	private final TimeKeeper plugin;

	// UUID -> (Server Name -> Join Time in milliseconds)
	// "Bungee" will be the server name for when they join Bungee
	private final Map<UUID, Map<String, Long>> serverJoinTime = new HashMap<>();


	public TimeModule(final TimeKeeper plugin) {
		this.plugin = plugin;
	}


	@Override
	public void enable() {

		assert !isEnabled;

		loadTimeData();
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
	protected final void onBungeeJoin(final PostLoginEvent event) {

		final UUID uuid = event.getPlayer().getUniqueId();

		serverJoinTime
			.computeIfAbsent(uuid, (it) -> new HashMap<>())
			.put("Bungee", System.currentTimeMillis());
	}

	@EventHandler
	protected final void onBungeeQuit(final PlayerDisconnectEvent event) {

		final UUID uuid = event.getPlayer().getUniqueId();
		final long joinTime = serverJoinTime.get(event.getPlayer().getUniqueId()).get("Bungee");

		plugin.getKuery().execute((it) ->
			it.execute(Insert.into(Session.class), new Session(uuid, "Bungee", joinTime, System.currentTimeMillis()))
		);
	}

	@EventHandler
	protected final void onServerJoin(final ServerConnectEvent event) {

		final UUID uuid = event.getPlayer().getUniqueId();

		serverJoinTime
			.computeIfAbsent(uuid, (it) -> new HashMap<>())
			.put(event.getTarget().getName(), System.currentTimeMillis());
	}

	@EventHandler
	protected final void onServerQuit(final ServerDisconnectEvent event) {

		final UUID uuid = event.getPlayer().getUniqueId();
		final String serverName = event.getTarget().getName();
		final long joinTime = serverJoinTime.get(event.getPlayer().getUniqueId()).get("Bungee");

		plugin.getKuery().execute((it) ->
			it.execute(Insert.into(Session.class), new Session(uuid, serverName, joinTime, System.currentTimeMillis()))
		);
	}

	public long getPlayTime(final UUID targetUUID, final Interval interval) {

		final AtomicLong onlineTime = new AtomicLong();

		plugin.getKuery().execute((task) -> {

			final long searchTime = System.currentTimeMillis() - interval.getMilli();

			task.execute(Select.from(Session.class).equals("playerUUID", targetUUID).greater("quitTime", searchTime, true), session ->
				onlineTime.addAndGet(session.quitTime - session.joinTime)
			);

		});

		return onlineTime.get();
	}


	private void loadTimeData() {
		plugin.getKuery().execute((it) -> it.execute(Create.from(Session.class)));
	}

	private void saveTimeData() {
		plugin.getKuery().execute((task) ->
			serverJoinTime.forEach((uuid, entries) ->
				entries.forEach((serverName, joinTime) ->
					task.execute(Insert.into(Session.class), new Session(uuid, serverName, joinTime, System.currentTimeMillis()))
				)
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
		Month(DAY.milli * YearMonth.now().lengthOfMonth()),
		ALL(Long.MAX_VALUE);


		private final long milli;

		Interval(final long milli) {
			this.milli = milli;
		}

		public long getMilli() {
			return milli;
		}

	}

	public final class Session implements SqlObject {

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
