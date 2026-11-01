package dev.xingkun.ornithetabinfo.data;

public final class TabInfoSnapshot {

	public final int serverTick;
	public final int autosaveRemainingGt;
	public final float previousSaveGt;
	public final float tps;
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
