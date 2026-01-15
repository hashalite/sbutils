package net.xolt.sbutils;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.xolt.sbutils.api.ApiClient;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.config.gui.ConfigGuiFactory;
import net.xolt.sbutils.feature.*;
import net.xolt.sbutils.feature.features.*;
import net.xolt.sbutils.systems.CommandSender;
import net.xolt.sbutils.systems.ServerDetector;
import net.xolt.sbutils.systems.TpsEstimator;
import net.xolt.sbutils.util.FileUtils;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//? }

import java.awt.*;
import java.util.List;

public class SbUtils implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("sbutils");
    public static final Minecraft MC = Minecraft.getInstance();
    public static final Features<ModConfig> FEATURES = new Features<>(ModConfig.HANDLER);
    public static final CommandSender COMMAND_SENDER = new CommandSender();
    public static final ServerDetector SERVER_DETECTOR = new ServerDetector();
    public static final TpsEstimator TPS_ESTIMATOR = new TpsEstimator();
    public static final ApiClient API_CLIENT = new ApiClient();
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
        FileUtils.createAll();
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
                new CommandAliases(),
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
        configKey = KeyBindingHelper.registerKeyBinding(createKeymapping("config", GLFW.GLFW_KEY_BACKSLASH));
        islandKey = KeyBindingHelper.registerKeyBinding(createKeymapping("island", GLFW.GLFW_KEY_UNKNOWN));
        ehomeKey = KeyBindingHelper.registerKeyBinding(createKeymapping("ehome", GLFW.GLFW_KEY_UNKNOWN));
        jumpKey = KeyBindingHelper.registerKeyBinding(createKeymapping("jump", GLFW.GLFW_KEY_UNKNOWN));
        backKey = KeyBindingHelper.registerKeyBinding(createKeymapping("back", GLFW.GLFW_KEY_UNKNOWN));
        craftKey = KeyBindingHelper.registerKeyBinding(createKeymapping("craft", GLFW.GLFW_KEY_UNKNOWN));
        echestKey = KeyBindingHelper.registerKeyBinding(createKeymapping("echest", GLFW.GLFW_KEY_UNKNOWN));
        trashKey = KeyBindingHelper.registerKeyBinding(createKeymapping("trash", GLFW.GLFW_KEY_UNKNOWN));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.consumeClick()) {
                MC.setScreen(GUI_FACTORY.getConfigScreen(MC.screen));
            }

            if (MC.getConnection() == null) {
                return;
            }

            while (islandKey.consumeClick()) {
                CommandSender.sendNow("is");
            }

            while (ehomeKey.consumeClick()) {
                CommandSender.sendNow("ehome");
            }

            while (jumpKey.consumeClick()) {
                CommandSender.sendNow("jump");
            }

            while (backKey.consumeClick()) {
                CommandSender.sendNow("back");
            }

            while (craftKey.consumeClick()) {
                CommandSender.sendNow("wb");
            }

            while (echestKey.consumeClick()) {
                CommandSender.sendNow("ec");
            }

            while (trashKey.consumeClick()) {
                CommandSender.sendNow("trash");
            }
        });
    }

    private static KeyMapping createKeymapping(String title, int key) {
        return new KeyMapping("key.sbutils." + title, InputConstants.Type.KEYSYM, key,
                //? if >=1.21.11
                new KeyMapping.Category(Identifier.parse(
                        "category.sbutils.sbutils"
                //? if >=1.21.11
                ))
        );
    }

    public static List<? extends ConfigBinding<ModConfig, ?>> getGlobalConfigBindings() {
        return List.of(prefixFormat, sbutilsColor, prefixColor, messageColor, valueColor);
    }
}
