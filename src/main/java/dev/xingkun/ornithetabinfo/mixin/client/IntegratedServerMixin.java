package dev.xingkun.ornithetabinfo.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.integrated.IntegratedServer;

import dev.xingkun.ornithetabinfo.server.ServerMetricsAccess;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {

	@Shadow private boolean paused;

	// While paused, IntegratedServer.tick() skips super.tick(), so the gap between two
	// real ticks would otherwise be measured as one very long tick interval.
	@Inject(
		method = "tick",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/server/integrated/IntegratedServer;paused:Z",
			opcode = Opcodes.PUTFIELD,
			shift = At.Shift.AFTER
		)
	)
	private void ornithetabinfo$onPauseUpdated(CallbackInfo ci) {
		if (this.paused) {
			((ServerMetricsAccess) (Object) this).ornithetabinfo$getMetrics().skipNextTickInterval();
		}
	}
}
