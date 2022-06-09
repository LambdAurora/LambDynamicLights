/*
 * Copyright © 2020-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.gui;

import dev.lambdaurora.lambdynlights.accessor.DynamicLightHandlerHolder;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.background.Background;
import dev.lambdaurora.spruceui.background.EmptyBackground;
import dev.lambdaurora.spruceui.background.SimpleColorBackground;
import dev.lambdaurora.spruceui.navigation.NavigationDirection;
import dev.lambdaurora.spruceui.navigation.NavigationUtils;
import dev.lambdaurora.spruceui.widget.AbstractSpruceWidget;
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import dev.lambdaurora.spruceui.widget.WithBackground;
import dev.lambdaurora.spruceui.widget.container.SpruceEntryListWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceParentWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class LightSourceListWidget extends SpruceEntryListWidget<LightSourceListWidget.LightSourceEntry> {
	private static final Background HIGHLIGHT_BACKGROUND = new SimpleColorBackground(128, 128, 128, 24);
	private int lastIndex = 0;

	public LightSourceListWidget(Position position, int width, int height) {
		super(position, width, height, 4, LightSourceEntry.class);
	}

	/**
	 * Adds a single option entry. The option will use all the width available.
	 *
	 * @param holder the option
	 * @return the index of the added entry
	 */
	public int addEntry(DynamicLightHandlerHolder<?> holder) {
		if (holder.lambdynlights$getSetting() != null) {
			var entry = LightSourceEntry.create(this, holder);
			int index = this.addEntry(entry);
			if (index % 2 != 0)
				entry.setBackground(HIGHLIGHT_BACKGROUND);
			return index;
		}
		return -1;
	}

	public void addAll(List<DynamicLightHandlerHolder<?>> types) {
		for (var type : types)
			this.addEntry(type);
	}

	/* Narration */

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		this.children()
				.stream()
				.filter(AbstractSpruceWidget::isMouseHovered)
				.findFirst()
				.ifPresentOrElse(
						hoveredEntry -> {
							hoveredEntry.appendNarrations(builder.nextMessage());
							this.appendPositionNarrations(builder, hoveredEntry);
						}, () -> {
							var focusedEntry = this.getFocused();
							if (focusedEntry != null) {
								focusedEntry.appendNarrations(builder.nextMessage());
								this.appendPositionNarrations(builder, focusedEntry);
							}
						}
				);

		builder.put(NarrationPart.USAGE, Text.translatable("narration.component_list.usage"));
	}

	public static class LightSourceEntry extends Entry implements SpruceParentWidget<SpruceWidget>, WithBackground {
		private final List<SpruceWidget> children = new ArrayList<>();
		private final LightSourceListWidget parent;
		private @Nullable SpruceWidget focused;
		private boolean dragging;
		private Background background = EmptyBackground.EMPTY_BACKGROUND;

		private LightSourceEntry(LightSourceListWidget parent) {
			this.parent = parent;
		}

		public static LightSourceEntry create(LightSourceListWidget parent, DynamicLightHandlerHolder<?> option) {
			var entry = new LightSourceEntry(parent);
			var setting = option.lambdynlights$getSetting();
			entry.children.add(new SpruceLabelWidget(Position.of(entry, entry.getWidth() / 2 - 155, 7), option.lambdynlights$getName(), 175));
			entry.children.add(setting.getOption().createWidget(Position.of(entry, entry.getWidth() / 2 + 60, 2), 75));
			return entry;
		}

		@Override
		public int getWidth() {
			return this.parent.getWidth() - (this.parent.getBorder().getThickness() * 2);
		}

		@Override
		public int getHeight() {
			return this.children.stream().mapToInt(SpruceWidget::getHeight).reduce(Integer::max).orElse(0) + 4;
		}

		@Override
		public List<SpruceWidget> children() {
			return this.children;
		}

		@Override
		public @Nullable SpruceWidget getFocused() {
			return this.focused;
		}

		@Override
		public void setFocused(@Nullable SpruceWidget focused) {
			if (this.focused == focused)
				return;
			if (this.focused != null)
				this.focused.setFocused(false);
			this.focused = focused;
		}

		@Override
		public void setFocused(boolean focused) {
			super.setFocused(focused);
			if (!focused) {
				this.setFocused(null);
			}
		}

		@Override
		public Background getBackground() {
			return this.background;
		}

		@Override
		public void setBackground(Background background) {
			this.background = background;
		}

		/* Input */

		@Override
		protected boolean onMouseClick(double mouseX, double mouseY, int button) {
			var it = this.iterator();

			SpruceWidget element;
			do {
				if (!it.hasNext()) {
					return false;
				}

				element = it.next();
			} while (!element.mouseClicked(mouseX, mouseY, button));

			this.setFocused(element);
			if (button == GLFW.GLFW_MOUSE_BUTTON_1)
				this.dragging = true;

			return true;
		}

		@Override
		protected boolean onMouseRelease(double mouseX, double mouseY, int button) {
			this.dragging = false;
			return this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseReleased(mouseX, mouseY, button)).isPresent();
		}

		@Override
		protected boolean onMouseDrag(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			return this.getFocused() != null && this.dragging && button == GLFW.GLFW_MOUSE_BUTTON_1
					&& this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}

		@Override
		protected boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
			return this.focused != null && this.focused.keyPressed(keyCode, scanCode, modifiers);
		}

		@Override
		protected boolean onKeyRelease(int keyCode, int scanCode, int modifiers) {
			return this.focused != null && this.focused.keyReleased(keyCode, scanCode, modifiers);
		}

		@Override
		protected boolean onCharTyped(char chr, int keyCode) {
			return this.focused != null && this.focused.charTyped(chr, keyCode);
		}

		/* Rendering */

		@Override
		protected void renderWidget(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			this.forEach(widget -> widget.render(matrices, mouseX, mouseY, delta));
		}

		@Override
		protected void renderBackground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			this.background.render(matrices, this, 0, mouseX, mouseY, delta);
		}

		/* Narration */

		@Override
		public void appendNarrations(NarrationMessageBuilder builder) {
			var focused = this.getFocused();
			if (focused != null) focused.appendNarrations(builder);
		}

		/* Navigation */

		@Override
		public boolean onNavigation(NavigationDirection direction, boolean tab) {
			if (this.requiresCursor()) return false;
			if (!tab && direction.isVertical()) {
				if (this.isFocused()) {
					this.setFocused(null);
					return false;
				}
				int lastIndex = this.parent.lastIndex;
				if (lastIndex >= this.children.size())
					lastIndex = this.children.size() - 1;
				if (!this.children.get(lastIndex).onNavigation(direction, tab))
					return false;
				this.setFocused(this.children.get(lastIndex));
				return true;
			}

			boolean result = NavigationUtils.tryNavigate(direction, tab, this.children, this.focused, this::setFocused, true);
			if (result) {
				this.setFocused(true);
				if (direction.isHorizontal() && this.getFocused() != null) {
					this.parent.lastIndex = this.children.indexOf(this.getFocused());
				}
			}
			return result;
		}
	}
}
