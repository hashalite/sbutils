package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.argument.ColorArgumentType;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.RegexFilters;
import net.xolt.sbutils.util.SoundUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.xolt.sbutils.SbUtils.MC;

public class Mentions extends Feature<ModConfig> {
    private static final String VISIT_COMMAND = "/visit ";

    private final OptionBinding<ModConfig, Boolean> enabled = new OptionBinding<>("sbutils", "mentions.enabled", Boolean.class, (config) -> config.mentions.enabled, (config, value) -> config.mentions.enabled = value);
    private final OptionBinding<ModConfig, Boolean> playSound = new OptionBinding<>("sbutils", "mentions.playSound", Boolean.class, (config) -> config.mentions.playSound, (config, value) -> config.mentions.playSound = value);
    private final OptionBinding<ModConfig, ModConfig.NotifSound> sound = new OptionBinding<>("sbutils", "mentions.sound", ModConfig.NotifSound.class, (config) -> config.mentions.sound, (config, value) -> config.mentions.sound = value);
    private final OptionBinding<ModConfig, Boolean> highlight = new OptionBinding<>("sbutils", "mentions.highlight", Boolean.class, (config) -> config.mentions.highlight, (config, value) -> config.mentions.highlight = value);
    private final OptionBinding<ModConfig, Boolean> highlightMultiColor = new OptionBinding<>("sbutils", "mentions.highlightMultiColor", Boolean.class, (config) -> config.mentions.highlightMultiColor, (config, value) -> config.mentions.highlightMultiColor = value);
    private final OptionBinding<ModConfig, Color> highlightColor = new OptionBinding<>("sbutils", "mentions.highlightColor", Color.class, (config) -> config.mentions.highlightColor, (config, value) -> config.mentions.highlightColor = value);
    private final OptionBinding<ModConfig, Boolean> excludeServerMsgs = new OptionBinding<>("sbutils", "mentions.excludeServerMsgs", Boolean.class, (config) -> config.mentions.excludeServerMsgs, (config, value) -> config.mentions.excludeServerMsgs = value);
    private final OptionBinding<ModConfig, Boolean> excludeSelfMsgs = new OptionBinding<>("sbutils", "mentions.excludeSelfMsgs", Boolean.class, (config) -> config.mentions.excludeSelfMsgs, (config, value) -> config.mentions.excludeSelfMsgs = value);
    private final OptionBinding<ModConfig, Boolean> excludeSender = new OptionBinding<>("sbutils", "mentions.excludeSender", Boolean.class, (config) -> config.mentions.excludeSender, (config, value) -> config.mentions.excludeSender = value);
    private final OptionBinding<ModConfig, Boolean> currentAccount = new OptionBinding<>("sbutils", "mentions.currentAccount", Boolean.class, (config) -> config.mentions.currentAccount, (config, value) -> config.mentions.currentAccount = value);
    private final ListOptionBinding<ModConfig, String> aliases = new ListOptionBinding<>("sbutils", "mentions.aliases", "", String.class, (config) -> config.mentions.aliases, (config, value) -> config.mentions.aliases = value);

    public Mentions() {
        super("sbutils", "mentions", "mentions", "ment");
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(enabled, playSound, sound, highlight, highlightMultiColor, highlightColor, excludeServerMsgs, excludeSelfMsgs, excludeSender, currentAccount, aliases);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> mentionsNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled, ModConfig.HANDLER)
                    .then(CommandHelper.bool("playSound", playSound, ModConfig.HANDLER))
                    .then(CommandHelper.bool("excludeServer", excludeServerMsgs, ModConfig.HANDLER))
                    .then(CommandHelper.bool("excludeSelf", excludeSelfMsgs, ModConfig.HANDLER))
                    .then(CommandHelper.bool("excludeSender", excludeSender, ModConfig.HANDLER))
                    .then(CommandHelper.bool("currentAccount", currentAccount, ModConfig.HANDLER))
                    .then(CommandHelper.stringList("aliases", "alias", aliases, ModConfig.HANDLER))
                    .then(CommandHelper.genericEnum("sound", "sound", sound, ModConfig.HANDLER))
                    .then(CommandHelper.bool("highlight", highlight, ModConfig.HANDLER)
                            .then(CommandHelper.getterSetter("color", "color", highlightColor, ModConfig.HANDLER, ColorArgumentType.color(), ColorArgumentType::getColor))
                            .then(CommandHelper.bool("multiColor", highlightMultiColor, ModConfig.HANDLER))));
        registerAlias(dispatcher, mentionsNode);
    }

    public static void processMessage(Component message) {
        if (!ModConfig.instance().mentions.enabled || !ModConfig.instance().mentions.playSound || !isValidMessage(message) || !mentioned(message))
            return;
        playSound();
    }

    public static Component modifyMessage(Component message) {
        if (!ModConfig.instance().mentions.enabled || !ModConfig.instance().mentions.highlight || !isValidMessage(message) || !mentioned(message)) {
            return message;
        }

        if (MC.player == null)
            return message;

        boolean highlightMultiColor = ModConfig.instance().mentions.highlightMultiColor;
        List<Region> mentions = findMentions(message, highlightMultiColor);
        if (mentions.isEmpty())
            return message;

        return highlightCompound(message, mentions, highlightMultiColor);
    }

    private static boolean isValidMessage(Component message) {
        if (ModConfig.instance().mentions.excludeServerMsgs &&
                !RegexFilters.playerMsgFilter.matcher(message.getString()).matches() &&
                !RegexFilters.incomingMsgFilter.matcher(message.getString()).matches() &&
                !RegexFilters.outgoingMsgFilter.matcher(message.getString()).matches())
            return false;

        if (!ModConfig.instance().mentions.excludeSelfMsgs || MC.player == null)
            return true;

        if (RegexFilters.outgoingMsgFilter.matcher(message.getString()).matches())
            return false;

        String username = MC.player.getGameProfile()
                //? if >=1.21.11 {
                .name();
                //? } else
                //.getName();

        String sender = findSender(message);

        return !username.equals(sender);
    }

    private static boolean mentioned(Component message) {
        return !findMentions(message, true).isEmpty();
    }

    private static List<Region> findMentions(Component text, boolean includeMultiColor) {
        List<Region> result = new ArrayList<>();
        if (MC.player == null)
            return result;

        List<String> targets = new ArrayList<>(ModConfig.instance().mentions.aliases);

        if (ModConfig.instance().mentions.currentAccount) {
            String username = MC.player.getGameProfile()
                    //? if >=1.21.11 {
                    .name()
                    //? } else
                    //.getName()
                    .toLowerCase();
            targets.add(username);
        }

        for (String target : targets)
            result.addAll(findMentions(text, target, includeMultiColor, ModConfig.instance().mentions.excludeSender));

        return Region.sortAndMergeOverlapping(result);
    }

    private static List<Region> findMentions(Component text, String target, boolean includeMultiColor, boolean excludeSender) {
        String string = text.getString().toLowerCase();
        target = target.toLowerCase();

        int prefixLen = 0;
        if (excludeSender) {
            List<Pattern> filters = List.of(RegexFilters.playerMsgFilter, RegexFilters.incomingMsgFilter, RegexFilters.outgoingMsgFilter);
            for (Pattern filter : filters) {
                Matcher matcher = filter.matcher(string);
                if (!matcher.matches())
                    continue;
                String prefixOnly = matcher.group(1);
                prefixLen = prefixOnly.length();
                string = string.replace(prefixOnly, "");
                break;
            }
        }

        Pattern targetRegex;
        if (includeMultiColor) {
            StringBuilder sb = new StringBuilder();
            for (char c : target.toCharArray()) {
                String quoted = Pattern.quote(String.valueOf(c));
                String withColorRegex = "(\u00a7[0-9a-fk-or])*" + quoted;
                sb.append(withColorRegex);
            }
            targetRegex = Pattern.compile(sb.toString());
        } else {
            targetRegex = Pattern.compile(Pattern.quote(target));
        }

        Matcher matcher = targetRegex.matcher(string);
        List<Region> result = new ArrayList<>();
        while (matcher.find())
            result.add(new Region(prefixLen + matcher.start(), prefixLen + matcher.end()));

        return Region.sortAndMergeOverlapping(result);
    }

    private static Component highlightCompound(Component text, List<Region> regions, boolean includeMultiColor) {
        List<Component> flatList = text.toFlatList();
        MutableComponent highlighted = Component.empty();
        int stringIndex = 0;
        for (int i = 0; i < flatList.size(); i++) {
            Component c = flatList.get(i);
            int cLen = c.getString().length();
            int cStart = stringIndex;
            int cEnd = cStart + cLen;
            stringIndex = cEnd;
            Region cRegion = new Region(cStart, cEnd);

            List<Region> overlaps;
            if (includeMultiColor) {
                overlaps = cRegion.getOverlapping(regions);
            } else {
                overlaps = cRegion.getContained(regions);
            }

            // If no regions intersect with this component, add to result and continue
            if (overlaps.isEmpty()) {
                highlighted.append(c);
                continue;
            }

            // Convert regions to indices within the current component, 'c'
            overlaps = overlaps.stream().map((region) -> new Region(Math.clamp(region.start - cStart, 0, cLen), Math.clamp(region.end - cStart, 0, cLen))).toList();

            // Highlight the matched regions
            highlighted.append(highlight(c, overlaps));
        }
        return highlighted;
    }

    private static Component highlight(Component text, List<Region> regions) {
        int index = 0;
        String cString = text.getString();
        MutableComponent highlighted = Component.empty();
        for (Region region : regions) {
            int start = region.start;
            int end = region.end;
            if (index < start)
                highlighted.append(Component.literal(cString.substring(index, start)).withStyle(text.getStyle()));
            String toHighlight = cString.substring(start, end);
            String noColorCodes = toHighlight.replaceAll("\u00a7[0-9a-fk-or]", "");
            highlighted.append(Component.literal(noColorCodes).withStyle(text.getStyle().withColor(ModConfig.instance().mentions.highlightColor.getRGB())));
            index = end;
        }

        if (index < cString.length())
            highlighted.append(Component.literal(cString.substring(index)).withStyle(text.getStyle()));

        return highlighted;
    }

    public static String findSender(Component message) {
        List<Component> flattened = message.toFlatList();
        for (Component c : flattened) {
            ClickEvent clickEvent = c.getStyle().getClickEvent();
            if (clickEvent == null)
                continue;

            ClickEvent.Action action = clickEvent
                    //? if >=1.21.11 {
                    .action();
                    //? } else
                    //.getAction();

            String command;
            if (action.equals(ClickEvent.Action.RUN_COMMAND)) {
                command =
                        //? if >=1.21.11 {
                        ((ClickEvent.RunCommand)clickEvent).command();
                        //? } else
                        //clickEvent.getValue();
            } else if (action.equals(ClickEvent.Action.SUGGEST_COMMAND)) {
                command =
                        //? if >=1.21.11 {
                        ((ClickEvent.SuggestCommand)clickEvent).command();
                        //? } else
                        //clickEvent.getValue();
            } else {
                continue;
            }

            if (!command.startsWith(VISIT_COMMAND))
                continue;

            return command.replace(VISIT_COMMAND, "");
        }
        return null;
    }

    private static void playSound() {
        SoundUtils.playNotifSound(ModConfig.instance().mentions.sound.getSound(), SoundSource.MASTER);
    }

    private static class Region {
        private int start;
        private int end;

        public Region(int start, int end) {
            this.start = start;
            this.end = end;
        }

        // Returns all regions from 'regions' that are fully contained within this region
        private List<Region> getContained(List<Region> regions) {
            List<Region> result = new ArrayList<>();
            for (Region r : regions) {
                int start = r.start;
                int end = r.end;
                if (start >= this.start && start < this.end && end > this.start && end <= this.end)
                    result.add(r);
            }
            return result;
        }

        // Return all regions from 'regions' that have any overlap with this region
        private List<Region> getOverlapping(List<Region> regions) {
            List<Region> result = new ArrayList<>();
            for (Region r : regions) {
                int start = r.start;
                int end = r.end;
                if ((start < this.start && end <= this.start) || (start >= this.end && end > this.end)) {
                    continue;
                }
                result.add(r);
            }
            return result;
        }

        private static List<Region> sortAndMergeOverlapping(List<Region> regions) {
            if (regions == null || regions.isEmpty())
                return List.of();

            // Sort by start index
            regions.sort(Comparator.comparingInt((r) -> r.start));

            List<Region> merged = new ArrayList<>();
            Region current = regions.getFirst();

            for (int i = 1; i < regions.size(); i++) {
                Region next = regions.get(i);

                if (next.start <= current.end) {
                    // Overlap: extend the current region
                    current.end = Math.max(current.end, next.end);
                } else {
                    // No overlap: add current to result and continue
                    merged.add(current);
                    current = next;
                }
            }

            // Add the last range
            merged.add(current);

            return merged;
        }
    }
}
