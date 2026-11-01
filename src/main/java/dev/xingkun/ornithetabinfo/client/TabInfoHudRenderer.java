package dev.xingkun.ornithetabinfo.client;

import java.util.Locale;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.Window;

import dev.xingkun.ornithetabinfo.data.TabInfoSnapshot;

public final class TabInfoHudRenderer {

	private static final String TITLE = "Ornithe Tab Info";
	private static final String UNAVAILABLE = "Server data unavailable";

	private static final int TOP = 10;
	private static final int LINE_HEIGHT = 9;
	private static final int PADDING_X = 3;

	private static final int BACKGROUND_COLOR = Integer.MIN_VALUE;
	private static final int TITLE_COLOR = 0xFFFF55;
	private static final int TEXT_COLOR = 0xFFFFFF;
	private static final int UNAVAILABLE_COLOR = 0xAAAAAA;

	private TabInfoHudRenderer() {
	}

	public static int playerListTop(TabInfoSnapshot snapshot) {
		return TOP + lineCount(snapshot) * LINE_HEIGHT + 1;
	}

	public static void render(Minecraft minecraft, TabInfoSnapshot snapshot) {
		TextRenderer textRenderer = minecraft.textRenderer;
		int screenWidth = new Window(minecraft, minecraft.width, minecraft.height).getWidth();

		String[] lines;
		int[] colors;

		if (snapshot == null) {
			lines = new String[] { TITLE, UNAVAILABLE };
			colors = new int[] { TITLE_COLOR, UNAVAILABLE_COLOR };
		} else {
			lines = new String[] {
				TITLE,
				"Autosave: " + snapshot.autosaveRemainingGt + "gt | Prev: " + formatGt(snapshot.previousSaveGt),
				"TPS: " + formatDecimal(snapshot.tps) + " | MSPT: " + formatDecimal(snapshot.mspt) + "ms"
			};
			colors = new int[] { TITLE_COLOR, TEXT_COLOR, TEXT_COLOR };
		}

		int panelWidth = 0;
		for (String line : lines) {
			panelWidth = Math.max(panelWidth, textRenderer.getWidth(line));
		}
		panelWidth += 2 * PADDING_X;

		int left = (screenWidth - panelWidth) / 2;

		GuiElement.fill(left, TOP - 1, left + panelWidth, TOP + lines.length * LINE_HEIGHT - 1, BACKGROUND_COLOR);

		for (int i = 0; i < lines.length; i++) {
			int x = (screenWidth - textRenderer.getWidth(lines[i])) / 2;
			textRenderer.drawWithShadow(lines[i], x, TOP + i * LINE_HEIGHT, colors[i]);
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private static int lineCount(TabInfoSnapshot snapshot) {
		return snapshot == null ? 2 : 3;
	}

	private static String formatGt(float gt) {
		return Float.isNaN(gt) ? "--" : formatDecimal(gt) + "gt";
	}

	private static String formatDecimal(float value) {
		return Float.isNaN(value) || Float.isInfinite(value) ? "--" : String.format(Locale.ROOT, "%.1f", value);
	}
}
