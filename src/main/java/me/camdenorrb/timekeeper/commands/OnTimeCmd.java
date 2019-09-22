package me.camdenorrb.timekeeper.commands;

import me.camdenorrb.timekeeper.TimeKeeper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;


public final class OnTimeCmd extends Command {

	private final TimeKeeper plugin;


	public OnTimeCmd(final TimeKeeper plugin) {
		super("ontime", "timekeeper.ontime");
		this.plugin = plugin;
	}


	@Override
	public void execute(CommandSender sender, String[] args) {


		plugin.getThreadPool().execute(() -> {
			plugin.getTimeModule().getPlayTime()
		});
	}

}
