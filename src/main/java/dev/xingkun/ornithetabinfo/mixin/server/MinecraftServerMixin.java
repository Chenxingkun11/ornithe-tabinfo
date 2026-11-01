package dev.xingkun.ornithetabinfo.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.MinecraftServer;

import net.ornithemc.osl.networking.api.server.ServerPlayNetworking;

import dev.xingkun.ornithetabinfo.data.TabInfoSnapshot;
import dev.xingkun.ornithetabinfo.network.TabInfoProtocol;
import dev.xingkun.ornithetabinfo.server.ServerMetricsAccess;
import dev.xingkun.ornithetabinfo.server.ServerMetricsTracker;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ServerMetricsAccess {

	@Shadow private int ticks;

	@Unique
	private final ServerMetricsTracker ornithetabinfo$metrics = new ServerMetricsTracker();

	@Inject(method = "tick", at = @At("HEAD"))
	private void ornithetabinfo$onTickStart(CallbackInfo ci) {
		this.ornithetabinfo$metrics.onTickStart(System.nanoTime());
	}

	// The autosave branch is `if (ticks % 900 == 0) { playerManager.saveAll(); saveWorlds(true); }`;
	// timing spans both calls so player data and chunk saving are included.
	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/PlayerManager;saveAll()V"
		)
	)
	private void ornithetabinfo$onAutosaveStart(CallbackInfo ci) {
		this.ornithetabinfo$metrics.onAutosaveStart(System.nanoTime());
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;saveWorlds(Z)V",
			shift = At.Shift.AFTER
		)
	)
	private void ornithetabinfo$onAutosaveEnd(CallbackInfo ci) {
		this.ornithetabinfo$metrics.onAutosaveEnd(System.nanoTime());
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void ornithetabinfo$onTickEnd(CallbackInfo ci) {
		this.ornithetabinfo$metrics.onTickEnd(System.nanoTime());

		if (ServerMetricsTracker.shouldSync(this.ticks)) {
			TabInfoSnapshot snapshot = this.ornithetabinfo$metrics.snapshot(this.ticks);
			// send(channel, ...) only reaches players whose client declared the channel,
			// so vanilla clients never receive this payload.
			ServerPlayNetworking.send(TabInfoProtocol.CHANNEL, buffer -> TabInfoProtocol.write(buffer, snapshot));
		}
	}

	@Override
	public ServerMetricsTracker ornithetabinfo$getMetrics() {
		return this.ornithetabinfo$metrics;
	}
}
