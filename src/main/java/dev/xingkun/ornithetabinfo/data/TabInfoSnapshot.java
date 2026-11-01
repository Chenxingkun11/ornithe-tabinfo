package dev.xingkun.ornithetabinfo.data;

/**
 * One authoritative set of server metrics, as sent from the server to the client.
 */
public final class TabInfoSnapshot {

	/** Server tick counter at the moment the snapshot was taken. */
	public final int serverTick;
	/** Game ticks until the next vanilla autosave (1..900). */
	public final int autosaveRemainingGt;
	/** Real duration of the previous autosave converted to game ticks (50 ms each), or NaN before the first one. */
	public final float previousSaveGt;
	/** Measured ticks per second over the recent tick window. */
	public final float tps;
	/** Average milliseconds spent per tick over the recent tick window. */
	public final float mspt;

	public TabInfoSnapshot(int serverTick, int autosaveRemainingGt, float previousSaveGt, float tps, float mspt) {
		this.serverTick = serverTick;
		this.autosaveRemainingGt = autosaveRemainingGt;
		this.previousSaveGt = previousSaveGt;
		this.tps = tps;
		this.mspt = mspt;
	}

	public boolean hasPreviousSave() {
		return !Float.isNaN(this.previousSaveGt);
	}
}
