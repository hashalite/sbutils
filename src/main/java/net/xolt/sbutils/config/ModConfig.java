package net.xolt.sbutils.config;

import com.mojang.brigadier.context.CommandContext;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler.EnumDisplayOption;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.xolt.sbutils.features.AutoFix;
import net.xolt.sbutils.features.AutoKit;
import net.xolt.sbutils.features.AutoPrivate;

import java.util.List;

@Config(name = "sbutils")
@Config.Gui.Background("minecraft:textures/block/deepslate_tiles.png")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    public static ModConfig INSTANCE;

    @ConfigEntry.Gui.Excluded
    public static ConfigHolder<ModConfig> HOLDER;

    public static void init() {
        AutoConfig.register(ModConfig.class, ModConfigSerializer::new);
        HOLDER = AutoConfig.getConfigHolder(ModConfig.class);
        HOLDER.registerSaveListener(ModConfig::onSave);
        INSTANCE = HOLDER.getConfig();
    }

    private static ActionResult onSave(ConfigHolder<ModConfig> modConfigConfigHolder, ModConfig modConfig) {
        AutoFix.onConfigSave(modConfig);
        AutoKit.onConfigSave(modConfig);
        AutoPrivate.onConfigSave(modConfig);

        return ActionResult.SUCCESS;
    }

    @ConfigEntry.Gui.Tooltip
    public String messagePrefix = "[@]";

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
    public Color sbutilsColor = Color.YELLOW;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
    public Color prefixColor = Color.AQUA;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
    public Color messageColor = Color.AQUA;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
    public Color valueColor = Color.WHITE;

    public List<String> stringList = List.of();

    @ConfigEntry.Category("gameplay")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public AntiPlaceConfig antiPlace = new AntiPlaceConfig();
    public static class AntiPlaceConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean antiPlaceHeads = false;

        @ConfigEntry.Gui.Tooltip
        public boolean antiPlaceGrass = false;
    }

    @ConfigEntry.Category("chat")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public AutoAdvertConfig autoAdvert = new AutoAdvertConfig();
    public static class AutoAdvertConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean enabled = false;

        @ConfigEntry.Gui.Tooltip
        public String skyblockAdFile = "skyblock";

        @ConfigEntry.Gui.Tooltip
        public String economyAdFile = "economy";

        @ConfigEntry.Gui.Tooltip
        public String classicAdFile = "classic";

        @ConfigEntry.Gui.Tooltip
        public double advertDelay = 300.0;

        @ConfigEntry.Gui.Tooltip
        public double advertInitialDelay = 10.0;

        @ConfigEntry.Gui.Tooltip
        public boolean advertUseWhitelist = false;

        @ConfigEntry.Gui.Tooltip
        public List<String> advertWhitelist = List.of();
    }

    @ConfigEntry.Category("command")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public AutoCommandConfig autoCommand = new AutoCommandConfig();
    public static class AutoCommandConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean autoCommandEnabled = false;

        @ConfigEntry.Gui.Tooltip
        public double minAutoCommandDelay = 1.5;

        @ConfigEntry.Gui.Tooltip
        public List<AutoCommandEntry> autoCommands = List.of(new AutoCommandEntry());

        public static class AutoCommandEntry {
            public String command;
            public double delay;
            public boolean enabled;

            public AutoCommandEntry() {
                this("", 5.0, false);
            }

            public AutoCommandEntry(String command, double delay, boolean enabled) {
                this.command = command;
                this.delay = delay;
                this.enabled = enabled;
            }
        }
    }

    @ConfigEntry.Category("world")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public AutoCrateConfig autoCrate = new AutoCrateConfig();
    public static class AutoCrateConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean autoCrate = false;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public CrateMode crateMode = CrateMode.COMMON;

        @ConfigEntry.Gui.Tooltip
        public double crateDelay = 0.25;

        @ConfigEntry.Gui.Tooltip
        public double crateDistance = 4.0;
    }

    @ConfigEntry.Category("command")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public AutoFixConfig autoFix = new AutoFixConfig();
    public static class AutoFixConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean autoFix = false;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public FixMode autoFixMode = FixMode.HAND;

        @ConfigEntry.Gui.Tooltip
        public double maxFixPercent = 0.2;

        @ConfigEntry.Gui.Tooltip
        public double autoFixDelay = 120.0;

        @ConfigEntry.Gui.Tooltip
        public double fixRetryDelay = 3.0;

        @ConfigEntry.Gui.Tooltip
        public int maxFixRetries = 3;
    }

    @ConfigEntry.Category("command")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public AutoKitConfig autoKit = new AutoKitConfig();
    public static class AutoKitConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean autoKit = false;

        @ConfigEntry.Gui.Tooltip
        public double autoKitCommandDelay = 1.0;

        @ConfigEntry.Gui.Tooltip
        public double autoKitClaimDelay = 10.0;

        @ConfigEntry.Gui.Tooltip
        public double autoKitSystemDelay = 10.0;

        @ConfigEntry.Gui.Tooltip
        public List<KitEntry> autoKits = List.of();

        public static class KitEntry {
            @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
            public Kit kit;

            public KitEntry() {
                this(Kit.SKYTITAN);
            }

            public KitEntry(Kit kit) {
                this.kit = kit;
            }

            @Override public int hashCode() {
                return kit.hashCode();
            }

            @Override public boolean equals(Object obj) {
                if (!(obj instanceof KitEntry))
                    return false;
                return kit.equals(((KitEntry)obj).kit);
            }
        }
    }

    @ConfigEntry.Category("world")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public AutoMineConfig autoMine = new AutoMineConfig();
    public static class AutoMineConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean autoMine = false;

        @ConfigEntry.Gui.Tooltip
        public boolean autoSwitch = true;

        @ConfigEntry.Gui.Tooltip
        public int switchDurability = 20;
    }

    @ConfigEntry.Category("world")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public AutoPrivateConfig autoPrivate = new AutoPrivateConfig();
    public static class AutoPrivateConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean autoPrivate = false;

        @ConfigEntry.Gui.Tooltip
        public List<String> autoPrivateNames = List.of();
    }

    @ConfigEntry.Category("command")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public AutoRaffleConfig autoRaffle = new AutoRaffleConfig();
    public static class AutoRaffleConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean autoRaffle = false;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 2)
        public int skyblockRaffleTickets = 2;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 5)
        public int economyRaffleTickets = 5;

        @ConfigEntry.Gui.Tooltip
        public double grassCheckDelay = 5.0;
    }

    @ConfigEntry.Category("chat")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public AutoReplyConfig autoReply = new AutoReplyConfig();
    public static class AutoReplyConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean autoReply = false;

        @ConfigEntry.Gui.Tooltip
        public String autoResponse = "I am currently AFK. Please /mail me and I'll get back to you later!";

        @ConfigEntry.Gui.Tooltip
        public double autoReplyDelay = 1.0;
    }

    @ConfigEntry.Category("world")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public AutoSilkConfig autoSilk = new AutoSilkConfig();
    public static class AutoSilkConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean autoSilk = false;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public SilkTarget targetTool = SilkTarget.DIAMOND_PICKAXE;

        @ConfigEntry.Gui.Tooltip
        public double autoSilkDelay = 0.25;

        @ConfigEntry.Gui.Tooltip
        public boolean showSilkButton = true;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public CornerButtonPos silkButtonPos = CornerButtonPos.BOTTOM_LEFT;
    }

    @ConfigEntry.Category("chat")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public ChatAppendConfig chatAppend = new ChatAppendConfig();
    public static class ChatAppendConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean addPrefix = false;

        @ConfigEntry.Gui.Tooltip
        public String chatPrefix = "";

        @ConfigEntry.Gui.Tooltip
        public boolean addSuffix = false;

        @ConfigEntry.Gui.Tooltip
        public String chatSuffix = "";
    }

    @ConfigEntry.Category("chat")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public ChatFiltersConfig chatFilters = new ChatFiltersConfig();
    public static class ChatFiltersConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean tipsFilterEnabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean advancementsFilterEnabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean welcomeFilterEnabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean friendJoinFilterEnabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean motdFilterEnabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean voteFilterEnabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean voteRewardFilterEnabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean raffleFilterEnabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean cratesFilterEnabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean perishedInVoidFilterEnabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean skyChatFilterEnabled = false;
    }

    @ConfigEntry.Category("chat")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public ChatLoggerConfig chatLogger = new ChatLoggerConfig();
    public static class ChatLoggerConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean shopLoggerIncoming = false;

        @ConfigEntry.Gui.Tooltip
        public boolean shopLoggerOutgoing = false;

        @ConfigEntry.Gui.Tooltip
        public boolean msgLoggerIncoming = false;

        @ConfigEntry.Gui.Tooltip
        public boolean msgLoggerOutgoing = false;

        @ConfigEntry.Gui.Tooltip
        public boolean visitLogger = false;

        @ConfigEntry.Gui.Tooltip
        public boolean dpLogger = false;
    }

    @ConfigEntry.Category("command")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public EnchantAllConfig enchantAll = new EnchantAllConfig();
    public static class EnchantAllConfig {
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public EnchantMode enchantMode = EnchantMode.ALL;

        @ConfigEntry.Gui.Tooltip
        public double enchantDelay = 0.55;

        @ConfigEntry.Gui.Tooltip
        public int cooldownFrequency = 12;

        @ConfigEntry.Gui.Tooltip
        public double cooldownTime = 6.0;

        @ConfigEntry.Gui.Tooltip
        public boolean excludeFrost = true;
    }

    @ConfigEntry.Category("notifications")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public EventNotifierConfig eventNotifier = new EventNotifierConfig();
    public static class EventNotifierConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean showLlamaTitle = false;

        @ConfigEntry.Gui.Tooltip
        public boolean playLlamaSound = false;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public NotifSound llamaSound = NotifSound.DIDGERIDOO;

        @ConfigEntry.Gui.Tooltip
        public boolean showTraderTitle = false;

        @ConfigEntry.Gui.Tooltip
        public boolean playTraderSound = false;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public NotifSound traderSound = NotifSound.BANJO;
    }

    @ConfigEntry.Category("command")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public JoinCommandsConfig joinCommands = new JoinCommandsConfig();
    public static class JoinCommandsConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean enabled = false;

        @ConfigEntry.Gui.Tooltip
        public double joinCmdInitialDelay = 0.0;

        @ConfigEntry.Gui.Tooltip
        public double joinCmdDelay = 0.0;
    }

    @ConfigEntry.Category("notifications")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public MentionsConfig mentions = new MentionsConfig();
    public static class MentionsConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean enabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean playMentionSound = true;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public NotifSound mentionSound = NotifSound.EXPERIENCE;

        @ConfigEntry.Gui.Tooltip
        public boolean mentionHighlight = true;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public Color highlightColor = Color.GOLD;

        @ConfigEntry.Gui.Tooltip
        public boolean excludeServerMsgs = true;

        @ConfigEntry.Gui.Tooltip
        public boolean excludeSelfMsgs = true;

        @ConfigEntry.Gui.Tooltip
        public boolean excludeSender = false;

        @ConfigEntry.Gui.Tooltip
        public boolean mentionsCurrentAccount = true;

        @ConfigEntry.Gui.Tooltip
        public List<String> mentionsAliases = List.of();
    }

    @ConfigEntry.Category("gameplay")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public NoGmtConfig noGmt = new NoGmtConfig();
    public static class NoGmtConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean noGMT = false;

        @ConfigEntry.Gui.Tooltip
        public String timeZone = "";

        @ConfigEntry.Gui.Tooltip
        public boolean showTimeZone = true;
    }

    @ConfigEntry.Category("notifications")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public StaffDetectorConfig staffDetector = new StaffDetectorConfig();
    public static class StaffDetectorConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean detectStaffJoin = false;

        @ConfigEntry.Gui.Tooltip
        public boolean detectStaffLeave = false;

        @ConfigEntry.Gui.Tooltip
        public boolean playStaffSound = false;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public NotifSound staffDetectSound = NotifSound.BIT;
    }

    @ConfigEntry.Category("gameplay")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public ToolSaverConfig toolSaver = new ToolSaverConfig();
    public static class ToolSaverConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean toolSaver = false;

        @ConfigEntry.Gui.Tooltip
        public int toolSaverDurability = 20;
    }

    public enum Color implements SelectionListEntry.Translatable, StringIdentifiable {
        DARK_RED("text.autoconfig.sbutils.option.color.darkRed", Formatting.DARK_RED),
        RED("text.autoconfig.sbutils.option.color.red", Formatting.RED),
        GOLD("text.autoconfig.sbutils.option.color.gold", Formatting.GOLD),
        YELLOW("text.autoconfig.sbutils.option.color.yellow", Formatting.YELLOW),
        DARK_GREEN("text.autoconfig.sbutils.option.color.darkGreen", Formatting.DARK_GREEN),
        GREEN("text.autoconfig.sbutils.option.color.green", Formatting.GREEN),
        DARK_BLUE("text.autoconfig.sbutils.option.color.darkBlue", Formatting.DARK_BLUE),
        BLUE("text.autoconfig.sbutils.option.color.blue", Formatting.BLUE),
        CYAN("text.autoconfig.sbutils.option.color.cyan", Formatting.DARK_AQUA),
        AQUA("text.autoconfig.sbutils.option.color.aqua", Formatting.AQUA),
        PURPLE("text.autoconfig.sbutils.option.color.purple", Formatting.DARK_PURPLE),
        PINK("text.autoconfig.sbutils.option.color.pink", Formatting.LIGHT_PURPLE),
        WHITE("text.autoconfig.sbutils.option.color.white", Formatting.WHITE),
        LIGHT_GRAY("text.autoconfig.sbutils.option.color.lightGray", Formatting.GRAY),
        DARK_GRAY("text.autoconfig.sbutils.option.color.darkGray", Formatting.DARK_GRAY),
        BLACK("text.autoconfig.sbutils.option.color.black", Formatting.BLACK);

        private final String name;
        private final Formatting formatting;

        Color(String name, Formatting formatting) {
            this.name = name;
            this.formatting = formatting;
        }

        public String asString() {
            return Text.translatable(name).getString();
        }

        public String getKey() {
            return name;
        }

        public Formatting getFormatting() {
            return formatting;
        }

        public static class ColorArgumentType extends EnumArgumentType<Color> {
            private ColorArgumentType() {
                super(StringIdentifiable.createCodec(Color::values), Color::values);
            }

            public static Color.ColorArgumentType color() {
                return new Color.ColorArgumentType();
            }

            public static Color getColor(CommandContext<?> context, String id) {
                return context.getArgument(id, Color.class);
            }
        }
    }

    public enum FixMode implements SelectionListEntry.Translatable, StringIdentifiable {
        HAND("text.autoconfig.sbutils.option.autoFix.autoFixMode.hand"),
        ALL("text.autoconfig.sbutils.option.autoFix.autoFixMode.all");

        private final String name;

        FixMode(String name) {
            this.name = name;
        }

        public String getKey() {
            return name;
        }

        public String asString() {
            return Text.translatable(name).getString();
        }

        public static class FixModeArgumentType extends EnumArgumentType<FixMode> {
            private FixModeArgumentType() {
                super(StringIdentifiable.createCodec(FixMode::values), FixMode::values);
            }

            public static FixMode.FixModeArgumentType fixMode() {
                return new FixMode.FixModeArgumentType();
            }

            public static FixMode getFixMode(CommandContext<?> context, String id) {
                return context.getArgument(id, FixMode.class);
            }
        }
    }

    public enum EnchantMode implements SelectionListEntry.Translatable, StringIdentifiable {
        INDIVIDUAL("text.autoconfig.sbutils.option.enchantAll.enchantMode.individual"),
        ALL("text.autoconfig.sbutils.option.enchantAll.enchantMode.all");

        private final String name;

        EnchantMode(String name) {
            this.name = name;
        }

        public String getKey() {
            return name;
        }

        public String asString() {
            return Text.translatable(name).getString();
        }

        public static class EnchantModeArgumentType extends EnumArgumentType<EnchantMode> {
            private EnchantModeArgumentType() {
                super(StringIdentifiable.createCodec(EnchantMode::values), EnchantMode::values);
            }

            public static EnchantMode.EnchantModeArgumentType enchantMode() {
                return new EnchantMode.EnchantModeArgumentType();
            }

            public static EnchantMode getEnchantMode(CommandContext<?> context, String id) {
                return context.getArgument(id, EnchantMode.class);
            }
        }
    }

    public enum SilkTarget implements SelectionListEntry.Translatable, StringIdentifiable {
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

        public String getKey() {
            return tool.getTranslationKey();
        }

        @Override
        public String asString() {
            return Registries.ITEM.getId(tool).getPath();
        }

        public static class SilkTargetArgumentType extends EnumArgumentType<SilkTarget> {
            private SilkTargetArgumentType() {
                super(StringIdentifiable.createCodec(SilkTarget::values), SilkTarget::values);
            }

            public static SilkTarget.SilkTargetArgumentType silkTarget() {
                return new SilkTarget.SilkTargetArgumentType();
            }

            public static SilkTarget getSilkTarget(CommandContext<?> context, String id) {
                return context.getArgument(id, SilkTarget.class);
            }
        }
    }

    public enum CornerButtonPos implements SelectionListEntry.Translatable, StringIdentifiable {
        TOP_LEFT("top_left"),
        TOP_RIGHT("top_right"),
        BOTTOM_LEFT("bottom_left"),
        BOTTOM_RIGHT("bottom_right");

        private final String name;

        CornerButtonPos(String name) {
            this.name = name;
        }

        public String getKey() {
            return name;
        }

        @Override
        public String asString() {
            return name;
        }

        public static class CornerButtonPosArgumentType extends EnumArgumentType<CornerButtonPos> {
            private CornerButtonPosArgumentType() {
                super(StringIdentifiable.createCodec(CornerButtonPos::values), CornerButtonPos::values);
            }

            public static CornerButtonPos.CornerButtonPosArgumentType cornerButtonPos() {
                return new CornerButtonPos.CornerButtonPosArgumentType();
            }

            public static CornerButtonPos getCornerButtonPos(CommandContext<?> context, String id) {
                return context.getArgument(id, CornerButtonPos.class);
            }
        }
    }

    public enum NotifSound implements SelectionListEntry.Translatable, StringIdentifiable {
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

        public String getKey() {
            return name;
        }

        public SoundEvent getSound() {
            return sound;
        }

        public static class NotifSoundArgumentType extends EnumArgumentType<NotifSound> {
            private NotifSoundArgumentType() {
                super(StringIdentifiable.createCodec(NotifSound::values), NotifSound::values);
            }

            public static NotifSound.NotifSoundArgumentType notifSound() {
                return new NotifSound.NotifSoundArgumentType();
            }

            public static NotifSound getNotifSound(CommandContext<?> context, String id) {
                return context.getArgument(id, NotifSound.class);
            }
        }
    }

    public enum CrateMode implements SelectionListEntry.Translatable, StringIdentifiable {
        VOTER("text.autoconfig.sbutils.option.autoCrate.crateMode.voter"),
        COMMON("text.autoconfig.sbutils.option.autoCrate.crateMode.common"),
        RARE("text.autoconfig.sbutils.option.autoCrate.crateMode.rare"),
        EPIC("text.autoconfig.sbutils.option.autoCrate.crateMode.epic"),
        LEGENDARY("text.autoconfig.sbutils.option.autoCrate.crateMode.legendary");

        private final String name;

        CrateMode(String name) {
            this.name = name;
        }

        public String asString() {
            return Text.translatable(name).getString();
        }

        public String getKey() {
            return name;
        }

        public static class CrateModeArgumentType extends EnumArgumentType<CrateMode> {
            private CrateModeArgumentType() {
                super(StringIdentifiable.createCodec(CrateMode::values), CrateMode::values);
            }

            public static CrateMode.CrateModeArgumentType crateMode() {
                return new CrateMode.CrateModeArgumentType();
            }

            public static CrateMode getCrateMode(CommandContext<?> context, String id) {
                return context.getArgument(id, CrateMode.class);
            }
        }
    }

    public enum Kit implements SelectionListEntry.Translatable, StringIdentifiable {

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

        public String getKey() {
            return name;
        }

        @Override
        public String asString() {
            return Text.translatable(name).getString();
        }

        public static class KitArgumentType extends EnumArgumentType<Kit> {
            private KitArgumentType() {
                super(StringIdentifiable.createCodec(Kit::values), Kit::values);
            }

            public static KitArgumentType kit() {
                return new Kit.KitArgumentType();
            }

            public static Kit getKit(CommandContext<?> context, String id) {
                return context.getArgument(id, Kit.class);
            }
        }
    }
}
