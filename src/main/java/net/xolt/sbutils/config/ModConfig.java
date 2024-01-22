package net.xolt.sbutils.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
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

import java.util.ArrayList;
import java.util.List;

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

    @SerialEntry
    public String messagePrefix = "[@]";
    @SerialEntry public Color sbutilsColor = Color.YELLOW;
    @SerialEntry public Color prefixColor = Color.AQUA;
    @SerialEntry public Color messageColor = Color.AQUA;
    @SerialEntry public Color valueColor = Color.WHITE;


    // Auto Advert Settings

    @SerialEntry public boolean autoAdvert = false;
    @SerialEntry public String skyblockAdFile = "skyblock";
    @SerialEntry public String economyAdFile = "economy";
    @SerialEntry public String classicAdFile = "classic";
    @SerialEntry public double advertDelay = 300.0;
    @SerialEntry public double advertInitialDelay = 10.0;
    @SerialEntry public boolean advertUseWhitelist = false;
    @SerialEntry public List<String> advertWhitelist = List.of();


    // Join Commands Settings

    @SerialEntry public boolean joinCmdsEnabled = false;
    @SerialEntry public double joinCmdInitialDelay = 0.0;
    @SerialEntry public double joinCmdDelay = 0.0;


    // Mentions Settings

    @SerialEntry public boolean mentions = false;
    @SerialEntry public boolean playMentionSound = true;
    @SerialEntry public NotifSound mentionSound = NotifSound.EXPERIENCE;
    @SerialEntry public boolean mentionHighlight = true;
    @SerialEntry public Color highlightColor = Color.GOLD;
    @SerialEntry public boolean excludeServerMsgs = true;
    @SerialEntry public boolean excludeSelfMsgs = true;
    @SerialEntry public boolean excludeSender = false;
    @SerialEntry public boolean mentionsCurrentAccount = true;
    @SerialEntry public List<String> mentionsAliases = List.of();


    // No GMT Settings

    @SerialEntry public boolean noGMT = false;
    @SerialEntry public String timeZone = "";
    @SerialEntry public boolean showTimeZone = true;


    // Enchant All Settings

    @SerialEntry public EnchantMode enchantMode = EnchantMode.ALL;
    @SerialEntry public double enchantDelay = 0.55;
    @SerialEntry public int cooldownFrequency = 12;
    @SerialEntry public double cooldownTime = 6.0;
    @SerialEntry public boolean excludeFrost = true;


    // Chat Append Settings

    @SerialEntry public boolean addPrefix = false;
    @SerialEntry public String chatPrefix = "";
    @SerialEntry public boolean addSuffix = false;
    @SerialEntry public String chatSuffix = "";


    // Chat Filters Settings

    @SerialEntry public boolean tipsFilterEnabled = false;
    @SerialEntry public boolean advancementsFilterEnabled = false;
    @SerialEntry public boolean welcomeFilterEnabled = false;
    @SerialEntry public boolean friendJoinFilterEnabled = false;
    @SerialEntry public boolean motdFilterEnabled = false;
    @SerialEntry public boolean voteFilterEnabled = false;
    @SerialEntry public boolean voteRewardFilterEnabled = false;
    @SerialEntry public boolean raffleFilterEnabled = false;
    @SerialEntry public boolean cratesFilterEnabled = false;
    @SerialEntry public boolean perishedInVoidFilterEnabled = false;
    @SerialEntry public boolean skyChatFilterEnabled = false;


    // Chat Logger Settings

    @SerialEntry public boolean shopLoggerIncoming = false;
    @SerialEntry public boolean shopLoggerOutgoing = false;
    @SerialEntry public boolean msgLoggerIncoming = false;
    @SerialEntry public boolean msgLoggerOutgoing = false;
    @SerialEntry public boolean visitLogger = false;
    @SerialEntry public boolean dpLogger = false;


    // Event Notifier Settings

    @SerialEntry public boolean showLlamaTitle = false;
    @SerialEntry public boolean playLlamaSound = false;
    @SerialEntry public NotifSound llamaSound = NotifSound.DIDGERIDOO;
    @SerialEntry public boolean showTraderTitle = false;
    @SerialEntry public boolean playTraderSound = false;
    @SerialEntry public NotifSound traderSound = NotifSound.BANJO;


    // Auto Mine Settings

    @SerialEntry public boolean autoMine = false;
    @SerialEntry public boolean autoSwitch = true;
    @SerialEntry public int switchDurability = 20;


    // Auto Fix Settings

    @SerialEntry public boolean autoFix = false;
    @SerialEntry public FixMode autoFixMode = FixMode.HAND;
    @SerialEntry public double maxFixPercent = 0.2;
    @SerialEntry public double autoFixDelay = 120.0;
    @SerialEntry public double fixRetryDelay = 3.0;
    @SerialEntry public int maxFixRetries = 3;


    // Tool Saver Settings

    @SerialEntry public boolean toolSaver = false;
    @SerialEntry public int toolSaverDurability = 20;


    // Anti Place Settings

    @SerialEntry public boolean antiPlaceHeads = false;
    @SerialEntry public boolean antiPlaceGrass = false;


    // Auto Command Settings

    @SerialEntry public boolean autoCommandEnabled = false;
    @SerialEntry public double minAutoCommandDelay = 1.5;
    @SerialEntry public List<KeyValuePair<String, KeyValuePair<Double, Boolean>>> autoCommands = List.of(new KeyValuePair<>("", new KeyValuePair<>(5.0, false)));


    // Auto Reply Settings

    @SerialEntry public boolean autoReply = false;
    @SerialEntry public String autoResponse = "I am currently AFK. Please /mail me and I'll get back to you later!";
    @SerialEntry public double autoReplyDelay = 1.0;


    // Auto Raffle Settings

    @SerialEntry public boolean autoRaffle = false;
    @SerialEntry public int skyblockRaffleTickets = 2;
    @SerialEntry public int economyRaffleTickets = 5;
    @SerialEntry public double grassCheckDelay = 5.0;


    // Auto Private Settings

    @SerialEntry public boolean autoPrivate = false;
    @SerialEntry public List<String> autoPrivateNames = new ArrayList<>();


    // Auto Silk Settings

    @SerialEntry public boolean autoSilk = false;
    @SerialEntry public SilkTarget targetTool = SilkTarget.DIAMOND_PICKAXE;
    @SerialEntry public double autoSilkDelay = 0.25;
    @SerialEntry public boolean showSilkButton = true;
    @SerialEntry public CornerButtonPos silkButtonPos = CornerButtonPos.BOTTOM_LEFT;


    // Auto Crate Settings

    @SerialEntry public boolean autoCrate = false;
    @SerialEntry public CrateMode crateMode = CrateMode.COMMON;
    @SerialEntry public double crateDelay = 0.25;
    @SerialEntry public double crateDistance = 4.0;


    // Auto Kit Settings

    @SerialEntry public boolean autoKit = false;
    @SerialEntry public double autoKitCommandDelay = 1.0;
    @SerialEntry public double autoKitClaimDelay = 10.0;
    @SerialEntry public double autoKitSystemDelay = 10.0;
    @SerialEntry public List<Kit> autoKits = List.of();


    // Staff Detector Settings

    @SerialEntry public boolean detectStaffJoin = false;
    @SerialEntry public boolean detectStaffLeave = false;
    @SerialEntry public boolean playStaffSound = false;
    @SerialEntry public NotifSound staffDetectSound = NotifSound.BIT;


    public enum Color implements NameableEnum, StringIdentifiable {
        DARK_RED("text.sbutils.config.option.color.darkRed", Formatting.DARK_RED),
        RED("text.sbutils.config.option.color.red", Formatting.RED),
        GOLD("text.sbutils.config.option.color.gold", Formatting.GOLD),
        YELLOW("text.sbutils.config.option.color.yellow", Formatting.YELLOW),
        DARK_GREEN("text.sbutils.config.option.color.darkGreen", Formatting.DARK_GREEN),
        GREEN("text.sbutils.config.option.color.green", Formatting.GREEN),
        DARK_BLUE("text.sbutils.config.option.color.darkBlue", Formatting.DARK_BLUE),
        BLUE("text.sbutils.config.option.color.blue", Formatting.BLUE),
        CYAN("text.sbutils.config.option.color.cyan", Formatting.DARK_AQUA),
        AQUA("text.sbutils.config.option.color.aqua", Formatting.AQUA),
        PURPLE("text.sbutils.config.option.color.purple", Formatting.DARK_PURPLE),
        PINK("text.sbutils.config.option.color.pink", Formatting.LIGHT_PURPLE),
        WHITE("text.sbutils.config.option.color.white", Formatting.WHITE),
        LIGHT_GRAY("text.sbutils.config.option.color.lightGray", Formatting.GRAY),
        DARK_GRAY("text.sbutils.config.option.color.darkGray", Formatting.DARK_GRAY),
        BLACK("text.sbutils.config.option.color.black", Formatting.BLACK);

        private final String name;
        private final Formatting formatting;

        Color(String name, Formatting formatting) {
            this.name = name;
            this.formatting = formatting;
        }

        public String asString() {
            return getDisplayName().getString();
        }

        public Text getDisplayName() {
            return Text.translatable(name);
        }

        public Formatting getFormatting() {
            return formatting;
        }

        public static class ColorArgumentType extends EnumArgumentType<Color> {
            private ColorArgumentType() {
                super(StringIdentifiable.createCodec(Color::values), Color::values);
            }

            public static ColorArgumentType color() {
                return new ColorArgumentType();
            }

            public static Color getColor(CommandContext<?> context, String id) {
                return context.getArgument(id, Color.class);
            }
        }
    }

    public enum FixMode implements NameableEnum, StringIdentifiable {
        HAND("text.sbutils.config.option.autoFixMode.hand"),
        ALL("text.sbutils.config.option.autoFixMode.all");

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
        INDIVIDUAL("text.sbutils.config.option.enchantMode.individual"),
        ALL("text.sbutils.config.option.enchantMode.all");

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

    public enum CrateMode implements NameableEnum, StringIdentifiable {
        VOTER("text.sbutils.config.option.crateMode.voter"),
        COMMON("text.sbutils.config.option.crateMode.common"),
        RARE("text.sbutils.config.option.crateMode.rare"),
        EPIC("text.sbutils.config.option.crateMode.epic"),
        LEGENDARY("text.sbutils.config.option.crateMode.legendary");

        private final String name;

        CrateMode(String name) {
            this.name = name;
        }

        public String asString() {
            return getDisplayName().getString();
        }

        public Text getDisplayName() {
            return Text.translatable(name);
        }

        public static class CrateModeArgumentType extends EnumArgumentType<CrateMode> {
            private CrateModeArgumentType() {
                super(StringIdentifiable.createCodec(CrateMode::values), CrateMode::values);
            }

            public static CrateModeArgumentType crateMode() {
                return new CrateModeArgumentType();
            }

            public static CrateMode getCrateMode(CommandContext<?> context, String id) {
                return context.getArgument(id, CrateMode.class);
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
