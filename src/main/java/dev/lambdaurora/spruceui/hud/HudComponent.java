/*
 * Copyright © 2020-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of SpruceUI.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.spruceui.hud;

import dev.lambdaurora.spruceui.util.Identifiable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a HUD component.
 *
 * @author LambdAurora
 * @version 3.2.1
 * @since 1.2.0
 */
public abstract class HudComponent implements Identifiable {
	protected final Identifier identifier;
	protected boolean enabled = true;
	protected int x;
	protected int y;

	protected HudComponent(Identifier identifier, int x, int y) {
		this.identifier = identifier;
		this.x = x;
		this.y = y;
	}

	/**
	 * Returns the translation key of this component.
	 *
	 * @return This component's translation key.
	 */
	public String getTranslationKey() {
		return this.identifier.getNamespace() + ".hud.component." + this.identifier.getPath();
	}

	/**
	 * Returns whether the HUD component is enabled or not.
	 *
	 * @return True if the HUD component is enabled, else false.
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * Sets whether the HUD component is enabled or not.
	 *
	 * @param enabled True if the HUD component is enabled, else false.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Renders the HUD component if enabled.
	 *
	 * @param tickDelta Progress for linearly interpolating between the previous and current game state.
	 * @see #isEnabled()
	 */
	public abstract void render(GuiGraphics guiGraphics, float tickDelta);

	/**
	 * Updates the HUD each tick if enabled and has tick updates.
	 *
	 * @see #isEnabled()
	 * @see #hasTicks()
	 */
	public void tick() {
	}

	/**
	 * Returns whether this HUD has tick updates.
	 *
	 * @return True if this HUD has tick updates, else false.
	 * @see #tick()
	 */
	public boolean hasTicks() {
		return false;
	}

	@Override
	public @NotNull Identifier getIdentifier() {
		return this.identifier;
	}
}
