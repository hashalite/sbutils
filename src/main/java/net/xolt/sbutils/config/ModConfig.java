package net.xolt.sbutils.config;

import dev.isxander.yacl.api.NameableEnum;
import dev.isxander.yacl.config.ConfigEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.yacl.CustomGsonConfigInstance;
import net.xolt.sbutils.feature.features.AutoCommand;
import net.xolt.sbutils.util.TextUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModConfig {

    public static final CustomGsonConfigInstance<ModConfig> HANDLER = new CustomGsonConfigInstance<>(ModConfig.class, FabricLoader.getInstance().getGameDir().resolve("sbutils").resolve("sbutils.json"));

    // Mod Settings

    @ConfigEntry
    public String prefixFormat = "%s »";
    @ConfigEntry public Color sbutilsColor = new Color(Integer.valueOf("90e7fc", 16));
    @ConfigEntry public Color prefixColor = new Color(Integer.valueOf("002c47", 16));
    @ConfigEntry public Color messageColor = new Color(Integer.valueOf("b5b5b5", 16));
    @ConfigEntry
    public Color valueColor = new Color(Integer.valueOf("ccf5ff", 16));

    @ConfigEntry public AntiPlaceConfig antiPlace = new AntiPlaceConfig();
    public static class AntiPlaceConfig {
        @ConfigEntry public boolean heads = false;
        @ConfigEntry public boolean grass = false;
    }

    @ConfigEntry public AutoAdvertConfig autoAdvert = new AutoAdvertConfig();
    public static class AutoAdvertConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public String sbFile = "skyblock";
        @ConfigEntry public double sbDelay = 300.0;
        @ConfigEntry public String ecoFile = "economy";
        @ConfigEntry public double ecoDelay = 600.0;
        @ConfigEntry public String classicFile = "classic";
        @ConfigEntry public double classicDelay = 300.0;
        @ConfigEntry public double initialDelay = 10.0;
        @ConfigEntry public boolean useWhitelist = false;
        @ConfigEntry public List<String> whitelist = new ArrayList<>();
    }

    @ConfigEntry public AutoCommandConfig autoCommand = new AutoCommandConfig();
    public static class AutoCommandConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public double minDelay = 1.5;
        @ConfigEntry public List<AutoCommandEntry> commands = Arrays.asList(new AutoCommandEntry("", 1.0, false));

        public static class AutoCommandEntry implements MultiValue {
            @ConfigEntry public String command;
            @ConfigEntry public double delay;
            @ConfigEntry public boolean enabled;

            public AutoCommandEntry(String command, double delay, boolean enabled) {
                this.command = command;
                this.delay = delay;
                this.enabled = enabled;
            }

            @Override public boolean equals(Object obj) {
                if (!(obj instanceof AutoCommandEntry other))
                    return false;
                return this.command.equals(other.command) && this.delay == other.delay && this.enabled == other.enabled;
            }

            @Override public MutableComponent format() {
                Long cmdLastSentAt = SbUtils.FEATURES.get(AutoCommand.class).getCmdLastSentAt(this);
                MutableComponent delayLeftText;
                if (!ModConfig.HANDLER.getConfig().autoCommand.enabled || !enabled || cmdLastSentAt == null) {
                    delayLeftText = Component.literal("N/A");
                } else {
                    long delayLeftMillis = (long)(delay * 1000.0) - (System.currentTimeMillis() - cmdLastSentAt);
                    double delayLeft = (double)Math.max(delayLeftMillis, 0) / 1000.0;
                    delayLeftText = Component.literal(TextUtils.formatTime(delayLeft));
                }
                return TextUtils.insertPlaceholders("message.sbutils.autoCommand.commandEntry", command, TextUtils.formatTime(delay), enabled, delayLeftText);
            }

            @Override public String toString() {
                return command;
            }
        }
    }

    @ConfigEntry public AutoCrateConfig autoCrate = new AutoCrateConfig();
    public static class AutoCrateConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public Crate mode = Crate.COMMON;
        @ConfigEntry public double delay = 0.25;
        @ConfigEntry public double distance = 4.0;
        @ConfigEntry public boolean cleaner = true;
        @ConfigEntry public List<String> itemsToClean = Arrays.asList("cobblestone");
    }

    @ConfigEntry public AutoFixConfig autoFix = new AutoFixConfig();
    public static class AutoFixConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public FixMode mode = FixMode.HAND;
        @ConfigEntry public double percent = 0.2;
        @ConfigEntry public double delay = 120.0;
        @ConfigEntry public double retryDelay = 3.0;
        @ConfigEntry public int maxRetries = 3;
    }

    @ConfigEntry public AutoKitConfig autoKit = new AutoKitConfig();
    public static class AutoKitConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public double commandDelay = 1.0;
        @ConfigEntry public double claimDelay = 10.0;
        @ConfigEntry public double systemDelay = 10.0;
        @ConfigEntry public List<SkyblockKit> sbKits = new ArrayList<>();
        @ConfigEntry public List<EconomyKit> ecoKits = new ArrayList<>();
        @ConfigEntry public List<ClassicKit> classicKits = new ArrayList<>();
    }

    @ConfigEntry public AutoMineConfig autoMine = new AutoMineConfig();
    public static class AutoMineConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public boolean autoSwitch = true;
        @ConfigEntry public int switchDurability = 20;
    }

    @ConfigEntry public AutoPrivateConfig autoPrivate = new AutoPrivateConfig();
    public static class AutoPrivateConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public List<String> names = new ArrayList<>();
    }

    @ConfigEntry public AutoRaffleConfig autoRaffle = new AutoRaffleConfig();
    public static class AutoRaffleConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public int sbTickets = 2;
        @ConfigEntry public int ecoTickets = 5;
    }

    @ConfigEntry public AutoReplyConfig autoReply = new AutoReplyConfig();
    public static class AutoReplyConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public String response = "I am currently AFK. Please /mail me and I'll get back to you later!";
        @ConfigEntry public double delay = 1.0;
    }

    @ConfigEntry public AutoSilkConfig autoSilk = new AutoSilkConfig();
    public static class AutoSilkConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public SilkTarget targetTool = SilkTarget.DIAMOND_PICKAXE;
        @ConfigEntry public boolean bookPriority = false;
        @ConfigEntry public boolean booksOnly = false;
        @ConfigEntry public boolean cleaner = true;
        @ConfigEntry public double delay = 0.25;
        @ConfigEntry public boolean showButton = true;
        @ConfigEntry public CornerButtonPos buttonPos = CornerButtonPos.BOTTOM_LEFT;
    }

    @ConfigEntry public ChatAppendConfig chatAppend = new ChatAppendConfig();
    public static class ChatAppendConfig {
        @ConfigEntry public boolean addPrefix = false;
        @ConfigEntry public String prefix = "";
        @ConfigEntry public boolean addSuffix = false;
        @ConfigEntry public String suffix = "";
    }

    @ConfigEntry public ChatFiltersConfig chatFilters = new ChatFiltersConfig();
    public static class ChatFiltersConfig {
        @ConfigEntry public boolean tipsFilter = false;
        @ConfigEntry public boolean advancementsFilter = false;
        @ConfigEntry public boolean welcomeFilter = false;
        @ConfigEntry public boolean friendJoinFilter = false;
        @ConfigEntry public boolean motdFilter = false;
        @ConfigEntry public boolean islandTitleFilter = false;
        @ConfigEntry public boolean islandWelcomeFilter = false;
        @ConfigEntry public boolean voteFilter = false;
        @ConfigEntry public boolean voteRewardFilter = false;
        @ConfigEntry public boolean raffleFilter = false;
        @ConfigEntry public boolean cratesFilter = false;
        @ConfigEntry public boolean perishedInVoidFilter = false;
        @ConfigEntry public boolean skyChatFilter = false;

        @ConfigEntry public List<CustomFilter> customFilters = new ArrayList<>();

        public static class CustomFilter implements MultiValue {
            @ConfigEntry public String regex;
            @ConfigEntry public FilterTarget target;
            @ConfigEntry public boolean enabled;

            public CustomFilter(String regex, FilterTarget target, boolean enabled) {
                this.regex = regex;
                this.target = target;
                this.enabled = enabled;
            }

            @Override
            public MutableComponent format() {
                return TextUtils.insertPlaceholders("message.sbutils.chatFilter.filterEntry", regex, target, enabled);
            }
        }
    }

    @ConfigEntry public ChatLoggerConfig chatLogger = new ChatLoggerConfig();
    public static class ChatLoggerConfig {
        @ConfigEntry public boolean shopIncoming = false;
        @ConfigEntry public boolean shopOutgoing = false;
        @ConfigEntry public boolean msgIncoming = false;
        @ConfigEntry public boolean msgOutgoing = false;
        @ConfigEntry public boolean visits = false;
        @ConfigEntry public boolean dp = false;
    }

    @ConfigEntry public EnchantAllConfig enchantAll = new EnchantAllConfig();
    public static class EnchantAllConfig {
        @ConfigEntry public EnchantMode mode = EnchantMode.ALL;
        @ConfigEntry public boolean tpsSync = true;
        @ConfigEntry public double delay = 0.55;
        @ConfigEntry public int cooldownFrequency = 12;
        @ConfigEntry public double cooldownTime = 6.0;
        @ConfigEntry public boolean excludeFrost = true;
    }

    @ConfigEntry public NotifierConfig notifier = new NotifierConfig();
    public static class NotifierConfig {
        @ConfigEntry public boolean showLlamaTitle = false;
        @ConfigEntry public boolean playLlamaSound = false;
        @ConfigEntry public NotifSound llamaSound = NotifSound.DIDGERIDOO;
        @ConfigEntry public boolean showTraderTitle = false;
        @ConfigEntry public boolean showTraderItems = false;
        @ConfigEntry public boolean showTradesOnClick = false;
        @ConfigEntry public boolean playTraderSound = false;
        @ConfigEntry public NotifSound traderSound = NotifSound.BANJO;
        @ConfigEntry public boolean playShopSound = false;
        @ConfigEntry public NotifSound shopSound = NotifSound.DISPENSER;
        @ConfigEntry public boolean playVisitSound = false;
        @ConfigEntry public NotifSound visitSound = NotifSound.COW_BELL;
    }

    @ConfigEntry public InvCleanerConfig invCleaner = new InvCleanerConfig();
    public static class InvCleanerConfig {
        @ConfigEntry public double clickDelay = 0.0;
        @ConfigEntry public List<String> itemsToClean = Arrays.asList("cobblestone");
    }

    @ConfigEntry public JoinCommandsConfig joinCommands = new JoinCommandsConfig();
    public static class JoinCommandsConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public double initialDelay = 0.0;
        @ConfigEntry public double delay = 0.0;
        @ConfigEntry public List<JoinCommandsEntry> commands = new ArrayList<>();

        public static class JoinCommandsEntry implements MultiValue {
            @ConfigEntry public String command;
            @ConfigEntry public String accounts;

            public JoinCommandsEntry(String command, String accounts) {
                this.command = command;
                this.accounts = accounts;
            }

            @Override public boolean equals(Object obj) {
                if (!(obj instanceof JoinCommandsConfig.JoinCommandsEntry other))
                    return false;
                return this.command.equals(other.command) && this.accounts.equals(other.accounts);
            }

            @Override public MutableComponent format() {
                return TextUtils.insertPlaceholders("message.sbutils.joinCommands.commandEntry", command, formatAccounts());
            }

            public List<String> getAccounts() {
                return Arrays.asList(accounts.replaceAll(" ", "").split(",")).stream().filter((account) -> !account.isEmpty()).toList();
            }

            public MutableComponent formatAccounts() {
                List<String> accountList = getAccounts();
                if (accountList.isEmpty())
                    return Component.literal("*");
                return Component.literal(String.join(", ", getAccounts()));
            }

            @Override public String toString() {
                return command;
            }
        }
    }

    @ConfigEntry public MentionsConfig mentions = new MentionsConfig();
    public static class MentionsConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public boolean playSound = true;
        @ConfigEntry public NotifSound sound = NotifSound.EXPERIENCE;
        @ConfigEntry public boolean highlight = true;
        @ConfigEntry public Color highlightColor = new Color(Integer.valueOf("fff700", 16));
        @ConfigEntry public boolean excludeServerMsgs = true;
        @ConfigEntry public boolean excludeSelfMsgs = true;
        @ConfigEntry public boolean excludeSender = false;
        @ConfigEntry public boolean currentAccount = true;
        @ConfigEntry public List<String> aliases = new ArrayList<>();
    }

    @ConfigEntry public NoGmtConfig noGmt = new NoGmtConfig();
    public static class NoGmtConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public String timeZone = "";
        @ConfigEntry public boolean showTimeZone = true;
    }

    @ConfigEntry public StaffDetectorConfig staffDetector = new StaffDetectorConfig();
    public static class StaffDetectorConfig {
        @ConfigEntry public boolean detectJoin = false;
        @ConfigEntry public boolean detectLeave = false;
        @ConfigEntry public boolean playSound = false;
        @ConfigEntry public NotifSound sound = NotifSound.BIT;
    }

    @ConfigEntry public ToolSaverConfig toolSaver = new ToolSaverConfig();
    public static class ToolSaverConfig {
        @ConfigEntry public boolean enabled = false;
        @ConfigEntry public int durability = 20;
    }

    public enum FixMode implements NameableEnum, StringRepresentable {
        HAND("text.sbutils.config.option.autoFix.mode.hand"),
        ALL("text.sbutils.config.option.autoFix.mode.all");

        private final String name;

        FixMode(String name) {
            this.name = name;
        }

        public Component getDisplayName() {
            return Component.translatable(name);
        }

        public String getSerializedName() {
            return getDisplayName().getString();
        }
    }

    public enum EnchantMode implements NameableEnum, StringRepresentable {
        INDIVIDUAL("text.sbutils.config.option.enchantAll.mode.individual"),
        ALL("text.sbutils.config.option.enchantAll.mode.all");

        private final String name;

        EnchantMode(String name) {
            this.name = name;
        }

        public Component getDisplayName() {
            return Component.translatable(name);
        }

        public String getSerializedName() {
            return getDisplayName().getString();
        }
    }

    public enum SilkTarget implements NameableEnum, StringRepresentable {
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

        public Component getDisplayName() {
            return Component.translatable(tool.getDescriptionId());
        }

        @Override
        public String getSerializedName() {
            return Registry.ITEM.getKey(tool).getPath();
        }
    }

    public enum CornerButtonPos implements NameableEnum, StringRepresentable {
        TOP_LEFT("top_left"),
        TOP_RIGHT("top_right"),
        BOTTOM_LEFT("bottom_left"),
        BOTTOM_RIGHT("bottom_right");

        private final String name;

        CornerButtonPos(String name) {
            this.name = name;
        }

        public Component getDisplayName() {
            return Component.translatable(name);
        }

        @Override
        public String getSerializedName() {
            return getDisplayName().getString();
        }
    }

    public enum NotifSound implements NameableEnum, StringRepresentable {
        EXPERIENCE(SoundEvents.EXPERIENCE_ORB_PICKUP),
        LAY_EGG(SoundEvents.CHICKEN_EGG),
        DISPENSER(SoundEvents.DISPENSER_FAIL),
        BUTTON(SoundEvents.STONE_BUTTON_CLICK_ON),
        ANVIL_LAND(SoundEvents.ANVIL_LAND),
        BANJO(SoundEvents.NOTE_BLOCK_BANJO),
        BASEDRUM(SoundEvents.NOTE_BLOCK_BASEDRUM),
        BASS(SoundEvents.NOTE_BLOCK_BASS),
        BELL(SoundEvents.NOTE_BLOCK_BELL),
        BIT(SoundEvents.NOTE_BLOCK_BIT),
        CHIME(SoundEvents.NOTE_BLOCK_CHIME),
        COW_BELL(SoundEvents.NOTE_BLOCK_COW_BELL),
        DIDGERIDOO(SoundEvents.NOTE_BLOCK_DIDGERIDOO),
        FLUTE(SoundEvents.NOTE_BLOCK_FLUTE),
        GUITAR(SoundEvents.NOTE_BLOCK_GUITAR),
        HARP(SoundEvents.NOTE_BLOCK_HARP),
        HAT(SoundEvents.NOTE_BLOCK_HAT),
        IRON_XYLOPHONE(SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE),
        PLING(SoundEvents.NOTE_BLOCK_PLING),
        SNARE(SoundEvents.NOTE_BLOCK_SNARE),
        XYLOPHONE(SoundEvents.NOTE_BLOCK_XYLOPHONE);
        private final SoundEvent sound;

        NotifSound(SoundEvent sound) {
            this.sound = sound;
        }

        public String getSerializedName() {
            return sound.getLocation().toShortLanguageKey();
        }

        public Component getDisplayName() {
            return Component.literal(getSerializedName());
        }

        public SoundEvent getSound() {
            return sound;
        }
    }

    public enum Crate implements NameableEnum, StringRepresentable {
        VOTER("text.sbutils.config.option.autoCrate.mode.voter"),
        COMMON("text.sbutils.config.option.autoCrate.mode.common"),
        RARE("text.sbutils.config.option.autoCrate.mode.rare"),
        EPIC("text.sbutils.config.option.autoCrate.mode.epic"),
        LEGENDARY("text.sbutils.config.option.autoCrate.mode.legendary");

        private final String name;

        Crate(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return getDisplayName().getString();
        }

        public Component getDisplayName() {
            return Component.translatable(name);
        }
    }

    public interface Kit extends NameableEnum, StringRepresentable {
        int getCooldown();

        List<ItemStack> getItems();
    }

    public enum SkyblockKit implements Kit {

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
        )),
        DAILY("Daily", 24, List.of(new ItemStack(Items.GRASS_BLOCK, 1)));

        private final String name;
        private final int cooldown;
        private final List<ItemStack> items;

        SkyblockKit(String name, int interval, List<ItemStack> items) {
            this.name = name;
            this.items = items;
            this.cooldown = interval;
        }

        @Override
        public int getCooldown() {
            return cooldown;
        }

        @Override
        public List<ItemStack> getItems() {
            return items;
        }

        @Override
        public Component getDisplayName() {
            return Component.literal(name);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum EconomyKit implements Kit {
        HIGHROLLER("Highroller", SkyblockKit.SKYGOD),
        ELITE("Elite", SkyblockKit.SKYLORD),
        VIP("Vip", SkyblockKit.SKYKING),
        PREMIUM("Premium", SkyblockKit.SKYKNIGHT),
        SPAWNER(SkyblockKit.SPAWNER),
        IRON(SkyblockKit.IRON),
        WOOL(SkyblockKit.WOOL),
        WOOD(SkyblockKit.WOOD),
        COBBLE(SkyblockKit.COBBLE),
        DAILY("Daily", 24, List.of());

        private final String name;
        private final int cooldown;
        private final List<ItemStack> items;

        EconomyKit(SkyblockKit kit) {
            this(kit.name, kit.cooldown, kit.items);
        }

        EconomyKit(String name, SkyblockKit kit) {
            this(name, kit.cooldown, kit.items);
        }

        EconomyKit(String name, int interval, List<ItemStack> items) {
            this.name = name;
            this.items = items;
            this.cooldown = interval;
        }

        @Override
        public int getCooldown() {
            return cooldown;
        }

        @Override
        public List<ItemStack> getItems() {
            return items;
        }

        @Override
        public Component getDisplayName() {
            return Component.literal(name);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum ClassicKit implements Kit {

        DONOR250("Donor250", SkyblockKit.SKYTITAN),
        DONOR100("Donor100", SkyblockKit.SKYGOD),
        DONOR50("Donor50", SkyblockKit.SKYLORD),
        DONOR25("Donor25", SkyblockKit.SKYKING),
        DONOR10("Donor10", SkyblockKit.SKYKNIGHT),
        SPAWNER(SkyblockKit.SPAWNER),
        IRON(SkyblockKit.IRON),
        WOOL(SkyblockKit.WOOL),
        WOOD(SkyblockKit.WOOD),
        COBBLE(SkyblockKit.COBBLE);

        private final String name;
        private final int cooldown;
        private final List<ItemStack> items;

        ClassicKit(Kit kit) {
            this(kit.getSerializedName(), kit.getCooldown(), kit.getItems());
        }

        ClassicKit(String name, Kit kit) {
            this(name, kit.getCooldown(), kit.getItems());
        }

        ClassicKit(String name, int interval, List<ItemStack> items) {
            this.name = name;
            this.items = items;
            this.cooldown = interval;
        }

        @Override
        public int getCooldown() {
            return cooldown;
        }

        @Override
        public List<ItemStack> getItems() {
            return items;
        }

        @Override
        public Component getDisplayName() {
            return Component.literal(name);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum FilterTarget implements NameableEnum, StringRepresentable {
        CHAT,
        TITLE;

        @Override
        public Component getDisplayName() {
            return Component.literal(this.name());
        }

        @Override
        public String getSerializedName() {
            return this.name();
        }
    }

    public interface MultiValue {

        MutableComponent format();
    }
}
