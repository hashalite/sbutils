package net.xolt.sbutils.systems;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.util.ChatUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static net.xolt.sbutils.SbUtils.MC;

public class CommandSender {
    private final LinkedList<CommandQueueEntry> entries = new LinkedList<>();
    private long lastCommandSentAt;
    private boolean awaitingResponse;

    public void sendCommand(String command) {
        entries.add(new CommandQueueEntry(command));
    }

    public void sendCommand(String command, Runnable timeOutCallback, CommandResponseMatcher ... responseMatchers) {
        sendCommand(command, timeOutCallback, 5.0, responseMatchers);
    }

    public void sendCommand(String command, Runnable timeOutCallback, double expiryTime, CommandResponseMatcher ... responseMatchers) {
        entries.add(new CommandQueueEntry(command, timeOutCallback, expiryTime, responseMatchers));
    }

    public void tick() {
        if (entries.isEmpty() || MC.getConnection() == null)
            return;
        CommandQueueEntry entry = entries.getFirst();
        long currentTime = System.currentTimeMillis();
        if (awaitingResponse && lastCommandSentAt + (entry.expiryTime * 1000) < currentTime) {
            ChatUtils.printWithPlaceholders("message.sbutils.commandSender.timedOut", entry.command);
            entry.timeOutCallback.run();
            awaitingResponse = false;
            entries.poll();
            return;
        }
        if (awaitingResponse)
            return;

        MC.getConnection().sendCommand(entry.command);
        lastCommandSentAt = currentTime;
        if (!entry.responseMatchers.isEmpty()) {
            awaitingResponse = true;
        } else {
            entries.poll();
        }
    }

    public void onDisconnect() {
        reset();
    }

    public void onContainerSetData(ClientboundContainerSetContentPacket packet) {
        if (!awaitingResponse || entries.isEmpty())
            return;
        if (!(MC.screen instanceof AbstractContainerScreen<?> screen) || screen.getMenu().containerId != packet.getContainerId())
            return;

        CommandQueueEntry entry = entries.getFirst();
        boolean hasScreenMatcher = false;
        for (CommandResponseMatcher responseMatcher : entry.responseMatchers) {
            if (!responseMatcher.matchScreenTitle) {
                hasScreenMatcher = true;
                break;
            }
        }
        if (!hasScreenMatcher)
            return;

        String stringTitle = screen.getTitle().getString();
        for (CommandResponseMatcher responseMatcher : entry.responseMatchers) {
            if (responseMatcher.matches(stringTitle)) {
                responseMatcher.callback.accept(screen.getTitle());
                entries.poll();
                awaitingResponse = false;
                return;
            }
        }
    }

    public void processMessage(Component message) {
        if (!awaitingResponse || entries.isEmpty())
            return;

        CommandQueueEntry entry = entries.getFirst();

        boolean hasChatMatcher = false;
        for (CommandResponseMatcher responseMatcher : entry.responseMatchers) {
            if (!responseMatcher.matchScreenTitle) {
                hasChatMatcher = true;
                break;
            }
        }
        if (!hasChatMatcher)
            return;

        String stringMessage = message.getString();
        for (CommandResponseMatcher responseMatcher : entry.responseMatchers) {
            if (!responseMatcher.matchScreenTitle && responseMatcher.matches(stringMessage)) {
                responseMatcher.callback.accept(message);
                entries.poll();
                awaitingResponse = false;
                return;
            }
        }
    }

    private void reset() {
        entries.clear();
        lastCommandSentAt = 0;
        awaitingResponse = false;
    }

    private static class CommandQueueEntry {
        private final String command;
        private final List<CommandResponseMatcher> responseMatchers = new ArrayList<>();
        private double expiryTime;
        private Runnable timeOutCallback;

        public CommandQueueEntry(String command) {
            this.command = command;
        }

        public CommandQueueEntry(String command, Runnable timeOutCallback, double expiryTime, CommandResponseMatcher ... responseMatchers) {
            this.command = command;
            this.responseMatchers.addAll(Arrays.asList(responseMatchers));
            this.expiryTime = expiryTime;
            this.timeOutCallback = timeOutCallback;
        }
    }

    public static class CommandResponseMatcher {
        private boolean matchScreenTitle;
        private final List<Pattern> responseMatchers = new ArrayList<>();
        private final Consumer<Component> callback;

        public CommandResponseMatcher(Consumer<Component> callback, Pattern ... responseMatchers) {
            this(false, callback, responseMatchers);
        }

        public CommandResponseMatcher(boolean matchScreenTitle, Consumer<Component> callback, Pattern ... responseMatchers) {
            this.matchScreenTitle = matchScreenTitle;
            this.responseMatchers.addAll(Arrays.asList(responseMatchers));
            this.callback = callback;
        }

        public boolean matches(String message) {
            for (Pattern responseMatcher : responseMatchers)
                if (responseMatcher.matcher(message).matches())
                    return true;
            return false;
        }
    }
}
