package me.camdenorrb.timekeeper.utils;

import com.zaxxer.hikari.HikariDataSource;
import me.camdenorrb.jcommons.base.tryblock.TryCloseBlock;

import java.sql.PreparedStatement;

import static me.camdenorrb.timekeeper.utils.TryUtils.attemptOrPrintErr;


public final class SqlUtils {

	private SqlUtils() {}


	public static void useStatement(final HikariDataSource dataSource, final String statement, final TryCloseBlock<PreparedStatement> block) {
		attemptOrPrintErr(dataSource::getConnection, (connection) ->
			attemptOrPrintErr(() -> connection.prepareStatement(statement), block)
		);
	}

}
