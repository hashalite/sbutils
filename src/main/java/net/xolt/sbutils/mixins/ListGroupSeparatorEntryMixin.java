package net.xolt.sbutils.mixins;

import dev.isxander.yacl.api.ListOption;
import dev.isxander.yacl.gui.OptionListWidget;
import dev.isxander.yacl.gui.TooltipButtonWidget;
import net.xolt.sbutils.config.yacl.CustomListOptionImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionListWidget.ListGroupSeparatorEntry.class)
public class ListGroupSeparatorEntryMixin {

    @Shadow(remap = false)
    @Final
    private TooltipButtonWidget addListButton;

    @Shadow(remap = false)
    @Final
    private ListOption<?> listOption;

    @Inject(method = "updateExpandMinimizeText", at = @At("TAIL"), remap = false)
    private void updateExpandMinimizeText(CallbackInfo ci) {
        if (addListButton != null && listOption instanceof CustomListOptionImpl<?> customListOption && customListOption.numberOfEntries() >= customListOption.maximumNumberOfEntries()) {
            addListButton.active = false;
        }
    }
}
