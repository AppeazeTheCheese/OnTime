package me.camdenorrb.timekeeper.base.tryblock;

/**
 * Represents a try with resources block
 */
@FunctionalInterface
public interface TryCloseBlock<T extends AutoCloseable> {

	/**
	 * Attempts to run the following method
	 *
	 * @throws Exception In order to be catched elsewhere
	 */
	void attempt(T resource) throws Exception;

}
