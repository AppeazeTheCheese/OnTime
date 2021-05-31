package me.camdenorrb.ontime.spigot.commands;

import me.camdenorrb.ontime.OnTimeSpigot;
import me.camdenorrb.ontime.spigot.modules.NameModule;
import me.camdenorrb.ontime.spigot.modules.TimeModule;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.md_5.bungee.api.ChatColor.AQUA;
import static net.md_5.bungee.api.ChatColor.GREEN;


public final class OnTimeTopCmd implements CommandExecutor {

	private final OnTimeSpigot plugin;

	private static final String usage = GREEN + "/onTimeTop <page>";


	public OnTimeTopCmd(final OnTimeSpigot plugin) {
		this.plugin = plugin;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final String serverName = plugin.getServer().getName();
		final AtomicInteger page = new AtomicInteger(1);
		if(args.length > 0){
			try{
				page.set(Integer.parseInt(args[0]));
			}catch (Exception e){}
			if(page.get() <= 1)
				page.set(1);
		}

		plugin.getThreadPool().execute(() -> {

			final TimeModule timeModule = plugin.getTimeModule();
			final NameModule nameModule = plugin.getNameModule();
			final int pageSize = 10;

			int pages = (int)Math.ceil((double)timeModule.getDistinctCount() / pageSize);
			if(page.get() > pages)
				page.set(pages);

			final LinkedHashMap<UUID, Long> top = timeModule.getTopPlayers(pageSize, (page.get() - 1) * 10, serverName);
			List<String> msg = new ArrayList<>();

			msg.add(ChatColor.YELLOW +"-----------------Page " + page.get() + "/" + pages + "-------------------");

			int i = ((page.get() - 1) * 10) + 1;
			for(Map.Entry<UUID, Long> item : top.entrySet()){
				msg.add(ChatColor.YELLOW + String.valueOf(i++) + ". " + AQUA + "[" + nameModule.getNameForUUID(item.getKey()) + "] " + GREEN + formatMillis(item.getValue()));
			}
			sender.sendMessage(msg.toArray(new String[]{}));
		});

		return true;
	}

	private String formatMillis(final long millis) {

		long seconds = (millis / 1000)     % 60;
		long minutes = (millis / 60000)    % 60;
		long hours   = (millis / 3600000)  % 24;
		long days    = (millis / 86400000);

		return days + " days, " + hours + " hours, " + minutes + " mins and " + seconds + " seconds.";
	}

}
