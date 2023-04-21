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

public class SbUtils implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("sbutils");
    public static final MinecraftClient MC = MinecraftClient.getInstance();

    public static KeyBinding configKey;
    public static KeyBinding islandKey;
    public static KeyBinding ehomeKey;
    public static KeyBinding jumpKey;
    public static KeyBinding backKey;
    public static KeyBinding craftKey;
    public static KeyBinding echestKey;

    @Override
    public void onInitializeClient() {
        IOHandler.createAll();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            EnchantAll.registerCommand(dispatcher);
            AutoAdvert.registerCommand(dispatcher);
            Convert.registerCommand(dispatcher);
            OpenFolder.registerCommand(dispatcher);
            AutoCrate.registerCommand(dispatcher);
            AutoPrivate.registerCommand(dispatcher);
            StaffDetector.registerCommand(dispatcher);
            ChatFilters.registerCommand(dispatcher);
            ChatLogger.registerCommand(dispatcher);
            JoinCommands.registerCommand(dispatcher);
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
        });

        registerKeybindings();

        ModConfig.INSTANCE.load();

        EnchantAll.init();
        AutoSilk.init();
        AutoFix.init();
        AutoReply.init();
    }

    private static void registerKeybindings() {
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, "category.sbutils.sbutils"));
        islandKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.island", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        ehomeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.ehome", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        jumpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.jump", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        backKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.back", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        craftKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.craft", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        echestKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sbutils.echest", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));

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
        });
    }
}
