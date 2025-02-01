package net.xolt.sbutils.config.gui.controllers;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.xolt.sbutils.config.ModConfig;

import java.util.List;

public class FilterEntryController extends MultiValueController<ModConfig.ChatFiltersConfig.CustomFilter> {

    public FilterEntryController(Option<ModConfig.ChatFiltersConfig.CustomFilter> option) {
        super(option, List.of(3, 1, 1), List.of(
                dummyController("text.sbutils.config.option.chatFilters.customFilters.regex", StringControllerBuilder::create,
                        "",
                        () -> option.pendingValue().regex,
                        (newValue) -> option.requestSet(new ModConfig.ChatFiltersConfig.CustomFilter(newValue, option.pendingValue().target, option.pendingValue().enabled))
                ),
                dummyController("text.sbutils.config.option.chatFilters.customFilters.target", (target) -> EnumControllerBuilder.create(target).enumClass(ModConfig.FilterTarget.class),
                        ModConfig.FilterTarget.CHAT,
                        () -> option.pendingValue().target,
                        (newValue) -> option.requestSet(new ModConfig.ChatFiltersConfig.CustomFilter(option.pendingValue().regex, newValue, option.pendingValue().enabled))
                ),
                dummyController("text.sbutils.config.option.chatFilters.customFilters.enabled", TickBoxControllerBuilder::create,
                        false,
                        () -> option.pendingValue().enabled,
                        (newValue) -> option.requestSet(new ModConfig.ChatFiltersConfig.CustomFilter(option.pendingValue().regex, option.pendingValue().target, newValue))
                )
        ));
    }
}
