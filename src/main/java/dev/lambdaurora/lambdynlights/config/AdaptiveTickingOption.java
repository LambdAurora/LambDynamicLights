/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.config;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.option.SpruceDoubleOption;
import dev.lambdaurora.spruceui.tooltip.TooltipData;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import dev.lambdaurora.spruceui.widget.option.SpruceOptionSliderWidget;
import net.minecraft.TextFormatting;
import net.minecraft.network.chat.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntConsumer;
import java.util.function.Supplier;

public final class AdaptiveTickingOption extends SpruceDoubleOption {
	private Runnable setCallback = () -> {};
	private Slider currentSlider;

	public AdaptiveTickingOption(
			String key,
			Supplier<Double> getter,
			IntConsumer setter,
			@NotNull TooltipData tooltip
	) {
		super(
				"lambdynlights.option.adaptive_ticking." + key,
				1, 33, 1,
				getter, value -> setter.accept(value.intValue()),
				option -> {
					double value = option.get();

					return option.getDisplayText(value == 33
							? SpruceTexts.OPTIONS_OFF.copy().withStyle(TextFormatting.RED)
							: Text.literal(String.valueOf((int) value))
					);
				},
				tooltip
		);
	}

	public void setCompanion(AdaptiveTickingOption companion) {
		this.setCallback = () -> {
			if (companion.currentSlider != null) {
				companion.currentSlider.update(companion);
			}
		};
	}

	@Override
	public void set(double value) {
		super.set(value);
		this.setCallback.run();
	}

	@Override
	public SpruceWidget createWidget(Position position, int width) {
		this.currentSlider = new Slider(position, width, 20, this);
		this.getTooltip().ifPresent(this.currentSlider::setTooltip);
		return this.currentSlider;
	}

	static final class Slider extends SpruceOptionSliderWidget {
		public Slider(Position position, int width, int height, SpruceDoubleOption option) {
			super(position, width, height, option);
		}

		public void update(AdaptiveTickingOption option) {
			this.value = option.getRatio(option.get());
			this.updateMessage();
		}
	}
}
