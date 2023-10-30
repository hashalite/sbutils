package net.xolt.sbutils.mixins;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.NoGMT;
import net.xolt.sbutils.util.RegexFilters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @ModifyVariable(method = "setStackInSlot", at = @At("HEAD"), argsOnly = true)
    private ItemStack onSetStackInSlot(ItemStack stack) {
        if (ModConfig.INSTANCE.getConfig().noGMT && MC.currentScreen instanceof GenericContainerScreen && RegexFilters.mailGuiFilter.matcher(MC.currentScreen.getTitle().getString()).matches()) {
            return NoGMT.replaceTimeInLore(stack);
        }
        return stack;
    }

    @ModifyVariable(method = "updateSlotStacks", at = @At("HEAD"), argsOnly = true)
    private List<ItemStack> onSetStackInSlot(List<ItemStack> stacks) {
        if (ModConfig.INSTANCE.getConfig().noGMT && MC.currentScreen instanceof GenericContainerScreen && RegexFilters.mailGuiFilter.matcher(MC.currentScreen.getTitle().getString()).matches()) {
            return NoGMT.replaceTimeInLores(stacks);
        }
        return stacks;
    }


}
