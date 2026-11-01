package dev.xingkun.ornithetabinfo.server;

import dev.xingkun.ornithetabinfo.data.TabInfoSnapshot;

/**
 * Samples tick timing and autosave duration for one server instance.
 *
 * <p>All methods are called from the server thread.
 */
public final class ServerMetricsTracker {

	/** Vanilla 1.7.10 autosaves when {@code ticks % 900 == 0}. */
	public static final int AUTOSAVE_INTERVAL_GT = 900;
	/** A snapshot is pushed to clients once per this many server ticks. */
	public static final int SYNC_INTERVAL_GT = 20;

	private static final int WINDOW_TICKS = 100;
	private static final double NANOS_PER_GT = 50_000_000.0;
	private static final double NANOS_PER_SECOND = 1_000_000_000.0;
	private static final double NANOS_PER_MILLI = 1_000_000.0;

	private final RollingWindow tickDurations = new RollingWindow(WINDOW_TICKS);
	private final RollingWindow tickIntervals = new RollingWindow(WINDOW_TICKS);

	private long tickStart;
	private long previousTickStart;
	private boolean hasPreviousTickStart;

	private long autosaveStart;
	private boolean autosaving;
	private float previousSaveGt = Float.NaN;

	public void onTickStart(long now) {
		if (this.hasPreviousTickStart) {
			this.tickIntervals.add(now - this.previousTickStart);
		}

		this.previousTickStart = now;
		this.hasPreviousTickStart = true;
		this.tickStart = now;
	}

	public void onTickEnd(long now) {
		this.tickDurations.add(now - this.tickStart);
	}

	/**
	 * Drops the interval that will end at the next tick start, so time spent with
	 * the integrated server paused does not count as a slow tick rate.
	 */
	public void skipNextTickInterval() {
		this.hasPreviousTickStart = false;
	}

	public void onAutosaveStart(long now) {
		this.autosaveStart = now;
		this.autosaving = true;
	}

	public void onAutosaveEnd(long now) {
		if (this.autosaving) {
			this.previousSaveGt = (float) ((now - this.autosaveStart) / NANOS_PER_GT);
			this.autosaving = false;
		}
	}

	public static boolean shouldSync(int ticks) {
		return ticks % SYNC_INTERVAL_GT == 0;
	}

	public TabInfoSnapshot snapshot(int ticks) {
		int autosaveRemainingGt = AUTOSAVE_INTERVAL_GT - Math.floorMod(ticks, AUTOSAVE_INTERVAL_GT);
		float tps = (float) (NANOS_PER_SECOND / this.tickIntervals.average());
		float mspt = (float) (this.tickDurations.average() / NANOS_PER_MILLI);

		return new TabInfoSnapshot(ticks, autosaveRemainingGt, this.previousSaveGt, tps, mspt);
	}

	private static final class RollingWindow {

		private final long[] samples;
		private int next;
		private int count;
		private long sum;

		RollingWindow(int size) {
			this.samples = new long[size];
		}

		void add(long sample) {
			this.sum += sample - this.samples[this.next];
			this.samples[this.next] = sample;
			this.next = (this.next + 1) % this.samples.length;
			if (this.count < this.samples.length) {
				this.count++;
			}
		}

		/** @return the mean of the stored samples, or NaN if there are none */
		double average() {
			return this.count == 0 ? Double.NaN : (double) this.sum / this.count;
		}
	}
}
