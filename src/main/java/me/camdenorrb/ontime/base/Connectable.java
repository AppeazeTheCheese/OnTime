package me.camdenorrb.ontime.base;

public interface Connectable {

	// Connect
	void attach();

	// Disconnect
	void detach();


	boolean isConnected();

}
