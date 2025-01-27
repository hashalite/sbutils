package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.component.ItemLore;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.RegexFilters;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;

public class NoGMT extends Feature {
    private static final DateTimeFormatter EMAIL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private static final DateTimeFormatter MAIL_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");

    private final OptionBinding<Boolean> enabled = new OptionBinding<>("noGmt.enabled", Boolean.class, (config) -> config.noGmt.enabled, (config, value) -> config.noGmt.enabled = value);
    private final OptionBinding<String> timeZone = new OptionBinding<>("noGmt.timeZone", String.class, (config) -> config.noGmt.timeZone, (config, value) -> config.noGmt.timeZone = value);
    private final OptionBinding<Boolean> showTimeZone = new OptionBinding<>("noGmt.showTimeZone", Boolean.class, (config) -> config.noGmt.showTimeZone, (config, value) -> config.noGmt.showTimeZone = value);

    public NoGMT() {
        super("noGmt", "nogmt", "ng");
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(enabled, timeZone, showTimeZone);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> noGMTNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled)
                        .then(CommandHelper.string("timeZone", "zone", timeZone))
                        .then(CommandHelper.bool("showTimeZone", showTimeZone))
        );
        registerAlias(dispatcher, noGMTNode);
    }

    public static Component modifyMessage(Component message) {
        Matcher matcher = RegexFilters.emailFilter.matcher(message.getString());
        if (!matcher.matches())
            return message;
        return replaceGmtTime(message, matcher.group(1), EMAIL_DATE_FORMAT);
    }

    public static boolean shouldModify(Component message) {
        return ModConfig.HANDLER.instance().noGmt.enabled && RegexFilters.emailFilter.matcher(message.getString()).matches();
    }

    public static List<ItemStack> replaceTimeInLores(List<ItemStack> stacks) {
        return stacks.stream().map(NoGMT::replaceTimeInLore).toList();
    }

    public static ItemStack replaceTimeInLore(ItemStack stack) {
        if (!(stack.getItem().equals(Items.BOOK)) && !(stack.getItem().equals(Items.WRITABLE_BOOK)))
            return stack;

        ItemLore lore = stack.get(DataComponents.LORE);

        if (lore == null)
            return stack;

        for (int j = 0; j < lore.lines().size(); ++j) {
            Component loreLine = lore.lines().get(j);
            if (loreLine == null)
                continue;
            Matcher matcher = RegexFilters.mailLoreFilter.matcher(loreLine.getString());
            if (matcher.matches()) {
                Component newLoreLine = replaceGmtTime(loreLine, matcher.group(1), MAIL_DATE_FORMAT);
                lore.lines().set(j, newLoreLine);
            }
        }

        ItemStack result = stack.copy();
        result.set(DataComponents.LORE, lore);
        return result;
    }

    private static Component replaceGmtTime(Component text, String target, DateTimeFormatter format) {
        String zoneStr = ModConfig.HANDLER.instance().noGmt.timeZone;
        ZoneId localZone;
        try {
            localZone = ZoneId.of(zoneStr.replaceAll(" ", ""), ZoneId.SHORT_IDS);
        } catch (Exception ignored) {
            localZone = ZoneId.systemDefault();
        }

        ZonedDateTime gmtTime = LocalDateTime.parse(target, format).atZone(ZoneId.of("GMT"));
        String newTimeStr = format.format(gmtTime.withZoneSameInstant(localZone));
        if (ModConfig.HANDLER.instance().noGmt.showTimeZone)
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
