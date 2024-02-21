package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
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

public class NoGMT {

    private static final DateTimeFormatter EMAIL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private static final DateTimeFormatter MAIL_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");
    private static final String COMMAND = "nogmt";
    private static final String ALIAS = "ng";

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> noGMTNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "noGmt", () -> ModConfig.HANDLER.instance().noGmt.enabled, (value) -> ModConfig.HANDLER.instance().noGmt.enabled = value)
                        .then(CommandHelper.string("timeZone", "zone", "noGmt.timeZone", () -> ModConfig.HANDLER.instance().noGmt.timeZone, (value) -> ModConfig.HANDLER.instance().noGmt.timeZone = value))
                        .then(CommandHelper.bool("showTimeZone", "noGmt.showTimeZone", () -> ModConfig.HANDLER.instance().noGmt.showTimeZone, (value) -> ModConfig.HANDLER.instance().noGmt.showTimeZone = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource()))
                .redirect(noGMTNode));
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
        if (!(stack.getItem() instanceof BookItem) && !(stack.getItem() instanceof WritableBookItem))
            return stack;

        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains(ItemStack.TAG_DISPLAY))
            return stack;

        CompoundTag displayNbt = nbt.getCompound(ItemStack.TAG_DISPLAY);
        if (!displayNbt.contains(ItemStack.TAG_LORE))
            return stack;

        ListTag nbtList = displayNbt.getList(ItemStack.TAG_LORE, Tag.TAG_STRING);
        for (int j = 0; j < nbtList.size(); ++j) {
            MutableComponent loreLine = Component.Serializer.fromJson(nbtList.getString(j));
            if (loreLine == null)
                continue;
            Matcher matcher = RegexFilters.mailLoreFilter.matcher(loreLine.getString());
            if (matcher.matches()) {
                Component newLoreLine = replaceGmtTime(loreLine, matcher.group(1), MAIL_DATE_FORMAT);
                nbtList.set(j, StringTag.valueOf(Component.Serializer.toJson(newLoreLine)));
            }
        }

        ItemStack result = stack.copy();
        result.addTagElement(ItemStack.TAG_LORE, nbtList);
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
        if (ModConfig.HANDLER.instance().noGmt.showTimeZone) {
            newTimeStr += " (" + localZone.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")";
        }
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

        for (Component text : message.getSiblings()) {
            newMessage.append(replaceText(text, target, replacement));
        }

        return newMessage;
    }
}
