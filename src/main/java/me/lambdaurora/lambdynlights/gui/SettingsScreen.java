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
import me.lambdaurora.lambdynlights.ExplosiveLightingMode;
import me.lambdaurora.lambdynlights.LambDynLights;
import me.lambdaurora.lambdynlights.LambDynLightsCompat;
import me.lambdaurora.spruceui.Tooltip;
import me.lambdaurora.spruceui.option.SpruceBooleanOption;
import me.lambdaurora.spruceui.option.SpruceCyclingOption;
import me.lambdaurora.spruceui.option.SpruceResetOption;
import me.lambdaurora.spruceui.option.SpruceSeparatorOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.Option;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the settings screen of LambDynamicLights.
 *
 * @author LambdAurora
 * @version 1.3.0
 * @since 1.0.0
 */
public class SettingsScreen extends Screen
{
    private final DynamicLightsConfig config;
    private final Screen              parent;
    private final Option              entitiesOption;
    private final Option              blockEntitiesOption;
    private final Option              waterSensitiveOption;
    private final Option              creeperLightingOption;
    private final Option              tntLightingOption;
    private final Option              resetOption;
    private       ButtonListWidget    list;

    public SettingsScreen(@Nullable Screen parent)
    {
        super(new TranslatableText("lambdynlights.menu.title"));
        this.parent = parent;
        this.config = LambDynLights.get().config;

        this.entitiesOption = new SpruceBooleanOption("lambdynlights.option.entities",
                this.config::hasEntitiesLightSource,
                this.config::setEntitiesLightSource,
                new TranslatableText("lambdynlights.tooltip.entities"), true);
        this.blockEntitiesOption = new SpruceBooleanOption("lambdynlights.option.block_entities",
                this.config::hasBlockEntitiesLightSource,
                this.config::setBlockEntitiesLightSource,
                new TranslatableText("lambdynlights.tooltip.block_entities"), true);
        this.waterSensitiveOption = new SpruceBooleanOption("lambdynlights.option.water_sensitive",
                this.config::hasWaterSensitiveCheck,
                this.config::setWaterSensitiveCheck,
                new TranslatableText("lambdynlights.tooltip.water_sensitive"), true);
        this.creeperLightingOption = new SpruceCyclingOption("entity.minecraft.creeper",
                amount -> this.config.setCreeperLightingMode(this.config.getCreeperLightingMode().next()),
                option -> option.getDisplayText(this.config.getCreeperLightingMode().getTranslatedText()),
                new TranslatableText("lambdynlights.tooltip.creeper_lighting",
                        ExplosiveLightingMode.OFF.getTranslatedText(),
                        ExplosiveLightingMode.SIMPLE.getTranslatedText(),
                        ExplosiveLightingMode.FANCY.getTranslatedText()));
        this.tntLightingOption = new SpruceCyclingOption("block.minecraft.tnt",
                amount -> this.config.setTntLightingMode(this.config.getTntLightingMode().next()),
                option -> option.getDisplayText(this.config.getTntLightingMode().getTranslatedText()),
                new TranslatableText("lambdynlights.tooltip.tnt_lighting",
                        ExplosiveLightingMode.OFF.getTranslatedText(),
                        ExplosiveLightingMode.SIMPLE.getTranslatedText(),
                        ExplosiveLightingMode.FANCY.getTranslatedText()));
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
        this.list.addSingleOptionEntry(new SpruceSeparatorOption("lambdynlights.menu.light_sources", true, null));
        this.list.addOptionEntry(this.entitiesOption, this.blockEntitiesOption);
        this.list.addOptionEntry(this.waterSensitiveOption, null);
        this.list.addOptionEntry(this.creeperLightingOption, this.tntLightingOption);
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
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);

        if (LambDynLightsCompat.isCanvasInstalled()) {
            drawCenteredString(matrices, this.textRenderer, I18n.translate("lambdynlights.menu.canvas.1"), this.width / 2, this.height - 29 - (5 + this.textRenderer.fontHeight) * 3, 0xFFFF0000);
            drawCenteredString(matrices, this.textRenderer, I18n.translate("lambdynlights.menu.canvas.2"), this.width / 2, this.height - 29 - (5 + this.textRenderer.fontHeight) * 2, 0xFFFF0000);
        }

        Tooltip.renderAll(matrices);
    }
}
