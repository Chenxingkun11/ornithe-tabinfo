package dev.xingkun.ornithetabinfo.client;

import java.lang.ref.WeakReference;

import net.minecraft.client.network.handler.ClientPlayNetworkHandler;

import dev.xingkun.ornithetabinfo.data.TabInfoSnapshot;

public final class TabInfoClientState {

	private static final long STALE_AFTER_NANOS = 3_000_000_000L;

	private static volatile Entry latest;

	private TabInfoClientState() {
	}

	public static void accept(ClientPlayNetworkHandler handler, TabInfoSnapshot snapshot) {
		latest = new Entry(handler, snapshot, System.nanoTime());
	}

	/**
	 * @return the snapshot for this connection, or {@code null} if none arrived on it
	 *  or the last one is older than 3 seconds
	 */
	public static TabInfoSnapshot get(ClientPlayNetworkHandler handler) {
		Entry entry = latest;

		if (entry == null || handler == null || entry.handler.get() != handler) {
			return null;
		}
		if (System.nanoTime() - entry.receivedAt > STALE_AFTER_NANOS) {
			return null;
		}

		return entry.snapshot;
	}

	public static void clear() {
		latest = null;
	}

	private static final class Entry {

		final WeakReference<ClientPlayNetworkHandler> handler;
		final TabInfoSnapshot snapshot;
		final long receivedAt;

		Entry(ClientPlayNetworkHandler handler, TabInfoSnapshot snapshot, long receivedAt) {
			this.handler = new WeakReference<>(handler);
			this.snapshot = snapshot;
			this.receivedAt = receivedAt;
		}
	}
}
