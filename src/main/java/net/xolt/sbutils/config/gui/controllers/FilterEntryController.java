package net.xolt.sbutils.config.gui.controllers;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.xolt.sbutils.config.ModConfig;

import java.util.List;

public class FilterEntryController extends MultiValueController<ModConfig.ChatFiltersConfig.FilterEntry> {

    public FilterEntryController(Option<ModConfig.ChatFiltersConfig.FilterEntry> option) {
        super(option, List.of(3, 1), List.of(
                dummyController("text.sbutils.config.option.chatFilters.customFilters.regex", StringControllerBuilder::create,
                        "",
                        () -> option.pendingValue().regex,
                        (newValue) -> option.requestSet(new ModConfig.ChatFiltersConfig.FilterEntry(newValue, option.pendingValue().enabled))
                ),
                dummyController("text.sbutils.config.option.chatFilters.customFilters.enabled", TickBoxControllerBuilder::create,
                        false,
                        () -> option.pendingValue().enabled,
                        (newValue) -> option.requestSet(new ModConfig.ChatFiltersConfig.FilterEntry(option.pendingValue().regex, newValue))
                )
        ));
    }
}
