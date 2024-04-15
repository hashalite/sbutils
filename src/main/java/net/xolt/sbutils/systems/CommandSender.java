package net.xolt.sbutils.systems;

import net.minecraft.network.chat.Component;
import net.xolt.sbutils.util.ChatUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static net.xolt.sbutils.SbUtils.MC;

public class CommandSender {
    private LinkedList<CommandQueueEntry> entries = new LinkedList<>();
    private long lastCommandSentAt;
    private boolean awaitingResponse;

    public void sendCommand(String command) {
        entries.add(new CommandQueueEntry(command));
    }

    public void sendCommand(String command, Runnable callback, Pattern ... responseMatchers) {
        sendCommand(command, callback, 5.0, responseMatchers);
    }

    public void sendCommand(String command, Runnable callback, double expiryTime, Pattern ... responseMatchers) {
        entries.add(new CommandQueueEntry(command, (response) -> callback.run(), expiryTime, responseMatchers));
    }

    public void sendCommand(String command, Consumer<Component> callback, Pattern ... responseMatchers) {
        sendCommand(command, callback, 5.0, responseMatchers);
    }

    public void sendCommand(String command, Consumer<Component> callback, double expiryTime, Pattern ... responseMatchers) {
        entries.add(new CommandQueueEntry(command, callback, expiryTime, responseMatchers));
    }

    public void tick() {
        if (entries.isEmpty() || MC.getConnection() == null)
            return;
        CommandQueueEntry entry = entries.getFirst();
        long currentTime = System.currentTimeMillis();
        if (awaitingResponse && lastCommandSentAt + (entry.expiryTime * 1000) < currentTime) {
            ChatUtils.printWithPlaceholders("message.sbutils.commandSender.timedOut", entry.command);
            entry.callback.accept(null);
            awaitingResponse = false;
            entries.poll();
            return;
        }
        if (awaitingResponse)
            return;

        MC.getConnection().sendCommand(entry.command);
        lastCommandSentAt = currentTime;
        if (entry.needsResponse) {
            awaitingResponse = true;
        } else {
            entries.poll();
        }
    }

    public void onDisconnect() {
        reset();
    }

    public void processMessage(Component message) {
        if (!awaitingResponse || entries.isEmpty())
            return;
        String stringMessage = message.getString();
        CommandQueueEntry entry = entries.getFirst();
        for (Pattern responseMatcher : entry.responseMatchers) {
            if (responseMatcher.matcher(stringMessage).matches()) {
                entry.callback.accept(message);
                entries.poll();
                awaitingResponse = false;
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
        private final List<Pattern> responseMatchers = new ArrayList<>();
        private final Consumer<Component> callback;
        private final boolean needsResponse;
        private double expiryTime;

        public CommandQueueEntry(String command) {
            this.command = command;
            this.callback = (response) -> {};
            this.needsResponse = false;
        }

        public CommandQueueEntry(String command, Consumer<Component> callback, double expiryTime, Pattern ... responseMatchers) {
            this.command = command;
            this.responseMatchers.addAll(Arrays.asList(responseMatchers));
            this.callback = callback;
            this.needsResponse = true;
            this.expiryTime = expiryTime;
        }
    }
}
