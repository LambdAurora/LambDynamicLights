/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.gui;

import me.lambdaurora.lambdynlights.DynamicLightsConfig;
import me.lambdaurora.lambdynlights.LambDynLights;
import me.lambdaurora.spruceui.Tooltip;
import me.lambdaurora.spruceui.option.SpruceResetOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.Option;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the settings screen of LambDynamicLights.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SettingsScreen extends Screen
{
    private final DynamicLightsConfig config;
    private final Screen              parent;
    private final Option              resetOption;
    private       ButtonListWidget    list;

    public SettingsScreen(@Nullable Screen parent)
    {
        super(new TranslatableText("lambdynlights.menu.title"));
        this.parent = parent;
        this.config = LambDynLights.get().config;

        this.resetOption = new SpruceResetOption(btn -> {
            this.config.reset();
            MinecraftClient client = MinecraftClient.getInstance();
            this.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        });
    }

    @Override
    public void removed()
    {
        super.removed();
        this.config.save();
    }

    private int getTextHeight()
    {
        return (5 + this.textRenderer.fontHeight) * 3 + 5;
    }

    @Override
    protected void init()
    {
        super.init();
        int buttonHeight = 20;

        this.list = new ButtonListWidget(this.client, this.width, this.height, 43, this.height - 29 - this.getTextHeight(), 25);
        this.list.addSingleOptionEntry(this.config.dynamicLightsModeOption);
        this.children.add(list);

        this.addButton(this.resetOption.createButton(this.client.options, this.width / 2 - 155, this.height - 29, 150));
        this.addButton(new ButtonWidget(this.width / 2 - 155 + 160, this.height - 29, 150, buttonHeight, new TranslatableText("gui.done"),
                (buttonWidget) -> this.client.openScreen(this.parent)));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
    {
        this.renderBackground(matrices);
        this.list.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);

        Tooltip.renderAll(matrices);
    }
}
