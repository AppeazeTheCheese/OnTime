package me.camdenorrb.timekeeper.utils;

import me.camdenorrb.timekeeper.base.tryblock.SupplierTryBlock;
import me.camdenorrb.timekeeper.base.tryblock.TryBlock;
import me.camdenorrb.timekeeper.base.tryblock.TryCloseBlock;


/**
 * Utilities for making TryBlocks clean
 */
public final class TryUtils {

	private TryUtils() {}


	/**
	 * Attempts to do an action or returns null if failed
	 *
	 * @param block The block to run
	 * @param <T> The type to return
	 *
	 * @return The returned value
	 */
	public static <T> T attemptOrNull(final SupplierTryBlock<T> block) {
		return attemptOrDefault(null, block);
	}

	/**
	 * Attempts to do an action or returns default if failed
	 *
	 * @param defaultValue The default value
	 * @param block The block to run
	 * @param <T> The type to return
	 *
	 * @return The returned value
	 */
	public static <T> T attemptOrDefault(final T defaultValue, final SupplierTryBlock<T> block) {
		try {
			return block.attempt();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return defaultValue;
	}


	/**
	 * Attempts to do an action or prints if failed
	 *
	 * @param block The block to run
	 */
	public static void attemptOrPrintErr(final TryBlock block) {
		try {
			block.attempt();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Attempts to do an action or prints if failed
	 *
	 * @param block The block to run
	 */
	public static <T extends AutoCloseable> void attemptOrPrintErr(final SupplierTryBlock<T> resource, final TryCloseBlock<T> block) {
		try (T resource1 = resource.attempt()){
			block.attempt(resource1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Attempts to do an action or breaks if failed
	 *
	 * @param block The block to run
	 */
	public static void attemptOrBreak(final TryBlock block) {
		try {
			block.attempt();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Attempts to do an action or breaks and closes if failed
	 *
	 * @param block The block to run
	 */
	public static <T extends AutoCloseable> void attemptOrBreak(final SupplierTryBlock<T> resource, final TryCloseBlock<T> block) {
		try (final T resource1 = resource.attempt()) {
			block.attempt(resource1);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}


	/**
	 * Attempts to do an action and return or breaks if failed
	 *
	 * @param block The block to run
	 * @param <T> The type to return
	 */
	public static <T> T attemptOrBreak(final SupplierTryBlock<T> block) {

		try {
			return block.attempt();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(1);

		return null;
	}

}
