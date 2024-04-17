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

    public void sendCommand(String command, boolean opensScreen, Consumer<Component> callback, Pattern ... responseMatchers) {
        sendCommand(command, opensScreen, callback, 5.0, responseMatchers);
    }

    public void sendCommand(String command, boolean opensScreen, Consumer<Component> callback, double expiryTime, Pattern ... responseMatchers) {
        entries.add(new CommandQueueEntry(command, opensScreen, callback, expiryTime, responseMatchers));
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

    public void onContainerSetData(ClientboundContainerSetContentPacket packet) {
        if (!awaitingResponse || entries.isEmpty() || !entries.getFirst().opensScreen)
            return;
        if (!(MC.screen instanceof AbstractContainerScreen<?> screen) || screen.getMenu().containerId != packet.getContainerId())
            return;

        String stringTitle = screen.getTitle().getString();
        CommandQueueEntry entry = entries.getFirst();
        for (Pattern responseMatcher : entry.responseMatchers) {
            if (responseMatcher.matcher(stringTitle).matches()) {
                entry.callback.accept(screen.getTitle());
                entries.poll();
                awaitingResponse = false;
                return;
            }
        }
    }

    public void processMessage(Component message) {
        if (!awaitingResponse || entries.isEmpty() || entries.getFirst().opensScreen)
            return;
        String stringMessage = message.getString();
        CommandQueueEntry entry = entries.getFirst();
        for (Pattern responseMatcher : entry.responseMatchers) {
            if (responseMatcher.matcher(stringMessage).matches()) {
                entry.callback.accept(message);
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
        private final List<Pattern> responseMatchers = new ArrayList<>();
        private final Consumer<Component> callback;
        private final boolean needsResponse;
        private final boolean opensScreen;
        private double expiryTime;


        public CommandQueueEntry(String command) {
            this.command = command;
            this.callback = (response) -> {};
            this.needsResponse = false;
            this.opensScreen = false;
        }

        public CommandQueueEntry(String command, boolean opensScreen, Consumer<Component> callback, double expiryTime, Pattern ... responseMatchers) {
            this.command = command;
            this.opensScreen = opensScreen;
            this.responseMatchers.addAll(Arrays.asList(responseMatchers));
            this.callback = callback;
            this.needsResponse = true;
            this.expiryTime = expiryTime;
        }
    }
}
