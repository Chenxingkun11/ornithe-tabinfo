package dev.xingkun.ornithetabinfo.network;

import net.ornithemc.osl.core.api.util.NamespacedIdentifier;
import net.ornithemc.osl.networking.api.ChannelIdentifiers;
import net.ornithemc.osl.networking.api.ChannelRegistry;
import net.ornithemc.osl.networking.api.PacketBuffer;

import dev.xingkun.ornithetabinfo.OrnitheTabInfo;
import dev.xingkun.ornithetabinfo.data.TabInfoSnapshot;

/**
 * Wire format of the {@code ornithetabinfo:stats} channel.
 *
 * <p>Version 1 layout, in order:
 * {@code byte protocolVersion, int serverTick, int autosaveRemainingGt,
 * float previousSaveGt (NaN before the first save), float tps, float mspt}.
 */
public final class TabInfoProtocol {

	/** Client-bound only: the server pushes snapshots and never listens on this channel. */
	public static final NamespacedIdentifier CHANNEL = ChannelRegistry.register(ChannelIdentifiers.from(OrnitheTabInfo.MOD_ID, "stats"), true, false);
	public static final byte VERSION = 1;

	private TabInfoProtocol() {
	}

	public static void write(PacketBuffer buffer, TabInfoSnapshot snapshot) {
		buffer.writeByte(VERSION);
		buffer.writeInt(snapshot.serverTick);
		buffer.writeInt(snapshot.autosaveRemainingGt);
		buffer.writeFloat(snapshot.previousSaveGt);
		buffer.writeFloat(snapshot.tps);
		buffer.writeFloat(snapshot.mspt);
	}

	/**
	 * @return the decoded snapshot, or {@code null} if the packet uses a protocol version this build does not understand
	 */
	public static TabInfoSnapshot read(PacketBuffer buffer) {
		byte version = buffer.readByte();
		if (version != VERSION) {
			return null;
		}

		int serverTick = buffer.readInt();
		int autosaveRemainingGt = buffer.readInt();
		float previousSaveGt = buffer.readFloat();
		float tps = buffer.readFloat();
		float mspt = buffer.readFloat();

		return new TabInfoSnapshot(serverTick, autosaveRemainingGt, previousSaveGt, tps, mspt);
	}
}
