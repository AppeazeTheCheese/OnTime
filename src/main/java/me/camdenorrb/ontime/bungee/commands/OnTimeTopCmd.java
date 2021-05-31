package me.camdenorrb.ontime.bungee.commands;

import me.camdenorrb.ontime.OnTimeBungee;
import me.camdenorrb.ontime.bungee.modules.NameModule;
import me.camdenorrb.ontime.bungee.modules.TimeModule;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.md_5.bungee.api.ChatColor.*;


public final class OnTimeTopCmd extends Command implements TabExecutor {

	private final OnTimeBungee plugin;

	private static final TextComponent usage = new TextComponent(GREEN + "/onTimeTop <page> (server)\n" +
																GREEN + "/onTimeTop (server)");

	public OnTimeTopCmd(final OnTimeBungee plugin) {
		super("ontimetop", "ontimetop.use");
		this.plugin = plugin;
	}


	@Override
	public void execute(CommandSender sender, String[] args) {
		String serverName = null;

		final AtomicInteger page = new AtomicInteger(1);
		if(args.length > 1){
			serverName = args[0];

			try{
				page.set(Integer.parseInt(args[1]));
			}catch (Exception e){}
			if(page.get() <= 1)
				page.set(1);
		}
		else if(args.length > 0){
			try{
				page.set(Integer.parseInt(args[0]));
			}catch (Exception e){
				serverName = args[0];
			}
			if(page.get() <= 1)
				page.set(1);
		}


		if (serverName != null && plugin.getProxy().getServerInfo(serverName) == null) {
			sender.sendMessage(new TextComponent(RED + "Server \"" + UNDERLINE + args[0] + RESET + "" + RED + "\" not found. - \n"), usage);
			return;
		}

		String finalServerName = serverName;
		plugin.getThreadPool().execute(() -> {

			final TimeModule timeModule = plugin.getTimeModule();
			final NameModule nameModule = plugin.getNameModule();
			final int pageSize = 10;

			double distinctCount = timeModule.getDistinctCountByServer(finalServerName);
			int pages = 0;
			if(distinctCount > 0) {
				pages = (int) Math.ceil(distinctCount / pageSize);
				if(page.get() > pages)
					page.set(pages);
			}

			final LinkedHashMap<UUID, Long> top = timeModule.getTopPlayers(pageSize, (page.get() - 1) * 10, finalServerName);
			List<BaseComponent> msg = new ArrayList<>();


			msg.add(new TextComponent(YELLOW +"-----------------Page " + page.get() + "/" + pages + "-------------------"));

			int i = ((page.get() - 1) * 10) + 1;
			for(Map.Entry<UUID, Long> item : top.entrySet()){
				msg.add(new TextComponent("\n" + YELLOW + i++ + ". " + AQUA + "[" + nameModule.getNameForUUID(item.getKey()) + "] " + GREEN + formatMillis(item.getValue())));
			}
			sender.sendMessage(msg.toArray(new BaseComponent[]{}));
		});
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if(args.length < 3){
			return plugin.getProxy().getServers().keySet();
		}
		return new ArrayList<>();
	}

	private String formatMillis(final long millis) {

		long seconds = (millis / 1000) % 60;
		long minutes = (millis / 60000) % 60;
		long hours   = (millis / 3600000) % 24;
		long days    = Double.valueOf(Math.floor(millis / (double) 86400000)).longValue();

		return days + " days, " + hours + " hours, " + minutes + " mins and " + seconds + " seconds.";
	}

}
