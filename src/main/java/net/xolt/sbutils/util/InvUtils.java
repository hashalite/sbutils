package net.xolt.sbutils.util;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class InvUtils {

    public static void swapToHotbar(int sourceIndex, int hotbarIndex) {
        if (MC.interactionManager == null || MC.player == null || sourceIndex == hotbarIndex) {
            return;
        }

        MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, indexToInventorySlot(sourceIndex), hotbarIndex, SlotActionType.SWAP, MC.player);
    }

    public static void quickMove(int index) {
        if (MC.interactionManager == null || MC.player == null) {
            return;
        }

        MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, indexToInventorySlot(index), 0, SlotActionType.QUICK_MOVE, MC.player);
    }

    public static int findEmptyHotbarSlot() {
        if (MC.player == null) {
            return -1;
        }
        int emptySlot = MC.player.getInventory().getEmptySlot();
        return emptySlot < 9 ? emptySlot : -1;
    }

    // Converts an index in the player's inventory to a slot id for the current screen
    private static int indexToInventorySlot(int index) {
        if (index < 0 || index > 40) {
            return -1;
        }

        if (MC.player == null) {
            return -1;
        }

        ScreenHandler currentScreenHandler = MC.player.currentScreenHandler;
        boolean playerScreenOpen = playerScreenOpen();

        int invOffset;
        if (playerScreenOpen) {
            invOffset = 9;
        } else {
            if (index >= 36) {
                // Armor and offhand slots inaccessible
                return -1;
            }
            invOffset = currentScreenHandler.slots.size() - 36;
        }

        if (index >= 0 && index <= 8) {
            // Hotbar
            return invOffset + 27 + index;
        }

        if (index >= 9 && index <= 35) {
            // Main inventory
            return (index - 9) + invOffset;
        }

        if (index >= 36 && index <= 39 && playerScreenOpen) {
            // Armor slots
            return 44 - index;
        }

        if (index == 40 && playerScreenOpen) {
            // Offhand slot
            return 45;
        }

        return -1;
    }

    // Returns true if the offhand and armor slots are accessible from the current screen
    public static boolean playerScreenOpen() {
        return MC.player != null && (MC.player.currentScreenHandler instanceof PlayerScreenHandler || MC.player.currentScreenHandler instanceof CreativeInventoryScreen.CreativeScreenHandler);
    }

    public static boolean canSwapSlot(int slotIndex) {
        return !(slotIndex >= 36 && slotIndex <= 40 && !playerScreenOpen());
    }

    public static boolean doesKitFit(PlayerInventory inventory, List<ItemStack> kit) {
        List<ItemStack> kitClone = kit.stream().map(ItemStack::copy).toList();

        List<ItemStack> invClone = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            if (inventory.getStack(i).getItem() == Items.AIR)
                continue;
            invClone.add(inventory.getStack(i).copy());
        }

        for (ItemStack stack : kit) {
            int count = stack.getCount();
            for (ItemStack invStack : invClone) {
                if (!invStack.getItem().equals(stack.getItem()) || invStack.getCount() == invStack.getMaxCount())
                    continue;
                if (invStack.getMaxCount() - invStack.getCount() >= stack.getCount()) {
                    invStack.setCount(invStack.getCount() + stack.getCount());
                    count = 0;
                    break;
                } else {
                    int space = invStack.getMaxCount() - invStack.getCount();
                    invStack.setCount(invStack.getMaxCount());
                    count = count - space;
                    break;
                }
            }
            if (count > 0) {
                invClone.add(new ItemStack(stack.getItem(), count));
            }
        }

        return invClone.size() <= 36;
    }
}
