package net.xolt.sbutils.features.common;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.xolt.sbutils.mixins.GenericContainerScreenAccessor;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.Messenger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.xolt.sbutils.SbUtils.MC;

public class InvCleaner {

    private static final double DELAY = 0.05; // FIXME Hardcoded delay between clicks

    private static boolean cleaning;
    private static boolean openedDisposal;
    private static Predicate<ItemStack> itemsToClean;
    private static Consumer<Boolean> callback;
    private static long lastClick;
    private static int stacksCleaned;

    public static void tick() {
        if (!cleaning || MC.player == null)
            return;

        Screen screen = MC.currentScreen;

        if (!openedDisposal && !isDisposalScreen(screen)) {
            openDisposal();
            openedDisposal = true;
            return;
        }

        if (isDisposalScreen(screen) && openedDisposal) {
            openedDisposal = false;
        }

        if (openedDisposal)
            return;

        if (System.currentTimeMillis() - lastClick < DELAY * 1000) {
            return;
        }

        doClean();
    }

    private static void openDisposal() {
        if (MC.getNetworkHandler() == null)
            return;
        MC.getNetworkHandler().sendChatCommand("disposal");
    }

    private static boolean isDisposalScreen(Screen screen) {
        if (screen == null)
            return false;
        return screen instanceof GenericContainerScreen
                && ((GenericContainerScreenAccessor)screen).getRows() == 4
                && screen.getTitle().getString().equals("Disposal");
    }

    private static boolean hasGarbage(Predicate<ItemStack> itemsToClean) {
        if (MC.player == null || itemsToClean == null)
            return false;
        for (int i = 0; i < 36; i++) {
            if (itemsToClean.test(MC.player.getInventory().getStack(i)))
                return true;
        }
        return false;
    }

    private static void doClean() {
        if (MC.player == null)
            return;
        for (int i = 0; i < 36; i++) {
            if (!itemsToClean.test(MC.player.getInventory().getStack(i)))
                continue;
            InvUtils.quickMove(i, MC.player.currentScreenHandler);
            lastClick = System.currentTimeMillis();
            stacksCleaned++;
            return;
        }
        MC.player.closeHandledScreen();
        // Callback needs to happen before reset() because stacksCleaned is set to 0 by reset()
        if (callback != null)
            callback.accept(stacksCleaned > 0);
        reset();
    }

    public static void clean(List<Item> toClean, Consumer<Boolean> cleanCallback) {
        clean((stack) -> toClean.contains(stack.getItem()), cleanCallback);
    }

    public static void clean(Predicate<ItemStack> toClean, @Nullable Consumer<Boolean> cleanCallback) {
        if (toClean == null || cleaning || !hasGarbage(toClean)) {
            if (cleanCallback != null)
                cleanCallback.accept(false);
            return;
        }
        reset();
        cleaning = true;
        itemsToClean = toClean;
        callback = cleanCallback;
        Messenger.printMessage("message.sbutils.invCleaner.cleaning");
    }

    private static void reset() {
        cleaning = false;
        openedDisposal = false;
        itemsToClean = null;
        callback = null;
        lastClick = 0;
        stacksCleaned = 0;
    }
}
