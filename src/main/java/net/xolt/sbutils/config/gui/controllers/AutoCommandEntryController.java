package net.xolt.sbutils.config.gui.controllers;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.xolt.sbutils.config.ModConfig;

import java.util.List;

public class AutoCommandEntryController extends MultiValueController<ModConfig.AutoCommandConfig.AutoCommandEntry> {

    public AutoCommandEntryController(Option<ModConfig.AutoCommandConfig.AutoCommandEntry> option) {
        super(option, List.of(2, 1, 1), List.of(
                dummyController("text.sbutils.config.option.autoCommand.commands.command", StringControllerBuilder::create,
                        "",
                        () -> option.pendingValue().command,
                        (newValue) -> option.requestSet(new ModConfig.AutoCommandConfig.AutoCommandEntry(newValue, option.pendingValue().delay, option.pendingValue().enabled))
                ),
                dummyController("text.sbutils.config.option.autoCommand.commands.delay", (delay) -> DoubleFieldControllerBuilder.create(delay).min(1.0),
                        0.0,
                        () -> option.pendingValue().delay,
                        (newValue) -> option.requestSet(new ModConfig.AutoCommandConfig.AutoCommandEntry(option.pendingValue().command, newValue, option.pendingValue().enabled))
                ),
                dummyController("text.sbutils.config.option.autoCommand.commands.enabled", TickBoxControllerBuilder::create,
                        false,
                        () -> option.pendingValue().enabled,
                        (newValue) -> option.requestSet(new ModConfig.AutoCommandConfig.AutoCommandEntry(option.pendingValue().command, option.pendingValue().delay, newValue))
                )
        ));
    }
}
