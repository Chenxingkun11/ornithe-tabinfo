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

	/** Resolved once per frame so the list offset and the panel always agree on the panel height. */
	@Unique
	private TabInfoSnapshot ornithetabinfo$snapshot;

	@Inject(method = "render", at = @At("HEAD"))
	private void ornithetabinfo$resolveSnapshot(float tickDelta, boolean screenOpen, int mouseX, int mouseY, CallbackInfo ci) {
		this.ornithetabinfo$snapshot = TabInfoClientState.get(this.minecraft.getNetworkHandler());
	}

	// The first int constant 10 after profiler.push("playerList") is the list's starting y (`int io = 10;`).
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

	// Drawn at TAIL rather than inside the player list branch: vanilla skips that branch in
	// singleplayer with no other players and no TAB objective, but the panel should still show.
	@Inject(method = "render", at = @At("TAIL"))
	private void ornithetabinfo$renderPanel(float tickDelta, boolean screenOpen, int mouseX, int mouseY, CallbackInfo ci) {
		if (this.minecraft.options.playerListKey.isPressed()) {
			TabInfoHudRenderer.render(this.minecraft, this.ornithetabinfo$snapshot);
		}
	}
}
