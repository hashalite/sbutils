package net.xolt.sbutils.config.gui.controllers;

import dev.isxander.yacl3.api.Option;
import net.xolt.sbutils.config.ModConfig;
//? yacl: >=3.0.0 {
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
//? } else {
/*import dev.isxander.yacl3.gui.controllers.string.number.DoubleFieldController;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import dev.isxander.yacl3.gui.controllers.TickBoxController;
*///? }

import java.util.List;

public class AutoCommandEntryController extends MultiValueController<ModConfig.AutoCommandConfig.AutoCommandEntry> {

    public AutoCommandEntryController(Option<ModConfig.AutoCommandConfig.AutoCommandEntry> option) {
        super(option, List.of(2, 1, 1), List.of(
                dummyController("text.sbutils.config.option.autoCommand.commands.command",
                        //? yacl: >=3.0.0 {
                        StringControllerBuilder::create,
                        //? } else
                        //StringController::new,
                        "",
                        () -> option.pendingValue().command,
                        (newValue) -> option.requestSet(new ModConfig.AutoCommandConfig.AutoCommandEntry(newValue, option.pendingValue().delay, option.pendingValue().enabled))
                ),
                dummyController("text.sbutils.config.option.autoCommand.commands.delay",
                        //? yacl: >=3.0.0 {
                        (delay) -> DoubleFieldControllerBuilder.create(delay).min(1.0),
                        //? } else
                        //(delay) -> new DoubleFieldController(delay, 1.0, Double.MAX_VALUE),
                        0.0,
                        () -> option.pendingValue().delay,
                        (newValue) -> option.requestSet(new ModConfig.AutoCommandConfig.AutoCommandEntry(option.pendingValue().command, newValue, option.pendingValue().enabled))
                ),
                dummyController("text.sbutils.config.option.autoCommand.commands.enabled",
                        //? yacl: >=3.0.0 {
                        TickBoxControllerBuilder::create,
                        //? } else
                        //TickBoxController::new,
                        false,
                        () -> option.pendingValue().enabled,
                        (newValue) -> option.requestSet(new ModConfig.AutoCommandConfig.AutoCommandEntry(option.pendingValue().command, option.pendingValue().delay, newValue))
                )
        ));
    }
}
