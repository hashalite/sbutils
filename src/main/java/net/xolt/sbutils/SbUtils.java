package net.xolt.sbutils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.xolt.sbutils.config.ConfigGui;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.*;
import net.xolt.sbutils.util.IOHandler;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SbUtils implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("sbutils");
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final List<String> commands = new ArrayList<>();

    public static KeyBinding configKey;
    public static KeyBinding islandKey;
    public static KeyBinding ehomeKey;
    public static KeyBinding jumpKey;
    public static KeyBinding backKey;
    public static KeyBinding craftKey;
    public static KeyBinding echestKey;
    public static KeyBinding trashKey;

    @Override
    public void onInitializeClient() {
        IOHandler.createAll();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            EnchantAll.registerCommand(dispatcher);
            AutoAdvert.registerCommand(dispatcher);
            Convert.registerCommand(dispatcher);
            OpenFolder.registerCommand(dispatcher);
            AutoPrivate.registerCommand(dispatcher);
            StaffDetector.registerCommand(dispatcher);
            ChatFilters.registerCommand(dispatcher);
            ChatLogger.registerCommand(dispatcher);
            JoinCommands.registerCommand(dispatcher);
            AutoCrate.registerCommand(dispatcher);
            AutoSilk.registerCommand(dispatcher);
            Mentions.registerCommand(dispatcher);
            ToolSaver.registerCommand(dispatcher);
            AutoMine.registerCommand(dispatcher);
            AutoFix.registerCommand(dispatcher);
            AutoRaffle.registerCommand(dispatcher);
            ChatAppend.registerCommand(dispatcher);
            AutoReply.registerCommand(dispatcher);
            AutoCommand.registerCommand(dispatcher);
            AntiPlace.registerCommand(dispatcher);
            Centered.registerCommand(dispatcher);
            DeathCoords.registerCommand(dispatcher);
            EventNotifier.registerCommand(dispatcher);
            NoGMT.registerCommand(dispatcher);
            AutoKit.registerCommand(dispatcher);
        });

        registerKeybindings();

        ModConfig.HANDLER.load();

        EnchantAll.init();
        AutoSilk.init();
        AutoFix.init();
        AutoReply.init();
        AutoKit.init();
    }

    private static void registerKeybindings() {
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, "category.sbutils.sbutils"));
        islandKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.island", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        ehomeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.ehome", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        jumpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.jump", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        backKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.back", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        craftKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.craft", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        echestKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.echest", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        trashKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.trash", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.wasPressed()) {
                MC.setScreen(ConfigGui.getModConfigScreen(MC.currentScreen));
            }

            if (MC.getNetworkHandler() == null) {
                return;
            }

            while (islandKey.wasPressed()) {
                MC.getNetworkHandler().sendChatCommand("is");
            }

            while (ehomeKey.wasPressed()) {
                MC.getNetworkHandler().sendChatCommand("ehome");
            }

            while (jumpKey.wasPressed()) {
                MC.getNetworkHandler().sendChatCommand("jump");
            }

            while (backKey.wasPressed()) {
                MC.getNetworkHandler().sendChatCommand("back");
            }

            while (craftKey.wasPressed()) {
                MC.getNetworkHandler().sendChatCommand("wb");
            }

            while (echestKey.wasPressed()) {
                MC.getNetworkHandler().sendChatCommand("ec");
            }

            while (trashKey.wasPressed()) {
                MC.getNetworkHandler().sendChatCommand("trash");
            }
        });
    }
}
