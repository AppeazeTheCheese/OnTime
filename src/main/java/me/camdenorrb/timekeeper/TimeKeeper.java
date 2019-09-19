package me.camdenorrb.timekeeper;

import me.camdenorrb.timekeeper.module.TimeModule;
import net.md_5.bungee.api.plugin.Plugin;


public final class TimeKeeper extends Plugin {

	private TimeModule timeModule = new TimeModule();


	@Override
	public void onLoad() {

	}

	@Override
	public void onEnable() {
		timeModule.enable();
	}

	@Override
	public void onDisable() {
		timeModule.disable();
	}

}
