package me.camdenorrb.timekeeper.base;

public interface Connectable {

	// Connect
	void attach();

	// Disconnect
	void detach();


	boolean isConnected();

}
