package dev.xingkun.ornithetabinfo.client;

import net.ornithemc.osl.entrypoints.api.client.ClientModInitializer;
import net.ornithemc.osl.networking.api.client.ClientConnectionEvents;
import net.ornithemc.osl.networking.api.client.ClientPlayNetworking;

import dev.xingkun.ornithetabinfo.OrnitheTabInfo;
import dev.xingkun.ornithetabinfo.data.TabInfoSnapshot;
import dev.xingkun.ornithetabinfo.network.TabInfoProtocol;

public class OrnitheTabInfoClient implements ClientModInitializer {

	private static volatile boolean warnedUnsupportedProtocol;

	@Override
	public void initClient() {
		// Registering a listener is also what declares the channel to the server during the OSL handshake.
		ClientPlayNetworking.registerListener(TabInfoProtocol.CHANNEL, (ctx, buffer) -> {
			TabInfoSnapshot snapshot = TabInfoProtocol.read(buffer);

			if (snapshot == null) {
				if (!warnedUnsupportedProtocol) {
					warnedUnsupportedProtocol = true;
					OrnitheTabInfo.LOGGER.warn("Ignoring {} packets with an unsupported protocol version (expected v{})", TabInfoProtocol.CHANNEL, TabInfoProtocol.VERSION);
				}
				return;
			}

			TabInfoClientState.accept(ctx.networkHandler(), snapshot);
		});

		ClientConnectionEvents.LOGIN.register(ctx -> TabInfoClientState.clear());
		ClientConnectionEvents.DISCONNECT.register(ctx -> TabInfoClientState.clear());
	}
}
