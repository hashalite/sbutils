package net.xolt.sbutils.config.gui.controllers;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.xolt.sbutils.config.ModConfig;

import java.util.List;

public class JoinCommandsEntryController extends MultiValueController<ModConfig.JoinCommandsConfig.JoinCommandsEntry> {

    public JoinCommandsEntryController(Option<ModConfig.JoinCommandsConfig.JoinCommandsEntry> option) {
        super(option, List.of(2, 1), List.of(
                dummyController("text.sbutils.config.option.joinCommands.commands.command", StringControllerBuilder::create,
                        "",
                        () -> option.pendingValue().command,
                        (newValue) -> option.requestSet(new ModConfig.JoinCommandsConfig.JoinCommandsEntry(newValue, option.pendingValue().accounts))
                ),
                dummyController("text.sbutils.config.option.joinCommands.commands.accounts", StringControllerBuilder::create,
                        "",
                        () -> option.pendingValue().accounts,
                        (newValue) -> option.requestSet(new ModConfig.JoinCommandsConfig.JoinCommandsEntry(option.pendingValue().command, newValue))
                )
        ));
    }
}
