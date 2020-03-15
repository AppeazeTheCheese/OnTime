package me.camdenorrb.timekeeper.spigot.modules;

import me.camdenorrb.jcommons.base.ModuleBase;
import me.camdenorrb.timekeeper.TimeKeeperSpigot;
import me.camdenorrb.timekeeper.utils.SqlUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.PreparedStatement;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static me.camdenorrb.jcommons.utils.TryUtils.attemptOrPrintErr;


public final class NameModule implements ModuleBase, Listener {

	private boolean isEnabled;

	private final TimeKeeperSpigot plugin;

	private static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS UUIDForName (uuid CHAR(36) PRIMARY KEY NOT NULL, name VARCHAR(255) NOT NULL)";
	private static final String INSERT_SQL = "INSERT INTO UUIDForName (uuid, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=name";
	private static final String SELECT_SQL = "SELECT * FROM UUIDForName WHERE name=?";


	public NameModule(TimeKeeperSpigot plugin) {
		this.plugin = plugin;
	}


	@Override
	public void enable() {

		assert !isEnabled;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);

		SqlUtils.useStatement(plugin.getHikariDataSource(), CREATE_SQL, PreparedStatement::execute);

		// Save online players
		plugin.getThreadPool().execute(() ->
			plugin.getServer().getOnlinePlayers().forEach((player) ->
				SqlUtils.useStatement(plugin.getHikariDataSource(), INSERT_SQL, (statement) -> {
					statement.setString(1, player.getUniqueId().toString());
					statement.setString(2, player.getName());
					statement.execute();
				})
			)
		);


		isEnabled = true;
	}

	@Override
	public void disable() {

		assert isEnabled;

		HandlerList.unregisterAll(this);
		/*
		plugin.getKuery().execute((task) -> {
			plugin.getProxy().getPlayers().forEach((it) -> {
				final UUIDForName uuidForName = new UUIDForName(it.getUniqueId(), it.getName());
				task.execute(Insert.into(UUIDForName.class).onDupeUpdate("uuid"), uuidForName);
			});
		});
		*/

		isEnabled = false;
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}


	// Should call from another thread
	public UUID getUUIDForName(final String name) {

		final AtomicReference<UUID> uuid = new AtomicReference<>();

		SqlUtils.useStatement(plugin.getHikariDataSource(), SELECT_SQL, (statement) -> {

			statement.setString(1, name);

			attemptOrPrintErr(statement::executeQuery, (resultSet) -> {
				if (resultSet.next()) {
					uuid.set(UUID.fromString(resultSet.getString("uuid")));
				}
			});
		});

		return uuid.get();
	}


	@EventHandler
	public void onJoin(final PlayerJoinEvent event) {

		final Player player = event.getPlayer();

		plugin.getThreadPool().execute(() ->
			SqlUtils.useStatement(plugin.getHikariDataSource(), INSERT_SQL, (statement) -> {
				statement.setString(1, player.getUniqueId().toString());
				statement.setString(2, player.getName());
				statement.execute();
			})
		);
	}


	public final class UUIDForName {

		private final UUID uuid;

		private final String name;


		public UUIDForName(final UUID uuid, final String name) {
			this.name = name;
			this.uuid = uuid;
		}


		public String getName() {
			return name;
		}

		public UUID getUuid() {
			return uuid;
		}

	}

}
