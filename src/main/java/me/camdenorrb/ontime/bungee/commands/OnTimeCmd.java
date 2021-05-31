package me.camdenorrb.ontime.bungee.commands;

import me.camdenorrb.ontime.OnTimeBungee;
import me.camdenorrb.ontime.bungee.modules.TimeModule;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.md_5.bungee.api.ChatColor.*;


public final class OnTimeCmd extends Command implements TabExecutor {

	private final OnTimeBungee plugin;

	private static final TextComponent usage = new TextComponent(GREEN + "/onTime <Target> (Server)");


	public OnTimeCmd(final OnTimeBungee plugin) {
		super("ontime", "ontime.use");
		this.plugin = plugin;
	}


	@Override
	public void execute(CommandSender sender, String[] args) {

		if (args.length == 0) {
			sender.sendMessage(new TextComponent(RED + "Please enter a target - \n"), usage);
			return;
		}

		final UUID targetUUID = plugin.getNameModule().getUUIDForName(args[0]);
		String serverName = null;
		if(args.length >= 2)
			serverName = args[1];

		if (targetUUID == null) {
			sender.sendMessage(new TextComponent(RED + "Please enter an existing target name - \n"), usage);
			return;
		}

		if (serverName != null && plugin.getProxy().getServerInfo(serverName) == null) {
			sender.sendMessage(new TextComponent(RED + "Please enter a valid server name - \n"), usage);
			return;
		}

		String finalServerName = serverName;
		plugin.getThreadPool().execute(() -> {

			final TimeModule timeModule = plugin.getTimeModule();

			final long day   = timeModule.getPlayTime(targetUUID, finalServerName, TimeModule.Timespan.TODAY);
			final long week  = timeModule.getPlayTime(targetUUID, finalServerName, TimeModule.Timespan.THIS_WEEK);
			final long month = timeModule.getPlayTime(targetUUID, finalServerName, TimeModule.Timespan.THIS_MONTH);
			final long all   = timeModule.getPlayTime(targetUUID, finalServerName, TimeModule.Timespan.ALL);

			sender.sendMessage(
					new TextComponent(AQUA + "[" + args[0] + "]\n"),
				new TextComponent(BLUE + "Server: " + AQUA + (finalServerName == null ? "All" : finalServerName) + "\n"),
				new TextComponent(BLUE + "Rank: " + AQUA + timeModule.getOverallRank(targetUUID, finalServerName) + "\n"),
				new TextComponent(DARK_GREEN + "Today:       " + GREEN + formatMillis(day)   + '\n'),
				new TextComponent(DARK_GREEN + "This Week:   " + GREEN + formatMillis(week)  + '\n'),
				new TextComponent(DARK_GREEN + "This Month:  " + GREEN + formatMillis(month) + '\n'),
				new TextComponent(DARK_GREEN + "All time:    " + GREEN + formatMillis(all))
			);
		});
	}


	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		switch (args.length) {
			case 1:
				//List<String> vanished = new ArrayList<String>(((VanishPlugin)Bukkit.getPluginManager().getPlugin("VanishNoPacket")).getManager().getVanishedPlayers());
				List<String> allPlayers = plugin.getProxy().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList());
				//allPlayers.removeAll(vanished);
				return allPlayers;
			case 2:
				return plugin.getProxy().getServers().keySet();

			default:
				return Collections.emptyList();
		}
	}


	private String formatMillis(final long millis) {

		long seconds = (millis / 1000) % 60;
		long minutes = (millis / 60000) % 60;
		long hours   = (millis / 3600000) % 24;
		long days    = (millis / 86400000);

		return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
	}

}
