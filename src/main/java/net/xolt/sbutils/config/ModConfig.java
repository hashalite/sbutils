package net.xolt.sbutils.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.impl.controller.ColorControllerBuilderImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.xolt.sbutils.config.KeyValueController.KeyValuePair;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModConfig {

    public static final ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(new Identifier("sbutils", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getGameDir().resolve("sbutils").resolve("sbutils.json"))
                    .appendGsonBuilder(builder -> builder.registerTypeHierarchyAdapter(KeyValueController.KeyValuePair.class, new KeyValueController.KeyValuePair.KeyValueTypeAdapter()))
                    .appendGsonBuilder(builder -> builder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .build()).build();

    // Mod Settings

    @SerialEntry public String prefixFormat = "%s »";
    @SerialEntry public Color sbutilsColor = new Color(Integer.valueOf("90e7fc", 16));
    @SerialEntry public Color prefixColor = new Color(Integer.valueOf("002c47", 16));
    @SerialEntry public Color messageColor = new Color(Integer.valueOf("b5b5b5", 16));
    @SerialEntry public Color valueColor = new Color(Integer.valueOf("ccf5ff", 16));

    @SerialEntry public AntiPlaceConfig antiPlace = new AntiPlaceConfig();
    public static class AntiPlaceConfig {
        @SerialEntry public boolean heads = false;
        @SerialEntry public boolean grass = false;
    }

    @SerialEntry public AutoAdvertConfig autoAdvert = new AutoAdvertConfig();
    public static class AutoAdvertConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public String sbFile = "skyblock";
        @SerialEntry public String ecoFile = "economy";
        @SerialEntry public String classicFile = "classic";
        @SerialEntry public double delay = 300.0;
        @SerialEntry public double initialDelay = 10.0;
        @SerialEntry public boolean useWhitelist = false;
        @SerialEntry public List<String> whitelist = List.of();
    }

    @SerialEntry public AutoCommandConfig autoCommand = new AutoCommandConfig();
    public static class AutoCommandConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public double minDelay = 1.5;
        @SerialEntry public List<KeyValuePair<String, KeyValuePair<Double, Boolean>>> commands = List.of(new KeyValuePair<>("", new KeyValuePair<>(5.0, false)));
    }

    @SerialEntry public AutoCrateConfig autoCrate = new AutoCrateConfig();
    public static class AutoCrateConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public Crate mode = Crate.COMMON;
        @SerialEntry public double delay = 0.25;
        @SerialEntry public double distance = 4.0;
    }

    @SerialEntry public AutoFixConfig autoFix = new AutoFixConfig();
    public static class AutoFixConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public FixMode mode = FixMode.HAND;
        @SerialEntry public double percent = 0.2;
        @SerialEntry public double delay = 120.0;
        @SerialEntry public double retryDelay = 3.0;
        @SerialEntry public int maxRetries = 3;
    }

    @SerialEntry public AutoKitConfig autoKit = new AutoKitConfig();
    public static class AutoKitConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public double commandDelay = 1.0;
        @SerialEntry public double claimDelay = 10.0;
        @SerialEntry public double systemDelay = 10.0;
        @SerialEntry public List<Kit> kits = List.of();
    }

    @SerialEntry public AutoMineConfig autoMine = new AutoMineConfig();
    public static class AutoMineConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public boolean autoSwitch = true;
        @SerialEntry public int switchDurability = 20;
    }

    @SerialEntry public AutoPrivateConfig autoPrivate = new AutoPrivateConfig();
    public static class AutoPrivateConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public List<String> names = new ArrayList<>();
    }

    @SerialEntry public AutoRaffleConfig autoRaffle = new AutoRaffleConfig();
    public static class AutoRaffleConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public int sbTickets = 2;
        @SerialEntry public int ecoTickets = 5;
    }

    @SerialEntry public AutoReplyConfig autoReply = new AutoReplyConfig();
    public static class AutoReplyConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public String response = "I am currently AFK. Please /mail me and I'll get back to you later!";
        @SerialEntry public double delay = 1.0;
    }

    @SerialEntry public AutoSilkConfig autoSilk = new AutoSilkConfig();
    public static class AutoSilkConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public SilkTarget targetTool = SilkTarget.DIAMOND_PICKAXE;
        @SerialEntry public double delay = 0.25;
        @SerialEntry public boolean showButton = true;
        @SerialEntry public CornerButtonPos buttonPos = CornerButtonPos.BOTTOM_LEFT;
    }

    @SerialEntry public ChatAppendConfig chatAppend = new ChatAppendConfig();
    public static class ChatAppendConfig {
        @SerialEntry public boolean addPrefix = false;
        @SerialEntry public String prefix = "";
        @SerialEntry public boolean addSuffix = false;
        @SerialEntry public String suffix = "";
    }

    @SerialEntry public ChatFiltersConfig chatFilters = new ChatFiltersConfig();
    public static class ChatFiltersConfig {
        @SerialEntry public boolean tipsFilter = false;
        @SerialEntry public boolean advancementsFilter = false;
        @SerialEntry public boolean welcomeFilter = false;
        @SerialEntry public boolean friendJoinFilter = false;
        @SerialEntry public boolean motdFilter = false;
        @SerialEntry public boolean voteFilter = false;
        @SerialEntry public boolean voteRewardFilter = false;
        @SerialEntry public boolean raffleFilter = false;
        @SerialEntry public boolean cratesFilter = false;
        @SerialEntry public boolean perishedInVoidFilter = false;
        @SerialEntry public boolean skyChatFilter = false;
    }

    @SerialEntry public ChatLoggerConfig chatLogger = new ChatLoggerConfig();
    public static class ChatLoggerConfig {
        @SerialEntry public boolean shopIncoming = false;
        @SerialEntry public boolean shopOutgoing = false;
        @SerialEntry public boolean msgIncoming = false;
        @SerialEntry public boolean msgOutgoing = false;
        @SerialEntry public boolean visits = false;
        @SerialEntry public boolean dp = false;
    }

    @SerialEntry public EnchantAllConfig enchantAll = new EnchantAllConfig();
    public static class EnchantAllConfig {
        @SerialEntry public EnchantMode mode = EnchantMode.ALL;
        @SerialEntry public double delay = 0.55;
        @SerialEntry public int cooldownFrequency = 12;
        @SerialEntry public double cooldownTime = 6.0;
        @SerialEntry public boolean excludeFrost = true;
    }

    @SerialEntry public EventNotifierConfig eventNotifier = new EventNotifierConfig();
    public static class EventNotifierConfig {
        @SerialEntry public boolean showLlamaTitle = false;
        @SerialEntry public boolean playLlamaSound = false;
        @SerialEntry public NotifSound llamaSound = NotifSound.DIDGERIDOO;
        @SerialEntry public boolean showTraderTitle = false;
        @SerialEntry public boolean playTraderSound = false;
        @SerialEntry public NotifSound traderSound = NotifSound.BANJO;
    }

    @SerialEntry public JoinCommandsConfig joinCommands = new JoinCommandsConfig();
    public static class JoinCommandsConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public double initialDelay = 0.0;
        @SerialEntry public double delay = 0.0;
    }

    @SerialEntry public MentionsConfig mentions = new MentionsConfig();
    public static class MentionsConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public boolean playSound = true;
        @SerialEntry public NotifSound sound = NotifSound.EXPERIENCE;
        @SerialEntry public boolean highlight = true;
        @SerialEntry public Color highlightColor = new Color(Integer.valueOf("fff700", 16));
        @SerialEntry public boolean excludeServerMsgs = true;
        @SerialEntry public boolean excludeSelfMsgs = true;
        @SerialEntry public boolean excludeSender = false;
        @SerialEntry public boolean currentAccount = true;
        @SerialEntry public List<String> aliases = List.of();
    }

    @SerialEntry public NoGmtConfig noGmt = new NoGmtConfig();
    public static class NoGmtConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public String timeZone = "";
        @SerialEntry public boolean showTimeZone = true;
    }

    @SerialEntry public StaffDetectorConfig staffDetector = new StaffDetectorConfig();
    public static class StaffDetectorConfig {
        @SerialEntry public boolean detectJoin = false;
        @SerialEntry public boolean detectLeave = false;
        @SerialEntry public boolean playSound = false;
        @SerialEntry public NotifSound sound = NotifSound.BIT;
    }

    @SerialEntry public ToolSaverConfig toolSaver = new ToolSaverConfig();
    public static class ToolSaverConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public int durability = 20;
    }

    public static class ColorArgumentType implements ArgumentType<Color> {

        private static final DynamicCommandExceptionType COLOR_PARSE_EXCEPTION = new DynamicCommandExceptionType(message -> new LiteralMessage("Could not parse color: " + message));

        public static ColorArgumentType color() {
            return new ColorArgumentType();
        }

        public static Color getColor(CommandContext<?> context, String id) {
            return context.getArgument(id, Color.class);
        }

        @Override public Color parse(StringReader reader) throws CommandSyntaxException {
            StringBuilder builder = new StringBuilder();
            while (reader.canRead() && reader.peek() != ' ')
                builder.append(reader.read());
            String text = builder.toString();
            String colorCode = text.startsWith("#") || text.startsWith("&") ? text.substring(1) : text;
            if (colorCode.length() == 1) {
                Formatting color = Formatting.byCode(colorCode.charAt(0));
                if (color == null || color.getColorValue() == null)
                    throw new CommandSyntaxException(COLOR_PARSE_EXCEPTION, () -> "Invalid color code \"" + colorCode + "\"");
                return new Color(color.getColorValue());
            }
            if (colorCode.length() == 6) {
                try {
                    int color = Integer.valueOf(colorCode, 16);
                    return new Color(color);
                } catch (NumberFormatException nfe) {
                    throw new CommandSyntaxException(COLOR_PARSE_EXCEPTION, () -> "Invalid hex color \"" + colorCode + "\"");
                }
            }
            throw new CommandSyntaxException(COLOR_PARSE_EXCEPTION, () -> "Invalid color format");
        }
    }

    public enum FixMode implements NameableEnum, StringIdentifiable {
        HAND("text.sbutils.config.option.autoFix.mode.hand"),
        ALL("text.sbutils.config.option.autoFix.mode.all");

        private final String name;

        FixMode(String name) {
            this.name = name;
        }

        public Text getDisplayName() {
            return Text.translatable(name);
        }

        public String asString() {
            return getDisplayName().getString();
        }

        public static class FixModeArgumentType extends EnumArgumentType<FixMode> {
            private FixModeArgumentType() {
                super(StringIdentifiable.createCodec(FixMode::values), FixMode::values);
            }

            public static FixModeArgumentType fixMode() {
                return new FixModeArgumentType();
            }

            public static FixMode getFixMode(CommandContext<?> context, String id) {
                return context.getArgument(id, FixMode.class);
            }
        }
    }

    public enum EnchantMode implements NameableEnum, StringIdentifiable {
        INDIVIDUAL("text.sbutils.config.option.enchantAll.mode.individual"),
        ALL("text.sbutils.config.option.enchantAll.mode.all");

        private final String name;

        EnchantMode(String name) {
            this.name = name;
        }

        public Text getDisplayName() {
            return Text.translatable(name);
        }

        public String asString() {
            return getDisplayName().getString();
        }

        public static class EnchantModeArgumentType extends EnumArgumentType<EnchantMode> {
            private EnchantModeArgumentType() {
                super(StringIdentifiable.createCodec(EnchantMode::values), EnchantMode::values);
            }

            public static EnchantModeArgumentType enchantMode() {
                return new EnchantModeArgumentType();
            }

            public static EnchantMode getEnchantMode(CommandContext<?> context, String id) {
                return context.getArgument(id, EnchantMode.class);
            }
        }
    }

    public enum SilkTarget implements NameableEnum, StringIdentifiable {
        DIAMOND_PICKAXE(Items.DIAMOND_PICKAXE),
        DIAMOND_AXE(Items.DIAMOND_AXE),
        DIAMOND_SHOVEL(Items.DIAMOND_SHOVEL),
        DIAMOND_HOE(Items.DIAMOND_HOE),
        SHEARS(Items.SHEARS);

        private final Item tool;

        SilkTarget(Item tool) {
            this.tool = tool;
        }

        public Item getTool() {
            return tool;
        }

        public Text getDisplayName() {
            return Text.translatable(tool.getTranslationKey());
        }

        @Override
        public String asString() {
            return Registries.ITEM.getId(tool).getPath();
        }

        public static class SilkTargetArgumentType extends EnumArgumentType<SilkTarget> {
            private SilkTargetArgumentType() {
                super(StringIdentifiable.createCodec(ModConfig.SilkTarget::values), ModConfig.SilkTarget::values);
            }

            public static SilkTargetArgumentType silkTarget() {
                return new SilkTargetArgumentType();
            }

            public static SilkTarget getSilkTarget(CommandContext<?> context, String id) {
                return context.getArgument(id, SilkTarget.class);
            }
        }
    }

    public enum CornerButtonPos implements NameableEnum, StringIdentifiable {
        TOP_LEFT("top_left"),
        TOP_RIGHT("top_right"),
        BOTTOM_LEFT("bottom_left"),
        BOTTOM_RIGHT("bottom_right");

        private final String name;

        CornerButtonPos(String name) {
            this.name = name;
        }

        public Text getDisplayName() {
            return Text.translatable(name);
        }

        @Override
        public String asString() {
            return getDisplayName().getString();
        }

        public static class CornerButtonPosArgumentType extends EnumArgumentType<CornerButtonPos> {
            private CornerButtonPosArgumentType() {
                super(StringIdentifiable.createCodec(ModConfig.CornerButtonPos::values), ModConfig.CornerButtonPos::values);
            }

            public static CornerButtonPosArgumentType cornerButtonPos() {
                return new CornerButtonPosArgumentType();
            }

            public static CornerButtonPos getCornerButtonPos(CommandContext<?> context, String id) {
                return context.getArgument(id, CornerButtonPos.class);
            }
        }
    }

    public enum NotifSound implements NameableEnum, StringIdentifiable {
        EXPERIENCE(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP.getId().toShortTranslationKey(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP),
        LAY_EGG(SoundEvents.ENTITY_CHICKEN_EGG.getId().toShortTranslationKey(), SoundEvents.ENTITY_CHICKEN_EGG),
        DISPENSER(SoundEvents.BLOCK_DISPENSER_FAIL.getId().toShortTranslationKey(), SoundEvents.BLOCK_DISPENSER_FAIL),
        BUTTON(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON.getId().toShortTranslationKey(), SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON),
        ANVIL_LAND(SoundEvents.BLOCK_ANVIL_LAND.getId().toShortTranslationKey(), SoundEvents.BLOCK_ANVIL_LAND),
        BANJO(SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value()),
        BASEDRUM(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM.value()),
        BASS(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_BASS.value()),
        BELL(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_BELL.value()),
        BIT(SoundEvents.BLOCK_NOTE_BLOCK_BIT.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_BIT.value()),
        CHIME(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value()),
        COW_BELL(SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL.value()),
        DIDGERIDOO(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value()),
        FLUTE(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value()),
        GUITAR(SoundEvents.BLOCK_NOTE_BLOCK_GUITAR.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_GUITAR.value()),
        HARP(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_HARP.value()),
        HAT(SoundEvents.BLOCK_NOTE_BLOCK_HAT.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_HAT.value()),
        IRON_XYLOPHONE(SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE.value()),
        PLING(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_PLING.value()),
        SNARE(SoundEvents.BLOCK_NOTE_BLOCK_SNARE.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_SNARE.value()),
        XYLOPHONE(SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE.value().getId().toShortTranslationKey(), SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE.value());

        private final String name;
        private final SoundEvent sound;

        NotifSound(String name, SoundEvent sound) {
            this.name = name;
            this.sound = sound;
        }

        public String asString() {
            return name;
        }

        public Text getDisplayName() {
            return Text.translatable(name);
        }

        public SoundEvent getSound() {
            return sound;
        }

        public static class NotifSoundArgumentType extends EnumArgumentType<NotifSound> {
            private NotifSoundArgumentType() {
                super(StringIdentifiable.createCodec(NotifSound::values), NotifSound::values);
            }

            public static NotifSoundArgumentType notifSound() {
                return new NotifSoundArgumentType();
            }

            public static NotifSound getNotifSound(CommandContext<?> context, String id) {
                return context.getArgument(id, NotifSound.class);
            }
        }
    }

    public enum Crate implements NameableEnum, StringIdentifiable {
        VOTER("text.sbutils.config.option.autoCrate.mode.voter"),
        COMMON("text.sbutils.config.option.autoCrate.mode.common"),
        RARE("text.sbutils.config.option.autoCrate.mode.rare"),
        EPIC("text.sbutils.config.option.autoCrate.mode.epic"),
        LEGENDARY("text.sbutils.config.option.autoCrate.mode.legendary");

        private final String name;

        Crate(String name) {
            this.name = name;
        }

        public String asString() {
            return getDisplayName().getString();
        }

        public Text getDisplayName() {
            return Text.translatable(name);
        }

        public static class CrateModeArgumentType extends EnumArgumentType<Crate> {
            private CrateModeArgumentType() {
                super(StringIdentifiable.createCodec(Crate::values), Crate::values);
            }

            public static CrateModeArgumentType crateMode() {
                return new CrateModeArgumentType();
            }

            public static Crate getCrateMode(CommandContext<?> context, String id) {
                return context.getArgument(id, Crate.class);
            }
        }
    }

    public enum Kit implements NameableEnum, StringIdentifiable {

        SKYTITAN("Skytitan", 24, List.of(
                new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64),
                new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64),
                new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.IRON_INGOT, 8), new ItemStack(Items.REDSTONE, 64), new ItemStack(Items.SAND, 8),
                new ItemStack(Items.GRASS_BLOCK, 15), new ItemStack(Items.DIAMOND, 3)
        )),
        SKYGOD("Skygod", 48, List.of(
                new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64),
                new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.IRON_INGOT, 16), new ItemStack(Items.REDSTONE, 64), new ItemStack(Items.SAND, 15),
                new ItemStack(Items.GRASS_BLOCK, 15), new ItemStack(Items.DIAMOND, 1)
        )),
        SKYLORD("Skylord", 48, List.of(
                new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64),
                new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.IRON_INGOT, 12), new ItemStack(Items.REDSTONE, 64), new ItemStack(Items.SAND, 10),
                new ItemStack(Items.GRASS_BLOCK, 10)
        )),
        SKYKING("Skyking", 72, List.of(
                new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64),
                new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.IRON_INGOT, 6), new ItemStack(Items.REDSTONE, 64), new ItemStack(Items.SAND, 5),
                new ItemStack(Items.GRASS_BLOCK, 10)
        )),
        SKYKNIGHT("Skyknight", 72, List.of(
                new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64),
                new ItemStack(Items.IRON_INGOT, 6), new ItemStack(Items.REDSTONE, 64)
        )),
        SPAWNER("Spawner", 168, List.of(
                new ItemStack(Items.SPAWNER, 1)
        )),
        IRON("Iron", 24, List.of(
                new ItemStack(Items.IRON_INGOT, 12)
        )),
        WOOL("Wool", 24, List.of(
                new ItemStack(Items.WHITE_WOOL, 64), new ItemStack(Items.ORANGE_WOOL, 64), new ItemStack(Items.MAGENTA_WOOL, 64), new ItemStack(Items.LIGHT_BLUE_WOOL, 64),
                new ItemStack(Items.YELLOW_WOOL, 64), new ItemStack(Items.LIME_WOOL, 64), new ItemStack(Items.PINK_WOOL, 64), new ItemStack(Items.GRAY_WOOL, 64),
                new ItemStack(Items.LIGHT_GRAY_WOOL, 64), new ItemStack(Items.CYAN_WOOL, 64), new ItemStack(Items.PURPLE_WOOL, 64), new ItemStack(Items.BLUE_WOOL, 64),
                new ItemStack(Items.BROWN_WOOL, 64), new ItemStack(Items.GREEN_WOOL, 64), new ItemStack(Items.RED_WOOL, 64), new ItemStack(Items.BLACK_WOOL, 64)
                )),
        WOOD("Wood", 24, List.of(
                new ItemStack(Items.OAK_PLANKS, 64), new ItemStack(Items.OAK_PLANKS, 64), new ItemStack(Items.OAK_PLANKS, 64), new ItemStack(Items.OAK_PLANKS, 64),
                new ItemStack(Items.OAK_PLANKS, 64), new ItemStack(Items.OAK_PLANKS, 64), new ItemStack(Items.OAK_PLANKS, 64), new ItemStack(Items.OAK_PLANKS, 64)
                )),
        COBBLE("Cobble", 24, List.of(
                new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64), new ItemStack(Items.COBBLESTONE, 64)
        ));

        private final String name;
        private final int cooldown;
        private final List<ItemStack> items;

        Kit(String name, int interval, List<ItemStack> items) {
            this.name = name;
            this.items = items;
            this.cooldown = interval;
        }

        public int getCooldown() {
            return cooldown;
        }

        public List<ItemStack> getItems() {
            return items;
        }

        public Text getDisplayName() {
            return Text.translatable(name);
        }

        @Override
        public String asString() {
            return getDisplayName().getString();
        }

        public static class KitArgumentType extends EnumArgumentType<Kit> {
            private KitArgumentType() {
                super(StringIdentifiable.createCodec(ModConfig.Kit::values), ModConfig.Kit::values);
            }

            public static KitArgumentType kit() {
                return new KitArgumentType();
            }

            public static Kit getKit(CommandContext<?> context, String id) {
                return context.getArgument(id, Kit.class);
            }
        }
    }
}
