package dev.xingkun.ornithetabinfo.network;

import net.minecraft.network.PacketByteBuf;

import dev.xingkun.ornithetabinfo.OrnitheTabInfo;
import dev.xingkun.ornithetabinfo.data.TabInfoSnapshot;

public final class TabInfoProtocol {

	public static final String CHANNEL = OrnitheTabInfo.MOD_ID + ":stats";
	public static final byte VERSION = 1;

	private TabInfoProtocol() {
	}

	public static void write(PacketByteBuf buffer, TabInfoSnapshot snapshot) {
		buffer.writeByte(VERSION);
		buffer.writeInt(snapshot.serverTick);
		buffer.writeInt(snapshot.autosaveRemainingGt);
		buffer.writeFloat(snapshot.previousSaveGt);
		buffer.writeFloat(snapshot.tps);
		buffer.writeFloat(snapshot.mspt);
	}

	public static TabInfoSnapshot read(PacketByteBuf buffer) {
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
