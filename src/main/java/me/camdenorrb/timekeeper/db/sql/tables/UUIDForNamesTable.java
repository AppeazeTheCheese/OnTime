package me.camdenorrb.timekeeper.db.sql.tables;

import me.camdenorrb.timekeeper.anno.NotNull;
import me.camdenorrb.timekeeper.db.sql.base.SqlTable;
import sun.misc.Unsafe;

import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


@Target(playerUUID)
public final class UUIDForNamesTable implements SqlTable {

	private final static String TABLE_NAME = "UUIDForNames";


	@NotNull
	private final UUID playerUUID;

	@NotNull
	@ForeignKey
	private final String playerName;


	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

}
