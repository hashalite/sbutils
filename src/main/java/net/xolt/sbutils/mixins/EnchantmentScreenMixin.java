package net.xolt.sbutils.mixins;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.feature.features.AutoSilk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=1.21.11 {
import net.minecraft.client.input.MouseButtonEvent;
//? }

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends AbstractContainerScreen<EnchantmentMenu> {

    public EnchantmentScreenMixin(EnchantmentMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!ModConfig.instance().autoSilk.showButton) {
            return;
        }
        ModConfig.CornerButtonPos buttonPos = ModConfig.instance().autoSilk.buttonPos;
        int y = switch (buttonPos) {
            case TOP_LEFT, TOP_RIGHT -> ((this.height - this.imageHeight) / 2) - AutoSilk.BUTTON_HEIGHT;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> (this.height + this.imageHeight) / 2;
        };
        int x = switch (buttonPos) {
            case TOP_LEFT, BOTTOM_LEFT -> (this.width - this.imageWidth) / 2;
            case TOP_RIGHT, BOTTOM_RIGHT -> ((this.width + this.imageWidth) / 2) - AutoSilk.BUTTON_WIDTH;
        };

        SbUtils.FEATURES.get(AutoSilk.class).autoSilkButton = CycleButton.booleanBuilder(Component.translatable("message.sbutils.enabled"), Component.translatable("message.sbutils.disabled")
                        //? if >=1.21.11
                        , ModConfig.instance().autoSilk.enabled
                )
                //? if <1.21.11
                //.withInitialValue(ModConfig.instance().autoSilk.enabled)
                .create(x, y, 100, 20, Component.translatable("text.sbutils.config.category.autoSilk"), (button, value) -> {
                    ModConfig.instance().autoSilk.enabled = value;
                    if (!value) {
                        ModConfig.HANDLER.save();
                    }
                });
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    //? if >=1.21.11 {
    private void onMouseClicked(MouseButtonEvent event, boolean isDoubleClick, CallbackInfoReturnable<Boolean> cir) {
    //? } else
    //private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        CycleButton<Boolean> autoSilkButton = SbUtils.FEATURES.get(AutoSilk.class).autoSilkButton;
        if (ModConfig.instance().autoSilk.showButton && autoSilkButton != null) {
            //? if >=1.21.11 {
            autoSilkButton.mouseClicked(event, isDoubleClick);
            //? } else
            //autoSilkButton.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        CycleButton<Boolean> autoSilkButton = SbUtils.FEATURES.get(AutoSilk.class).autoSilkButton;
        if (ModConfig.instance().autoSilk.showButton && autoSilkButton != null) {
            autoSilkButton.render(context, mouseX, mouseY, delta);
        }
    }

}
