/*
 * Copyright © 2020-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of SpruceUI.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.spruceui;

import com.google.common.collect.Queues;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * Represents a tooltip.
 *
 * @author LambdAurora
 * @version 3.3.0
 * @since 1.0.0
 */
public class Tooltip implements SprucePositioned {
	private static final Queue<Tooltip> TOOLTIPS = Queues.newConcurrentLinkedQueue();
	private static boolean delayed = false;
	private final int x;
	private final int y;
	private final List<OrderedText> tooltip;

	public Tooltip(int x, int y, String tooltip, int parentWidth) {
		this(x, y, StringVisitable.plain(tooltip), parentWidth);
	}

	public Tooltip(int x, int y, StringVisitable tooltip, int parentWidth) {
		this(x, y, MinecraftClient.getInstance().textRenderer.wrapLines(tooltip, Math.max(parentWidth * 2 / 3, 200)));
	}

	public Tooltip(int x, int y, List<OrderedText> tooltip) {
		this.x = x;
		this.y = y;
		this.tooltip = tooltip;
	}

	public static Tooltip create(int x, int y, String tooltip, int parentWidth) {
		return new Tooltip(x, y, tooltip, parentWidth);
	}

	public static Tooltip create(int x, int y, StringVisitable tooltip, int parentWidth) {
		return new Tooltip(x, y, tooltip, parentWidth);
	}

	public static Tooltip create(int x, int y, List<OrderedText> tooltip) {
		return new Tooltip(x, y, tooltip);
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	/**
	 * Returns whether the tooltip should render or not.
	 *
	 * @return {@code true} if the tooltip should render, else {@code false}
	 */
	public boolean shouldRender() {
		return !this.tooltip.isEmpty();
	}

	/**
	 * Renders the tooltip.
	 *
	 * @param guiGraphics The GuiGraphics instance used to render.
	 */
	public void render(GuiGraphics guiGraphics) {
		guiGraphics.drawTooltip(MinecraftClient.getInstance().textRenderer, this.tooltip, DefaultTooltipPositioner.INSTANCE, this.x, this.y);
	}

	/**
	 * Queues the tooltip to render.
	 */
	public void queue() {
		TOOLTIPS.add(this);
	}

	/**
	 * Queues the tooltip of the widget to render.
	 *
	 * @param widget the widget
	 * @param mouseX the mouse X coordinate
	 * @param mouseY the mouse Y coordinate
	 * @param <T> the type of the widget
	 * @since 1.6.0
	 */
	public static <T extends Tooltipable & SpruceWidget> void queueFor(T widget, int mouseX, int mouseY, int tooltipTicks,
	                                                                   IntConsumer tooltipTicksSetter,
	                                                                   long lastTick,
	                                                                   LongConsumer lastTickSetter) {
		if (widget.isVisible()) {
			widget.getTooltip().ifPresent(tooltip -> {
				long currentRender = System.currentTimeMillis();
				if (lastTick != 0) {
					if (currentRender - lastTick >= 20) {
						tooltipTicksSetter.accept(tooltipTicks + 1);
						lastTickSetter.accept(currentRender);
					}
				} else lastTickSetter.accept(currentRender);

				if (!widget.isFocused() && !widget.isMouseHovered())
					tooltipTicksSetter.accept(0);

				if (!tooltip.getString().isEmpty() && tooltipTicks >= 45) {
					var wrappedTooltipText = MinecraftClient.getInstance().textRenderer.wrapLines(
							tooltip, Math.max(widget.getWidth() * 2 / 3, 200));
					if (widget.isMouseHovered())
						create(mouseX, mouseY, wrappedTooltipText).queue();
					else if (widget.isFocused())
						create(widget.getX() - 12, widget.getY() + widget.getHeight() + 16,
								wrappedTooltipText)
								.queue();
				}
			});
		}
	}

	/**
	 * Sets whether tooltip rendering is delayed or not.
	 *
	 * @param delayed true if tooltip rendering is delayed
	 */
	@ApiStatus.Internal
	static void setDelayedRender(boolean delayed) {
		Tooltip.delayed = delayed;
	}

	/**
	 * Renders all the tooltips.
	 *
	 * @param screen the screen on which the tooltips are rendered
	 * @param matrices the matrices
	 */
	public static void renderAll(GuiGraphics guiGraphics) {
		if (delayed)
			return;
		synchronized (TOOLTIPS) {
			Tooltip tooltip;

			while ((tooltip = TOOLTIPS.poll()) != null)
				tooltip.render(guiGraphics);
		}
	}
}
