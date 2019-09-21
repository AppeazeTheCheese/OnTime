package me.camdenorrb.timekeeper;

import com.sxtanna.database.Kuery;
import me.camdenorrb.timekeeper.module.TimeModule;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;


public final class TimeKeeper extends Plugin {

	private final TimeModule timeModule = new TimeModule(this);

	private final Kuery kuery = Kuery.get(new File(getDataFolder(), "mysqlConfig.json"));


	@Override
	public void onEnable() {
		kuery.enable();
		timeModule.enable();
	}

	@Override
	public void onDisable() {
		kuery.disable();
		timeModule.disable();
	}


	public TimeModule getTimeModule() {
		return timeModule;
	}

	public Kuery getKuery() {
		return kuery;
	}

}
