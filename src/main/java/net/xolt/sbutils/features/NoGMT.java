package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.BookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WritableBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.CommandUtils;
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
                CommandUtils.toggle(COMMAND, "nogmt", () -> ModConfig.HANDLER.instance().noGMT, (value) -> ModConfig.HANDLER.instance().noGMT = value)
                        .then(CommandUtils.string("timeZone", "zone", "timeZone", () -> ModConfig.HANDLER.instance().timeZone, (value) -> ModConfig.HANDLER.instance().timeZone = value))
                        .then(CommandUtils.bool("showTimeZone", "showTimeZone", () -> ModConfig.HANDLER.instance().showTimeZone, (value) -> ModConfig.HANDLER.instance().showTimeZone = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource()))
                .redirect(noGMTNode));
    }

    public static Text modifyMessage(Text message) {
        Matcher matcher = RegexFilters.emailFilter.matcher(message.getString());
        if (!matcher.matches())
            return message;
        return replaceGmtTime(message, matcher.group(1), EMAIL_DATE_FORMAT);
    }

    public static boolean shouldModify(Text message) {
        return ModConfig.HANDLER.instance().noGMT && RegexFilters.emailFilter.matcher(message.getString()).matches();
    }

    public static List<ItemStack> replaceTimeInLores(List<ItemStack> stacks) {
        return stacks.stream().map(NoGMT::replaceTimeInLore).toList();
    }

    public static ItemStack replaceTimeInLore(ItemStack stack) {
        if (!(stack.getItem() instanceof BookItem) && !(stack.getItem() instanceof WritableBookItem))
            return stack;

        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(ItemStack.DISPLAY_KEY))
            return stack;

        NbtCompound displayNbt = nbt.getCompound(ItemStack.DISPLAY_KEY);
        if (!displayNbt.contains(ItemStack.LORE_KEY))
            return stack;

        NbtList nbtList = displayNbt.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
        for (int j = 0; j < nbtList.size(); ++j) {
            MutableText loreLine = Text.Serialization.fromJson(nbtList.getString(j));
            if (loreLine == null)
                continue;
            Matcher matcher = RegexFilters.mailLoreFilter.matcher(loreLine.getString());
            if (matcher.matches()) {
                Text newLoreLine = replaceGmtTime(loreLine, matcher.group(1), MAIL_DATE_FORMAT);
                nbtList.set(j, NbtString.of(Text.Serialization.toJsonString(newLoreLine)));
            }
        }

        ItemStack result = stack.copy();
        result.setSubNbt(ItemStack.LORE_KEY, nbtList);
        return result;
    }

    private static Text replaceGmtTime(Text text, String target, DateTimeFormatter format) {
        String zoneStr = ModConfig.HANDLER.instance().timeZone;
        ZoneId localZone;
        try {
            localZone = ZoneId.of(zoneStr.replaceAll(" ", ""), ZoneId.SHORT_IDS);
        } catch (Exception ignored) {
            localZone = ZoneId.systemDefault();
        }

        ZonedDateTime gmtTime = LocalDateTime.parse(target, format).atZone(ZoneId.of("GMT"));
        String newTimeStr = format.format(gmtTime.withZoneSameInstant(localZone));
        if (ModConfig.HANDLER.instance().showTimeZone) {
            newTimeStr += " (" + localZone.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")";
        }
        return replaceText(text, target, newTimeStr);
    }

    private static Text replaceText(Text message, String target, String replacement) {
        StringBuilder sb = new StringBuilder();
        TextContent content = message.getContent();
        content.visit(string -> {
            sb.append(string);
            return Optional.empty();
        });
        String stringText = sb.toString();

        MutableText newMessage = MutableText.of(message.getContent()).setStyle(message.getStyle());
        if (stringText.contains(target)) {
            stringText = stringText.replaceAll(target, replacement);
            newMessage = Text.literal(stringText).setStyle(message.getStyle());
        }

        for (Text text : message.getSiblings()) {
            newMessage.append(replaceText(text, target, replacement));
        }

        return newMessage;
    }
}
