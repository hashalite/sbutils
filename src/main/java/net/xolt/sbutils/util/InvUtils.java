package net.xolt.sbutils.util;

import java.util.*;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import static net.xolt.sbutils.SbUtils.MC;

public class InvUtils {

    public static void swapToHotbar(int sourceIndex, int hotbarIndex, AbstractContainerMenu screenHandler) {
        if (MC.gameMode == null || MC.player == null || sourceIndex == hotbarIndex) {
            return;
        }

        MC.gameMode.handleInventoryMouseClick(MC.player.containerMenu.containerId, indexToInventorySlot(sourceIndex, screenHandler), hotbarIndex, ClickType.SWAP, MC.player);
    }

    public static void quickMove(int index, AbstractContainerMenu screenHandler) {
        if (MC.gameMode == null || MC.player == null) {
            return;
        }

        MC.gameMode.handleInventoryMouseClick(MC.player.containerMenu.containerId, indexToInventorySlot(index, screenHandler), 0, ClickType.QUICK_MOVE, MC.player);
    }

    public static int findEmptyHotbarSlot() {
        if (MC.player == null) {
            return -1;
        }
        int emptySlot = MC.player.getInventory().getFreeSlot();
        return emptySlot < 9 ? emptySlot : -1;
    }

    // Converts an index in the player's inventory to a slot id for the provided screen handler
    private static int indexToInventorySlot(int index, AbstractContainerMenu screenHandler) {
        if (screenHandler == null || index < 0 || index > 40) {
            return -1;
        }

        boolean playerScreenOpen = isPlayerScreen(screenHandler);

        int invOffset;
        if (playerScreenOpen) {
            invOffset = 9;
        } else {
            if (index >= 36) {
                // Armor and offhand slots inaccessible
                return -1;
            }
            invOffset = screenHandler.slots.size() - 36;
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
    private static boolean isPlayerScreen(AbstractContainerMenu screenHandler) {
        return screenHandler instanceof InventoryMenu || screenHandler instanceof CreativeModeInventoryScreen.ItemPickerMenu;
    }

    public static boolean canSwapSlot(int slotIndex, AbstractContainerMenu screenHandler) {
        return !(slotIndex >= 36 && slotIndex <= 40 && !isPlayerScreen(screenHandler));
    }

    public static boolean doesKitFit(Inventory inventory, List<ItemStack> kit) {
        List<ItemStack> invItems = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            if (inventory.getItem(i).getItem() == Items.AIR)
                continue;
            invItems.add(inventory.getItem(i).copy());
        }

        for (ItemStack stack : kit) {
            int count = stack.getCount();
            for (ItemStack invStack : invItems) {
                if (!invStack.getItem().equals(stack.getItem()) || invStack.getCount() == invStack.getMaxStackSize())
                    continue;
                if (invStack.getMaxStackSize() - invStack.getCount() >= stack.getCount()) {
                    invStack.setCount(invStack.getCount() + stack.getCount());
                    count = 0;
                    break;
                } else {
                    int space = invStack.getMaxStackSize() - invStack.getCount();
                    invStack.setCount(invStack.getMaxStackSize());
                    count = count - space;
                    break;
                }
            }
            if (count > 0) {
                invItems.add(new ItemStack(stack.getItem(), count));
            }
        }

        return invItems.size() <= 36;
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack itemStack) {
        ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        itemEnchantments.keySet().forEach(enchantment -> {
            enchantments.put(enchantment.value(), itemEnchantments.getLevel(enchantment));
        });
        return enchantments;
    }

    public static Enchantment getEnchantment(ResourceKey<Enchantment> enchantment) {
        assert MC.level != null;
        Registry<Enchantment> registry = MC.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Optional<Holder.Reference<Enchantment>> optionalEnchantment = registry.get(enchantment);
        if (optionalEnchantment.isEmpty())
            return null;
        return optionalEnchantment.get().value();
    }
}
