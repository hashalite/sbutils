package net.xolt.sbutils.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xolt.sbutils.features.AutoCommand;
import net.xolt.sbutils.util.Messenger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModConfig {

    public static final ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(new ResourceLocation("sbutils", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getGameDir().resolve("sbutils").resolve("sbutils.json"))
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
        @SerialEntry public List<String> whitelist = new ArrayList<>();
    }

    @SerialEntry public AutoCommandConfig autoCommand = new AutoCommandConfig();
    public static class AutoCommandConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public double minDelay = 1.5;
        @SerialEntry public List<AutoCommandEntry> commands = Arrays.asList(new AutoCommandEntry("", 1.0, false));

        public static class AutoCommandEntry implements MultiValue {
            @SerialEntry public String command;
            @SerialEntry public double delay;
            @SerialEntry public boolean enabled;

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
                Long cmdLastSentAt = AutoCommand.getCmdsLastSentAt().get(this);
                MutableComponent delayLeftText;
                if (!ModConfig.HANDLER.instance().autoCommand.enabled || !enabled || cmdLastSentAt == null) {
                    delayLeftText = Component.literal("N/A");
                } else {
                    long delayLeftMillis = (long)(delay * 1000.0) - (System.currentTimeMillis() - cmdLastSentAt);
                    double delayLeft = (double)Math.max(delayLeftMillis, 0) / 1000.0;
                    delayLeftText = Component.literal(Messenger.formatTime(delayLeft));
                }
                return Messenger.insertPlaceholders("message.sbutils.autoCommand.commandEntry", command, Messenger.formatTime(delay), enabled, delayLeftText);
            }

            @Override public String toString() {
                return command;
            }
        }
    }

    @SerialEntry public AutoCrateConfig autoCrate = new AutoCrateConfig();
    public static class AutoCrateConfig {
        @SerialEntry public boolean enabled = false;
        @SerialEntry public Crate mode = Crate.COMMON;
        @SerialEntry public double delay = 0.25;
        @SerialEntry public double distance = 4.0;
        @SerialEntry public boolean cleaner = true;
        @SerialEntry public List<String> itemsToClean = Arrays.asList("cobblestone");
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
        @SerialEntry public List<Kit> kits = new ArrayList<>();
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
        @SerialEntry public boolean cleaner = true;
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
        @SerialEntry public List<JoinCommandsEntry> commands = new ArrayList<>();

        public static class JoinCommandsEntry implements MultiValue {
            @SerialEntry public String command;
            @SerialEntry public String accounts;

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
                return Messenger.insertPlaceholders("message.sbutils.joinCommands.commandEntry", command, formatAccounts());
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
        @SerialEntry public List<String> aliases = new ArrayList<>();
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
            return BuiltInRegistries.ITEM.getKey(tool).getPath();
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
        BANJO(SoundEvents.NOTE_BLOCK_BANJO.value()),
        BASEDRUM(SoundEvents.NOTE_BLOCK_BASEDRUM.value()),
        BASS(SoundEvents.NOTE_BLOCK_BASS.value()),
        BELL(SoundEvents.NOTE_BLOCK_BELL.value()),
        BIT(SoundEvents.NOTE_BLOCK_BIT.value()),
        CHIME(SoundEvents.NOTE_BLOCK_CHIME.value()),
        COW_BELL(SoundEvents.NOTE_BLOCK_COW_BELL.value()),
        DIDGERIDOO(SoundEvents.NOTE_BLOCK_DIDGERIDOO.value()),
        FLUTE(SoundEvents.NOTE_BLOCK_FLUTE.value()),
        GUITAR(SoundEvents.NOTE_BLOCK_GUITAR.value()),
        HARP(SoundEvents.NOTE_BLOCK_HARP.value()),
        HAT(SoundEvents.NOTE_BLOCK_HAT.value()),
        IRON_XYLOPHONE(SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value()),
        PLING(SoundEvents.NOTE_BLOCK_PLING.value()),
        SNARE(SoundEvents.NOTE_BLOCK_SNARE.value()),
        XYLOPHONE(SoundEvents.NOTE_BLOCK_XYLOPHONE.value());
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

    public enum Kit implements NameableEnum, StringRepresentable {

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

        public Component getDisplayName() {
            return Component.translatable(name);
        }

        @Override
        public String getSerializedName() {
            return getDisplayName().getString();
        }
    }

    public interface MultiValue {

        MutableComponent format();
    }
}
