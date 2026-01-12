package net.xolt.sbutils.config.gui.controllers;

import dev.isxander.yacl3.api.Option;
import net.xolt.sbutils.config.ModConfig;
//? yacl: >=3.0.0 {
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
//? } else {
/*import dev.isxander.yacl3.gui.controllers.string.StringController;
 *///? }

import java.util.List;

public class JoinCommandsEntryController extends MultiValueController<ModConfig.JoinCommandsConfig.JoinCommandsEntry> {

    public JoinCommandsEntryController(Option<ModConfig.JoinCommandsConfig.JoinCommandsEntry> option) {
        super(option, List.of(3, 2), List.of(
                dummyController("text.sbutils.config.option.joinCommands.commands.command",
                        //? yacl: >=3.0.0 {
                        StringControllerBuilder::create,
                         //? } else
                        //StringController::new,
                        "",
                        () -> option.pendingValue().command,
                        (newValue) -> option.requestSet(new ModConfig.JoinCommandsConfig.JoinCommandsEntry(newValue, option.pendingValue().accounts))
                ),
                dummyController("text.sbutils.config.option.joinCommands.commands.accounts",
                        //? yacl: >=3.0.0 {
                        StringControllerBuilder::create,
                         //? } else
                        //StringController::new,
                        "",
                        () -> option.pendingValue().accounts,
                        (newValue) -> option.requestSet(new ModConfig.JoinCommandsConfig.JoinCommandsEntry(option.pendingValue().command, newValue))
                )
        ));
    }
}
