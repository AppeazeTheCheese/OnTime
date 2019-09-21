package me.camdenorrb.timekeeper.module;

import com.sxtanna.database.task.builder.Create;
import com.sxtanna.database.task.builder.Delete;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


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

		final long joinTime = serverJoinTime.get(event.getPlayer().getUniqueId()).get("Bungee");

		plugin.getKuery().execute((it) -> {
			Delete
			Select.from(Session.class).equals("uuid", )
		});
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
		event.getPlayer();
	}


	private void loadTimeData() {

		plugin.getKuery().execute((it) -> {
			it.execute(Create.from(Session.class));
		});

	}

	private void saveTimeData() {

	}


	// All time = All this combined
	public enum Interval {
		DAY, WEEK, MONTH, OTHER
	}

	public final class Session implements SqlObject {

		private final long joinTime, quitTime;


		public Session(final long joinTime, final long quitTime) {
			this.joinTime = joinTime;
			this.quitTime = quitTime;
		}


		public long getJoinTime() {
			return joinTime;
		}

		public long getQuitTime() {
			return quitTime;
		}

	}

}
