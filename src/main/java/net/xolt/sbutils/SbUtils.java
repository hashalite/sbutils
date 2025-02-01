package net.xolt.sbutils;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.config.gui.ConfigGuiFactory;
import net.xolt.sbutils.feature.*;
import net.xolt.sbutils.feature.features.*;
import net.xolt.sbutils.systems.CommandSender;
import net.xolt.sbutils.systems.ServerDetector;
import net.xolt.sbutils.systems.TpsEstimator;
import net.xolt.sbutils.util.IOHandler;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;

public class SbUtils implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("sbutils");
    public static final Minecraft MC = Minecraft.getInstance();
    public static final Features<ModConfig> FEATURES = new Features<>(ModConfig.HANDLER);
    public static final CommandSender COMMAND_SENDER = new CommandSender();
    public static final ServerDetector SERVER_DETECTOR = new ServerDetector();
    public static final TpsEstimator TPS_ESTIMATOR = new TpsEstimator();
    private static final OptionBinding<ModConfig, String> prefixFormat = new OptionBinding<>("sbutils", "prefixFormat", String.class, (config) -> config.prefixFormat, (config, value) -> config.prefixFormat = value);
    private static final OptionBinding<ModConfig, Color> sbutilsColor = new OptionBinding<>("sbutils", "sbutilsColor", Color.class, (config) -> config.sbutilsColor, (config, value) -> config.sbutilsColor = value);
    private static final OptionBinding<ModConfig, Color> prefixColor = new OptionBinding<>("sbutils", "prefixColor", Color.class, (config) -> config.prefixColor, (config, value) -> config.prefixColor = value);
    private static final OptionBinding<ModConfig, Color> messageColor = new OptionBinding<>("sbutils", "messageColor", Color.class, (config) -> config.messageColor, (config, value) -> config.messageColor = value);
    private static final OptionBinding<ModConfig, Color> valueColor = new OptionBinding<>("sbutils", "valueColor", Color.class, (config) -> config.valueColor, (config, value) -> config.valueColor = value);
    public static final ConfigGuiFactory<ModConfig> GUI_FACTORY = new ConfigGuiFactory<>("sbutils", FEATURES, List.of(prefixFormat, sbutilsColor, prefixColor, messageColor, valueColor));

    public static KeyMapping configKey;
    public static KeyMapping islandKey;
    public static KeyMapping ehomeKey;
    public static KeyMapping jumpKey;
    public static KeyMapping backKey;
    public static KeyMapping craftKey;
    public static KeyMapping echestKey;
    public static KeyMapping trashKey;

    @Override
    public void onInitializeClient() {
        IOHandler.createAll();
        ModConfig.HANDLER.load();
        initializeFeatures();
        ClientCommandRegistrationCallback.EVENT.register(FEATURES::registerCommands);
        registerKeybindings();
    }

    private static void initializeFeatures() {
        FEATURES.add(
                new AntiPlace(),
                new AutoAdvert(),
                new AutoCommand(),
                new AutoCrate(),
                new AutoFix(),
                new AutoKit(),
                new AutoMine(),
                new AutoPrivate(),
                new AutoRaffle(),
                new AutoReply(),
                new AutoSilk(),
                new Centered(),
                new ChatAppend(),
                new ChatFilters(),
                new ChatLogger(),
                new Convert(),
                new DeathCoords(),
                new EnchantAll(),
                new Notifier(),
                new InvCleaner(),
                new JoinCommands(),
                new MapSaver(),
                new Mentions(),
                new NoGMT(),
                new OpenFolder(),
                new StaffDetector(),
                new ToolSaver(),
                new UnenchantAll()
        );
    }

    private static void registerKeybindings() {
        configKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, "category.sbutils.sbutils"));
        islandKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.island", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        ehomeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.ehome", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        jumpKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.jump", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        backKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.back", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        craftKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.craft", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        echestKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.echest", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        trashKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.trash", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.consumeClick()) {
                MC.setScreen(GUI_FACTORY.getConfigScreen(MC.screen));
            }

            if (MC.getConnection() == null) {
                return;
            }

            assert MC.player != null;

            while (islandKey.consumeClick()) {
                MC.player.commandSigned("is", null);
            }

            while (ehomeKey.consumeClick()) {
                MC.player.commandSigned("ehome", null);
            }

            while (jumpKey.consumeClick()) {
                MC.player.commandSigned("jump", null);
            }

            while (backKey.consumeClick()) {
                MC.player.commandSigned("back", null);
            }

            while (craftKey.consumeClick()) {
                MC.player.commandSigned("wb", null);
            }

            while (echestKey.consumeClick()) {
                MC.player.commandSigned("ec", null);
            }

            while (trashKey.consumeClick()) {
                MC.player.commandSigned("trash", null);
            }
        });
    }

    public static List<? extends ConfigBinding<ModConfig, ?>> getGlobalConfigBindings() {
        return List.of(prefixFormat, sbutilsColor, prefixColor, messageColor, valueColor);
    }
}
