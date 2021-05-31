package me.camdenorrb.ontime.spigot.commands;

import me.camdenorrb.ontime.OnTimeSpigot;

import me.camdenorrb.ontime.spigot.modules.TimeModule;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.kitteh.vanish.VanishPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.md_5.bungee.api.ChatColor.*;


public final class OnTimeCmd implements TabExecutor {

	private final OnTimeSpigot plugin;

	private static final String usage = GREEN + "/onTime <Target>";


	public OnTimeCmd(final OnTimeSpigot plugin) {
		this.plugin = plugin;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length == 0) {
			sender.sendMessage(RED + "Please enter a target - " + usage);
			return true;
		}

		final UUID targetUUID = plugin.getNameModule().getUUIDForName(args[0]);
		final String serverName = plugin.getServer().getName();

		if (targetUUID == null) {
			sender.sendMessage(RED + "Please enter an existing target name - " + usage);
			return true;
		}

		plugin.getThreadPool().execute(() -> {

			final TimeModule timeModule = plugin.getTimeModule();

			final long day   = timeModule.getPlayTime(targetUUID, serverName, TimeModule.Timespan.TODAY);
			final long week  = timeModule.getPlayTime(targetUUID, serverName, TimeModule.Timespan.THIS_WEEK);
			final long month = timeModule.getPlayTime(targetUUID, serverName, TimeModule.Timespan.THIS_MONTH);
			final long all   = timeModule.getPlayTime(targetUUID, serverName, TimeModule.Timespan.ALL);
			final long rank = timeModule.getOverallRank(targetUUID, serverName);

			sender.sendMessage(
				new String[] {
					AQUA + "[" + args[0] + "]\n",
						YELLOW + "Rank         " + rank,
					DARK_GREEN + "Today:       " + GREEN + formatMillis(day)   + '\n',
					DARK_GREEN + "This Week:   " + GREEN + formatMillis(week)  + '\n',
					DARK_GREEN + "This Month:  " + GREEN + formatMillis(month) + '\n',
					DARK_GREEN + "All time:    " + GREEN + formatMillis(all)   + '\n',
				}
			);
		});

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) {
			List<String> vanishedPlayers = new ArrayList<String>(((VanishPlugin)Bukkit.getPluginManager().getPlugin("VanishNoPacket")).getManager().getVanishedPlayers());
			List<String> allPlayers = plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
			
			allPlayers.removeAll(vanishedPlayers);
			return allPlayers;
		}

		return Collections.emptyList();
	}


	private String formatMillis(final long millis) {

		long seconds = (millis / 1000)     % 60;
		long minutes = (millis / 60000)    % 60;
		long hours   = (millis / 3600000)  % 24;
		long days    = (millis / 86400000);

		return days + " days, " + hours + " hours, " + minutes + " mins and " + seconds + " seconds.";
	}

}
