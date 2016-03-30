package com.github.maxopoly.SkilUp.tracking;

public class TrackableConfig {

	private boolean eventOnlyForRegistered;
	private boolean registerPlaced;
	private boolean registerGrown;
	private boolean registerPushedIn;
	private boolean unregisterPushedOut;
	private boolean unregisterExploded;

	public TrackableConfig(boolean eventOnlyForRegistered,
			boolean registerPlaced, boolean registerGrown,
			boolean registerPushedIn, boolean unregisterPushedOut,
			boolean unregisterExploded) {
		this.eventOnlyForRegistered = eventOnlyForRegistered;
		this.registerPlaced = registerPlaced;
		this.registerGrown = registerGrown;
		this.registerPushedIn = registerPushedIn; 
		this.unregisterExploded = unregisterExploded;
		this.unregisterPushedOut = unregisterPushedOut;
	}

	public boolean callEventOnlyForRegisteredBlocks() {
		return eventOnlyForRegistered;
	}

	public boolean registerPlacedBlocks() {
		return registerPlaced;
	}

	public boolean registerGrownBlocks() {
		return registerGrown;
	}

	public boolean registerPushedInBlocks() {
		return registerPushedIn;
	}

	public boolean unregisterPushedOutBlocks() {
		return unregisterPushedOut;
	}

	public boolean unregisterExplodedBlocks() {
		return unregisterExploded;
	}

}
