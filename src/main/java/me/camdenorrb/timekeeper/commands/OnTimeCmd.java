package me.camdenorrb.timekeeper.commands;

import me.camdenorrb.timekeeper.TimeKeeper;
import me.camdenorrb.timekeeper.modules.TimeModule;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.md_5.bungee.api.ChatColor.*;


public final class OnTimeCmd extends Command implements TabExecutor {

	private final TimeKeeper plugin;

	private static final TextComponent usage = new TextComponent(GOLD + "/onTime <Target> (Server)");


	public OnTimeCmd(final TimeKeeper plugin) {
		super("ontime", "timekeeper.ontime");
		this.plugin = plugin;
	}


	@Override
	public void execute(CommandSender sender, String[] args) {

		if (args.length == 0) {
			sender.sendMessage(new TextComponent(AQUA + "Please enter a target - "), usage);
			return;
		}

		final UUID target = plugin.getNameModule().getUUIDForName(args[0]);
		final String serverName = args.length > 1 ? args[1] : "Bungee";

		if (target == null) {
			sender.sendMessage(new TextComponent(AQUA + "Please enter an existing target name - "), usage);
			return;
		}

		if (!serverName.equals("Bungee") && plugin.getProxy().getServerInfo(serverName) == null) {
			sender.sendMessage(new TextComponent(AQUA + "Please enter a valid server name - "), usage);
			return;
		}

		plugin.getThreadPool().execute(() -> {

			final TimeModule timeModule = plugin.getTimeModule();

			final long day = timeModule.getPlayTime(target, serverName, TimeModule.Timespan.TODAY);
			final long week = timeModule.getPlayTime(target, serverName, TimeModule.Timespan.THIS_WEEK);
			final long month = timeModule.getPlayTime(target, serverName, TimeModule.Timespan.THIS_MONTH);
			final long all = timeModule.getPlayTime(target, serverName, TimeModule.Timespan.ALL);

			sender.sendMessage(
				new TextComponent("\n"),
				new TextComponent(GREEN + "Today: "      + YELLOW + formatMillis(day)   + '\n'),
				new TextComponent(GREEN + "This Week: "  + YELLOW + formatMillis(week)  + '\n'),
				new TextComponent(GREEN + "This Month: " + YELLOW + formatMillis(month) + '\n'),
				new TextComponent(GREEN + "All time: "   + YELLOW + formatMillis(all))
			);
		});
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {

		switch (args.length) {

			case 1:
				return plugin.getProxy().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList());
			case 2:
				return plugin.getProxy().getServers().keySet();

			default: return new ArrayList<>();
		}
	}


	private String formatMillis(final long millis) {

		long seconds = (millis / 1000) % 60;
		long minutes = (millis / 60000) % 60;
		long hours   = (millis / 3600000) % 24;
		long days    = (millis / 86400000) % 24;

		return days + " days, " + hours + " hours, " + minutes + " mins and " + seconds + " seconds.";
	}

}
