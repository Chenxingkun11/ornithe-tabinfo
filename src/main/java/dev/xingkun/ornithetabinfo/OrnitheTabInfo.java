package dev.xingkun.ornithetabinfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.ornithemc.osl.entrypoints.api.ModInitializer;

import dev.xingkun.ornithetabinfo.network.TabInfoProtocol;

public class OrnitheTabInfo implements ModInitializer {

	public static final String MOD_ID = "ornithetabinfo";
	public static final Logger LOGGER = LogManager.getLogger("Ornithe Tab Info");

	@Override
	public void init() {
		// Touching TabInfoProtocol.CHANNEL registers the channel with OSL on both sides.
		// Snapshots are pushed from MinecraftServerMixin, and OSL only delivers them to
		// clients that declared the channel.
		LOGGER.info("Ornithe Tab Info initialized (channel {}, protocol v{})", TabInfoProtocol.CHANNEL, TabInfoProtocol.VERSION);
	}
}
