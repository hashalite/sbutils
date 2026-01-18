package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.RegexFilters;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;

public class NoGMT extends Feature<ModConfig> {
    private static final DateTimeFormatter EMAIL_DATE_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("yyyy/MM/dd HH:mm")
            .toFormatter(Locale.US);
    private static final DateTimeFormatter MAIL_DATE_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MM/dd/yyyy hh:mm a")
            .toFormatter(Locale.US);

    private final OptionBinding<ModConfig, Boolean> enabled = new OptionBinding<>("sbutils", "noGmt.enabled", Boolean.class, (config) -> config.noGmt.enabled, (config, value) -> config.noGmt.enabled = value);
    private final OptionBinding<ModConfig, String> timeZone = new OptionBinding<>("sbutils", "noGmt.timeZone", String.class, (config) -> config.noGmt.timeZone, (config, value) -> config.noGmt.timeZone = value);
    private final OptionBinding<ModConfig, Boolean> showTimeZone = new OptionBinding<>("sbutils", "noGmt.showTimeZone", Boolean.class, (config) -> config.noGmt.showTimeZone, (config, value) -> config.noGmt.showTimeZone = value);

    public NoGMT() {
        super("sbutils", "noGmt", "nogmt", "ng");
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(enabled, timeZone, showTimeZone);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> noGMTNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled, ModConfig.HANDLER)
                        .then(CommandHelper.string("timeZone", "zone", timeZone, ModConfig.HANDLER))
                        .then(CommandHelper.bool("showTimeZone", showTimeZone, ModConfig.HANDLER))
        );
        registerAlias(dispatcher, noGMTNode);
    }

    public static Component modifyMessage(Component message) {
        if (!ModConfig.instance().noGmt.enabled)
            return message;

        Matcher matcher = RegexFilters.emailFilter.matcher(message.getString());
        if (!matcher.matches())
            return message;

        return replaceGmtTime(message, matcher.group(1), EMAIL_DATE_FORMAT);
    }

    public static List<ItemStack> replaceTimeInLores(List<ItemStack> stacks) {
        return stacks.stream().map(NoGMT::replaceTimeInLore).toList();
    }

    public static ItemStack replaceTimeInLore(ItemStack stack) {
        if (!(stack.getItem().equals(Items.BOOK)) && !(stack.getItem().equals(Items.WRITABLE_BOOK)))
            return stack;

        List<Component> lore = InvUtils.getItemLore(stack);

        for (int j = 0; j < lore.size(); ++j) {
            Component loreLine = lore.get(j);
            if (loreLine == null)
                continue;
            Matcher matcher = RegexFilters.mailLoreFilter.matcher(loreLine.getString());
            if (matcher.matches()) {
                Component newLoreLine = replaceGmtTime(loreLine, matcher.group(1), MAIL_DATE_FORMAT);
                lore.set(j, newLoreLine);
            }
        }

        ItemStack result = stack.copy();
        InvUtils.setItemLore(result, lore);
        return result;
    }

    private static Component replaceGmtTime(Component text, String target, DateTimeFormatter format) {
        String zoneStr = ModConfig.instance().noGmt.timeZone;
        ZoneId localZone;
        try {
            localZone = ZoneId.of(zoneStr.replaceAll(" ", ""), ZoneId.SHORT_IDS);
        } catch (Exception ignored) {
            localZone = ZoneId.systemDefault();
        }

        ZonedDateTime gmtTime = LocalDateTime.parse(target, format).atZone(ZoneId.of("GMT"));
        String newTimeStr = format.format(gmtTime.withZoneSameInstant(localZone));
        if (ModConfig.instance().noGmt.showTimeZone)
            newTimeStr += " (" + localZone.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")";
        return replaceText(text, target, newTimeStr);
    }

    private static Component replaceText(Component message, String target, String replacement) {
        StringBuilder sb = new StringBuilder();
        ComponentContents content = message.getContents();
        content.visit(string -> {
            sb.append(string);
            return Optional.empty();
        });
        String stringText = sb.toString();

        MutableComponent newMessage = MutableComponent.create(message.getContents()).setStyle(message.getStyle());
        if (stringText.contains(target)) {
            stringText = stringText.replaceAll(target, replacement);
            newMessage = Component.literal(stringText).setStyle(message.getStyle());
        }

        for (Component text : message.getSiblings())
            newMessage.append(replaceText(text, target, replacement));

        return newMessage;
    }
}
