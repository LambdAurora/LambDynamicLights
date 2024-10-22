/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.gui;

import dev.lambdaurora.lambdynlights.accessor.DynamicLightHandlerHolder;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
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
import dev.lambdaurora.spruceui.widget.text.SpruceTextFieldWidget;
import dev.yumi.commons.TriState;
import net.minecraft.TextFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Text;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class LightSourceListWidget extends SpruceEntryListWidget<LightSourceListWidget.LightSourceEntry> {
	private static final Background HIGHLIGHT_BACKGROUND = new SimpleColorBackground(128, 128, 128, 24);
	private final List<LightSourceEntry> entries = new ArrayList<>();
	private int lastIndex = 0;

	public LightSourceListWidget(Position position, int width, int height, SpruceTextFieldWidget searchBar) {
		super(position, width, height, 4, LightSourceEntry.class);

		searchBar.setChangedListener(this::update);
		searchBar.setRenderTextProvider((input, firstCharacterIndex) -> {
			if (this.children().isEmpty()) {
				return FormattedCharSequence.forward(input, Style.EMPTY.withColor(TextFormatting.RED));
			}

			var list = Stream.of(input.split(" "))
					.map(this::stylizeFilterPart)
					.map(Text::getVisualOrderText)
					.toList();
			return FormattedCharSequence.fromList(list);
		});
	}

	private void update(@Nullable String filter) {
		if (filter == null) {
			this.replaceEntries(this.entries);
		} else {
			final var entryFilter = List.of(filter.toLowerCase().split("\\s"));
			this.replaceEntries(this.entries.stream().filter(entry -> this.checkFilter(entry, entryFilter)).toList());
		}

		for (int i = 0; i < this.children().size(); i++) {
			var entry = this.children().get(i);
			if (i % 2 != 0)
				entry.setBackground(HIGHLIGHT_BACKGROUND);
			else
				entry.setBackground(EmptyBackground.EMPTY_BACKGROUND);
		}
	}

	private TriState evaluateValueFilter(String filter) {
		if (filter.isBlank()) {
			return TriState.DEFAULT;
		}

		if (filter.equalsIgnoreCase(SpruceTexts.OPTIONS_ON.getString())) {
			return TriState.TRUE;
		} else if (filter.equalsIgnoreCase(SpruceTexts.OPTIONS_OFF.getString())) {
			return TriState.FALSE;
		}

		return switch (filter) {
			case "true", "1", "on" -> TriState.TRUE;
			case "false", "0", "off" -> TriState.FALSE;
			default -> TriState.DEFAULT;
		};
	}

	private boolean checkFilter(LightSourceEntry entry, @NotNull List<String> filter) {
		var name = entry.option.lambdynlights$getName().getString().toLowerCase();

		for (var part : filter) {
			if (part.startsWith("@")) {
				// Namespace
				if (!entry.option.lambdynlights$getId().namespace().startsWith(part.substring(1))) {
					return false;
				}

				continue;
			} else if (part.startsWith("$")) {
				var valueFilter = this.evaluateValueFilter(part.substring(1));

				if (valueFilter == TriState.DEFAULT) continue;

				if (entry.option.lambdynlights$getSetting().get() != valueFilter.toBoolean()) {
					return false;
				}

				continue;
			}

			if (!name.contains(part)) {
				return false;
			}
		}

		return true;
	}

	private Text stylizeFilterPart(String filter) {
		if (filter.startsWith("@")) {
			return Text.literal(filter + " ").withStyle(TextFormatting.AQUA);
		} else if (filter.startsWith("$")) {
			var valueFilter = this.evaluateValueFilter(filter.substring(1));
			return Text.literal(filter + " ").withStyle(switch (valueFilter) {
				case TriState.TRUE, TriState.FALSE -> TextFormatting.GOLD;
				default -> TextFormatting.RED;
			});
		} else {
			return Text.literal(filter + " ");
		}
	}

	/**
	 * Adds a single option entry. The option will use all the width available.
	 *
	 * @param holder the option
	 */
	private void addEntry(DynamicLightHandlerHolder<?> holder) {
		if (holder.lambdynlights$getSetting() != null) {
			var entry = LightSourceEntry.create(this, holder);
			this.entries.add(entry);
		}
	}

	public void addAll(List<DynamicLightHandlerHolder<?>> types) {
		types.stream()
				.sorted(Comparator.comparing(handler -> handler.lambdynlights$getName().getString()))
				.forEach(this::addEntry);
		this.update(null);
	}

	/* Narration */

	@Override
	public void updateNarration(NarrationElementOutput builder) {
		this.children()
				.stream()
				.filter(AbstractSpruceWidget::isMouseHovered)
				.findFirst()
				.ifPresentOrElse(
						hoveredEntry -> {
							hoveredEntry.updateNarration(builder.nest());
							this.appendPositionNarrations(builder, hoveredEntry);
						}, () -> {
							var focusedEntry = this.getFocused();
							if (focusedEntry != null) {
								focusedEntry.updateNarration(builder.nest());
								this.appendPositionNarrations(builder, focusedEntry);
							}
						}
				);

		builder.add(NarratedElementType.USAGE, Text.translatable("narration.component_list.usage"));
	}

	public static class LightSourceEntry extends Entry implements SpruceParentWidget<SpruceWidget>, WithBackground {
		private final List<SpruceWidget> children = new ArrayList<>();
		private final LightSourceListWidget parent;
		private final DynamicLightHandlerHolder<?> option;
		private @Nullable SpruceWidget focused;
		private boolean dragging;
		private Background background = EmptyBackground.EMPTY_BACKGROUND;

		private LightSourceEntry(LightSourceListWidget parent, DynamicLightHandlerHolder<?> option) {
			this.parent = parent;
			this.option = option;
		}

		public static LightSourceEntry create(LightSourceListWidget parent, DynamicLightHandlerHolder<?> option) {
			var entry = new LightSourceEntry(parent, option);
			var setting = option.lambdynlights$getSetting();
			entry.children.add(new SpruceLabelWidget(Position.of(entry, entry.getWidth() / 2 - 145, 7), option.lambdynlights$getName(), 175));
			entry.children.add(setting.getOption().createWidget(Position.of(entry, entry.getWidth() / 2 + 70, 2), 32));
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

		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			this.forEach(widget -> widget.render(graphics, mouseX, mouseY, delta));
		}

		protected void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			this.background.render(graphics, this, 0, mouseX, mouseY, delta);
		}

		/* Narration */

		@Override
		public void updateNarration(NarrationElementOutput builder) {
			var focused = this.getFocused();
			if (focused != null) focused.updateNarration(builder);
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
