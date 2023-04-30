package net.xolt.sbutils.config;

import dev.isxander.yacl.api.NameableEnum;
import dev.isxander.yacl.config.ConfigEntry;
import dev.isxander.yacl.config.ConfigInstance;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.xolt.sbutils.util.LimitedList;

import java.nio.file.Path;
import java.util.List;

public class ModConfig {

    public static final ConfigInstance<ModConfig> INSTANCE = new ModConfigInstance<>(ModConfig.class, Path.of("./sbutils/sbutils.json"));

    // Mod Settings

    @ConfigEntry public String messagePrefix = "[@]";
    @ConfigEntry public Color sbutilsColor = Color.YELLOW;
    @ConfigEntry public Color prefixColor = Color.AQUA;
    @ConfigEntry public Color messageColor = Color.AQUA;
    @ConfigEntry public Color valueColor = Color.WHITE;


    // Auto Advert Settings

    @ConfigEntry public boolean autoAdvert = false;
    @ConfigEntry public String skyblockAdFile = "skyblock";
    @ConfigEntry public String economyAdFile = "economy";
    @ConfigEntry public String classicAdFile = "classic";
    @ConfigEntry public double advertDelay = 300.0;
    @ConfigEntry public double advertInitialDelay = 10.0;
    @ConfigEntry public boolean advertUseWhitelist = false;
    @ConfigEntry public List<String> advertWhitelist = List.of();


    // Join Commands Settings

    @ConfigEntry public boolean joinCmdsEnabled = false;
    @ConfigEntry public double joinCmdInitialDelay = 0.0;
    @ConfigEntry public double joinCmdDelay = 0.0;
    @ConfigEntry public List<String> joinCmdServers = List.of("server.skyblock.net");


    // Mentions Settings

    @ConfigEntry public boolean mentions = false;
    @ConfigEntry public boolean excludeServerMsgs = true;
    @ConfigEntry public boolean excludeSelfMsgs = true;
    @ConfigEntry public NotifSound mentionSound = NotifSound.EXPERIENCE;
    @ConfigEntry public boolean mentionHighlight = true;
    @ConfigEntry public Color highlightColor = Color.GOLD;
    @ConfigEntry public boolean mentionsCurrentAccount = true;
    @ConfigEntry public List<String> mentionsAliases = List.of();


    // Enchant All Settings

    @ConfigEntry public double enchantDelay = 0.55;
    @ConfigEntry public int cooldownFrequency = 12;
    @ConfigEntry public double cooldownTime = 6.0;
    @ConfigEntry public boolean excludeFrost = true;


    // Chat Append Settings

    @ConfigEntry public boolean addPrefix = false;
    @ConfigEntry public String chatPrefix = "";
    @ConfigEntry public boolean addSuffix = false;
    @ConfigEntry public String chatSuffix = "";


    // Chat Filters Settings

    @ConfigEntry public boolean tipsFilterEnabled = false;
    @ConfigEntry public boolean advancementsFilterEnabled = false;
    @ConfigEntry public boolean welcomeFilterEnabled = false;
    @ConfigEntry public boolean friendJoinFilterEnabled = false;
    @ConfigEntry public boolean motdFilterEnabled = false;
    @ConfigEntry public boolean voteFilterEnabled = false;
    @ConfigEntry public boolean voteRewardFilterEnabled = false;
    @ConfigEntry public boolean raffleFilterEnabled = false;
    @ConfigEntry public boolean cratesFilterEnabled = false;
    @ConfigEntry public boolean perishedInVoidFilterEnabled = false;
    @ConfigEntry public boolean skyChatFilterEnabled = false;


    // Chat Logger Settings

    //@ConfigEntry public boolean shopLoggerIncoming = false;
    //@ConfigEntry public boolean shopLoggerOutgoing = false;
    @ConfigEntry public boolean msgLoggerIncoming = false;
    @ConfigEntry public boolean msgLoggerOutgoing = false;
    @ConfigEntry public boolean visitLogger = false;
    @ConfigEntry public boolean dpLogger = false;


    // Auto Mine Settings

    @ConfigEntry public boolean autoMine = false;
    @ConfigEntry public boolean autoSwitch = true;
    @ConfigEntry public int switchDurability = 20;


    // Auto Fix Settings

    @ConfigEntry public boolean autoFix = false;
    @ConfigEntry public FixMode autoFixMode = FixMode.HAND;
    @ConfigEntry public double maxFixPercent = 0.2;
    @ConfigEntry public double autoFixDelay = 120.0;
    @ConfigEntry public double fixRetryDelay = 3.0;
    @ConfigEntry public int maxFixRetries = 3;


    // Tool Saver Settings

    @ConfigEntry public boolean toolSaver = false;
    @ConfigEntry public int toolSaverDurability = 20;


    // Anti Place Settings

    @ConfigEntry public boolean antiPlaceHeads = false;
    @ConfigEntry public boolean antiPlaceGrass = false;


    // Auto Command Settings

    @ConfigEntry public boolean autoCommandEnabled = false;
    @ConfigEntry public String autoCommand = "";
    @ConfigEntry public double autoCommandDelay = 45.0;


    // Auto Reply Settings

    @ConfigEntry public boolean autoReply = false;
    @ConfigEntry public String autoResponse = "I am currently AFK. Please /mail me and I'll get back to you later!";
    @ConfigEntry public double autoReplyDelay = 1.0;


    // Auto Raffle Settings

    @ConfigEntry public boolean autoRaffle = false;
    @ConfigEntry public int skyblockRaffleTickets = 2;
    @ConfigEntry public int economyRaffleTickets = 5;
    @ConfigEntry public double grassCheckDelay = 5.0;


    // Auto Private Settings

    @ConfigEntry public boolean autoPrivate = false;
    @ConfigEntry public List<String> autoPrivateNames = new LimitedList<>(2);


    // Auto Silk Settings

    @ConfigEntry public boolean autoSilk = false;
    @ConfigEntry public SilkTarget targetTool = SilkTarget.DIAMOND_PICKAXE;
    @ConfigEntry public double autoSilkDelay = 0.25;


    // Staff Detector Settings

    @ConfigEntry public boolean detectStaffJoin = false;
    @ConfigEntry public boolean detectStaffLeave = false;
    @ConfigEntry public boolean playStaffSound = false;
    @ConfigEntry public NotifSound staffDetectSound = NotifSound.BIT;


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
    }

    public enum FixMode implements NameableEnum {
        HAND("text.sbutils.config.option.autoFixMode.hand"),
        ALL("text.sbutils.config.option.autoFixMode.all");

        private final String name;

        FixMode(String name) {
            this.name = name;
        }

        public Text getDisplayName() {
            return Text.translatable(name);
        }
    }

    public enum SilkTarget implements NameableEnum {
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
    }
}
