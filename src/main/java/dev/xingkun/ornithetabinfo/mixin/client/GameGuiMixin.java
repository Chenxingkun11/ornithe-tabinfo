package dev.xingkun.ornithetabinfo.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GameGui;

import dev.xingkun.ornithetabinfo.client.TabInfoClientState;
import dev.xingkun.ornithetabinfo.client.TabInfoHudRenderer;
import dev.xingkun.ornithetabinfo.data.TabInfoSnapshot;

@Mixin(GameGui.class)
public abstract class GameGuiMixin {

	@Shadow @Final private Minecraft minecraft;


	@Unique
	private TabInfoSnapshot ornithetabinfo$snapshot;

	@Inject(method = "render", at = @At("HEAD"))
	private void ornithetabinfo$resolveSnapshot(float tickDelta, boolean screenOpen, int mouseX, int mouseY, CallbackInfo ci) {
		this.ornithetabinfo$snapshot = TabInfoClientState.get(this.minecraft.getNetworkHandler());
	}


	@ModifyConstant(
		method = "render",
		slice = @Slice(
			from = @At(
				value = "INVOKE_STRING",
				target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V",
				args = "ldc=playerList"
			)
		),
		constant = @Constant(
			intValue = 10,
			ordinal = 0
		)
	)
	private int ornithetabinfo$movePlayerListDown(int top) {
		return TabInfoHudRenderer.playerListTop(this.ornithetabinfo$snapshot);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void ornithetabinfo$renderPanel(float tickDelta, boolean screenOpen, int mouseX, int mouseY, CallbackInfo ci) {
		if (this.minecraft.options.playerListKey.isPressed()) {
			TabInfoHudRenderer.render(this.minecraft, this.ornithetabinfo$snapshot);
		}
	}
}
