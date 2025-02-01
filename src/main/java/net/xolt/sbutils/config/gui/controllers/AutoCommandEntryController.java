package net.xolt.sbutils.config.gui.controllers;

import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.string.StringController;
import dev.isxander.yacl.gui.controllers.string.number.DoubleFieldController;
import net.xolt.sbutils.config.ModConfig;

import java.util.List;

public class AutoCommandEntryController extends MultiValueController<ModConfig.AutoCommandConfig.AutoCommandEntry> {

    public AutoCommandEntryController(Option<ModConfig.AutoCommandConfig.AutoCommandEntry> option) {
        super(option, List.of(2, 1, 1), List.of(
                dummyController("text.sbutils.config.option.autoCommand.commands.command", StringController::new,
                        "",
                        () -> option.pendingValue().command,
                        (newValue) -> option.requestSet(new ModConfig.AutoCommandConfig.AutoCommandEntry(newValue, option.pendingValue().delay, option.pendingValue().enabled))
                ),
                dummyController("text.sbutils.config.option.autoCommand.commands.delay", (delay) -> new DoubleFieldController(delay, 1.0, Double.MAX_VALUE),
                        0.0,
                        () -> option.pendingValue().delay,
                        (newValue) -> option.requestSet(new ModConfig.AutoCommandConfig.AutoCommandEntry(option.pendingValue().command, newValue, option.pendingValue().enabled))
                ),
                dummyController("text.sbutils.config.option.autoCommand.commands.enabled", TickBoxController::new,
                        false,
                        () -> option.pendingValue().enabled,
                        (newValue) -> option.requestSet(new ModConfig.AutoCommandConfig.AutoCommandEntry(option.pendingValue().command, option.pendingValue().delay, newValue))
                )
        ));
    }
}
