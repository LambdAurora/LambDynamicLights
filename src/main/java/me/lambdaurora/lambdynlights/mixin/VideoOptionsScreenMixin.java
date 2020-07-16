/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.mixin;

import me.lambdaurora.lambdynlights.gui.SettingsScreen;
import me.lambdaurora.spruceui.Tooltip;
import me.lambdaurora.spruceui.option.SpruceSimpleActionOption;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.VideoOptionsScreen;
import net.minecraft.client.gui.screen.options.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoOptionsScreen.class)
public class VideoOptionsScreenMixin extends GameOptionsScreen
{
    @Shadow
    private ButtonListWidget list;

    private Option lambdynlights_option;

    public VideoOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title)
    {
        super(parent, gameOptions, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstruct(Screen parent, GameOptions gameOptions, CallbackInfo ci)
    {
        this.lambdynlights_option = new SpruceSimpleActionOption("lambdynlights.menu.title", btn -> this.client.openScreen(new SettingsScreen(this)));
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci)
    {
        this.list.addSingleOptionEntry(this.lambdynlights_option);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci)
    {
        Tooltip.renderAll(matrices);
    }
}
