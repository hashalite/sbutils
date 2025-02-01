package net.xolt.sbutils.config.gui.controllers;

import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.cycling.EnumController;
import dev.isxander.yacl.gui.controllers.string.StringController;
import net.xolt.sbutils.config.ModConfig;

import java.util.List;

public class FilterEntryController extends MultiValueController<ModConfig.ChatFiltersConfig.CustomFilter> {

    public FilterEntryController(Option<ModConfig.ChatFiltersConfig.CustomFilter> option) {
        super(option, List.of(3, 1, 1), List.of(
                dummyController("text.sbutils.config.option.chatFilters.customFilters.regex", StringController::new,
                        "",
                        () -> option.pendingValue().regex,
                        (newValue) -> option.requestSet(new ModConfig.ChatFiltersConfig.CustomFilter(newValue, option.pendingValue().target, option.pendingValue().enabled))
                ),
                dummyController("text.sbutils.config.option.chatFilters.customFilters.target", EnumController::new,
                        ModConfig.FilterTarget.CHAT,
                        () -> option.pendingValue().target,
                        (newValue) -> option.requestSet(new ModConfig.ChatFiltersConfig.CustomFilter(option.pendingValue().regex, newValue, option.pendingValue().enabled))
                ),
                dummyController("text.sbutils.config.option.chatFilters.customFilters.enabled", TickBoxController::new,
                        false,
                        () -> option.pendingValue().enabled,
                        (newValue) -> option.requestSet(new ModConfig.ChatFiltersConfig.CustomFilter(option.pendingValue().regex, option.pendingValue().target, newValue))
                )
        ));
    }
}
