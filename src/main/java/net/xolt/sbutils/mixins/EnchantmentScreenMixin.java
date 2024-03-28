package net.xolt.sbutils.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.text.Text;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.AutoSilk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends HandledScreen<EnchantmentScreenHandler> {

    public EnchantmentScreenMixin(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!ModConfig.INSTANCE.autoSilk.showSilkButton) {
            return;
        }
        ModConfig.CornerButtonPos buttonPos = ModConfig.INSTANCE.autoSilk.silkButtonPos;
        int y = switch (buttonPos) {
            case TOP_LEFT, TOP_RIGHT -> ((this.height - this.backgroundHeight) / 2) - AutoSilk.BUTTON_HEIGHT;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> (this.height + this.backgroundHeight) / 2;
        };
        int x = switch (buttonPos) {
            case TOP_LEFT, BOTTOM_LEFT -> (this.width - this.backgroundWidth) / 2;
            case TOP_RIGHT, BOTTOM_RIGHT -> ((this.width + this.backgroundWidth) / 2) - AutoSilk.BUTTON_WIDTH;
        };
        AutoSilk.autoSilkButton = CyclingButtonWidget.onOffBuilder(Text.translatable("message.sbutils.enabled"), Text.translatable("message.sbutils.disabled"))
                .initially(ModConfig.INSTANCE.autoSilk.autoSilk)
                .build(x, y, 100, 20, Text.translatable("text.autoconfig.sbutils.option.autoSilk.autoSilk"), (button, value) -> {
                    ModConfig.INSTANCE.autoSilk.autoSilk = value;
                    if (!value) {
                        ModConfig.HOLDER.save();
                    }
                });
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.INSTANCE.autoSilk.showSilkButton && AutoSilk.autoSilkButton != null) {
            AutoSilk.autoSilkButton.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (ModConfig.INSTANCE.autoSilk.showSilkButton && AutoSilk.autoSilkButton != null) {
            AutoSilk.autoSilkButton.render(context, mouseX, mouseY, delta);
        }
    }

}
