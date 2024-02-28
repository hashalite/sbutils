package net.xolt.sbutils.mixins;

import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.feature.features.NoGMT;
import net.xolt.sbutils.util.RegexFilters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {

    @ModifyVariable(method = "setItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack onSetStackInSlot(ItemStack stack) {
        if (ModConfig.HANDLER.instance().noGmt.enabled && MC.screen instanceof ContainerScreen && RegexFilters.mailGuiFilter.matcher(MC.screen.getTitle().getString()).matches()) {
            return NoGMT.replaceTimeInLore(stack);
        }
        return stack;
    }

    @ModifyVariable(method = "initializeContents", at = @At("HEAD"), argsOnly = true)
    private List<ItemStack> onSetStackInSlot(List<ItemStack> stacks) {
        if (ModConfig.HANDLER.instance().noGmt.enabled && MC.screen instanceof ContainerScreen && RegexFilters.mailGuiFilter.matcher(MC.screen.getTitle().getString()).matches()) {
            return NoGMT.replaceTimeInLores(stacks);
        }
        return stacks;
    }
}
