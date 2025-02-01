package net.xolt.sbutils.config.gui.controllers;

import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.gui.controllers.string.StringController;
import net.xolt.sbutils.config.ModConfig;

import java.util.List;

public class JoinCommandsEntryController extends MultiValueController<ModConfig.JoinCommandsConfig.JoinCommandsEntry> {

    public JoinCommandsEntryController(Option<ModConfig.JoinCommandsConfig.JoinCommandsEntry> option) {
        super(option, List.of(3, 2), List.of(
                dummyController("text.sbutils.config.option.joinCommands.commands.command", StringController::new,
                        "",
                        () -> option.pendingValue().command,
                        (newValue) -> option.requestSet(new ModConfig.JoinCommandsConfig.JoinCommandsEntry(newValue, option.pendingValue().accounts))
                ),
                dummyController("text.sbutils.config.option.joinCommands.commands.accounts", StringController::new,
                        "",
                        () -> option.pendingValue().accounts,
                        (newValue) -> option.requestSet(new ModConfig.JoinCommandsConfig.JoinCommandsEntry(option.pendingValue().command, newValue))
                )
        ));
    }
}
