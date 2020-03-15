package me.camdenorrb.timekeeper.spigot.commands;

import me.camdenorrb.timekeeper.TimeKeeperSpigot;
import me.camdenorrb.timekeeper.spigot.modules.TimeModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.md_5.bungee.api.ChatColor.*;


public final class OnTimeCmd implements TabExecutor {

	private final TimeKeeperSpigot plugin;

	private static final String usage = GOLD + "/onTime <Target>";


	public OnTimeCmd(final TimeKeeperSpigot plugin) {
		this.plugin = plugin;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length == 0) {
			sender.sendMessage(AQUA + "Please enter a target - " + usage);
			return true;
		}

		final UUID target = plugin.getNameModule().getUUIDForName(args[0]);
		final String serverName = plugin.getServer().getName();

		if (target == null) {
			sender.sendMessage(AQUA + "Please enter an existing target name - " + usage);
			return true;
		}

		plugin.getThreadPool().execute(() -> {

			final TimeModule timeModule = plugin.getTimeModule();

			final long day   = timeModule.getPlayTime(target, serverName, TimeModule.Timespan.TODAY);
			final long week  = timeModule.getPlayTime(target, serverName, TimeModule.Timespan.THIS_WEEK);
			final long month = timeModule.getPlayTime(target, serverName, TimeModule.Timespan.THIS_MONTH);
			final long all   = timeModule.getPlayTime(target, serverName, TimeModule.Timespan.ALL);

			sender.sendMessage(
				new String[] {
					"\n",
					"Today: "              + YELLOW + formatMillis(day)   + '\n',
					GREEN + "This Week: "  + YELLOW + formatMillis(week)  + '\n',
					GREEN + "This Month: " + YELLOW + formatMillis(month) + '\n',
					GREEN + "All time: "   + YELLOW + formatMillis(all)
				}
			);
		});

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		if (args.length == 1) {
			return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
		}

		return Collections.emptyList();
	}


	private String formatMillis(final long millis) {

		long seconds = (millis / 1000)     % 60;
		long minutes = (millis / 60000)    % 60;
		long hours   = (millis / 3600000)  % 24;
		long days    = (millis / 86400000) % 24;

		return days + " days, " + hours + " hours, " + minutes + " mins and " + seconds + " seconds.";
	}

}
