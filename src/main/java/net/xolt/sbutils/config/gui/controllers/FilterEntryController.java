package net.xolt.sbutils.config.gui.controllers;

import dev.isxander.yacl3.api.Option;
import net.xolt.sbutils.config.ModConfig;
//? yacl: >=3.0.0 {
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
//? } else {
/*import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import dev.isxander.yacl3.gui.controllers.TickBoxController;
*///? }

import java.util.List;

public class FilterEntryController extends MultiValueController<ModConfig.ChatFiltersConfig.CustomFilter> {

    public FilterEntryController(Option<ModConfig.ChatFiltersConfig.CustomFilter> option) {
        super(option, List.of(3, 1, 1), List.of(
                dummyController("text.sbutils.config.option.chatFilters.customFilters.regex",
                        //? yacl: >=3.0.0 {
                        StringControllerBuilder::create,
                        //? } else
                        //StringController::new,
                        "",
                        () -> option.pendingValue().regex,
                        (newValue) -> option.requestSet(new ModConfig.ChatFiltersConfig.CustomFilter(newValue, option.pendingValue().target, option.pendingValue().enabled))
                ),
                dummyController("text.sbutils.config.option.chatFilters.customFilters.target",
                        //? yacl: >=3.0.0 {
                        (target) -> EnumControllerBuilder.create(target).enumClass(ModConfig.FilterTarget.class),
                        //? } else
                        //EnumController::new,
                        ModConfig.FilterTarget.CHAT,
                        () -> option.pendingValue().target,
                        (newValue) -> option.requestSet(new ModConfig.ChatFiltersConfig.CustomFilter(option.pendingValue().regex, newValue, option.pendingValue().enabled))
                ),
                dummyController("text.sbutils.config.option.chatFilters.customFilters.enabled",
                        //? yacl: >=3.0.0 {
                        TickBoxControllerBuilder::create,
                         //? } else
                        //TickBoxController::new,
                        false,
                        () -> option.pendingValue().enabled,
                        (newValue) -> option.requestSet(new ModConfig.ChatFiltersConfig.CustomFilter(option.pendingValue().regex, option.pendingValue().target, newValue))
                )
        ));
    }
}
