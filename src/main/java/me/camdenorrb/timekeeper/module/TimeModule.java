package me.camdenorrb.timekeeper.module;

import com.sxtanna.database.Kuery;
import com.sxtanna.database.config.KueryConfig;
import me.camdenorrb.timekeeper.TimeKeeper;
import me.camdenorrb.timekeeper.module.base.ModuleBase;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Handles the keeping of time
 */
public final class TimeModule implements ModuleBase, Listener {

	private boolean isEnabled = false;

	private final File timeFolder;

	private final TimeKeeper plugin;

	// UUID -> (Interval -> Time in hours)
	//private final Map<UUID, Map<Interval, Long>> serverTime = new HashMap<>();
	//private final Map<UUID, Map<Interval, Long>> bungeeTime = new HashMap<>();


	public TimeModule(final TimeKeeper plugin) {
		this.plugin = plugin;
		this.timeFolder = new File(plugin.getDataFolder(), "PlayTime");
	}


	@Override
	public void enable() {

		assert !isEnabled;

		final File mysqlConfigFile = new File(plugin.getDataFolder(), "mysqlConfig.json");

		if (!mysqlConfigFile.exists()) {
			mysqlConfigFile
		}

		KueryConfig.Companion.getDEFAULT()

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
	protected final void onBungeeJoin(final LoginEvent event) {
		event.
	}

	@EventHandler
	protected final void onBungeeQuit(final PlayerDisconnectEvent event) {

	}

	@EventHandler
	protected final void onServerJoin(final ServerConnectEvent event) {

	}

	@EventHandler
	protected final void onServerQuit(final ServerDisconnectEvent event) {

	}


	// All time = All this combined
	public enum Interval {
		DAY, WEEK, MONTH, OTHER
	}

	public class Session {

		private final long joinTime, quitTime;


		public Session(long joinTime, long quitTime) {
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


	private void loadTimeData() {

	}

}
