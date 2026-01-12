package net.xolt.sbutils.util;

import java.util.*;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
//? if >=1.21 {
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
//? }
//? if >=1.20.6 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.ItemEnchantments;
//? } else {
/*import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
*///? }

import static net.xolt.sbutils.SbUtils.MC;

public class InvUtils {

    public static void swapToHotbar(int sourceIndex, int hotbarIndex, AbstractContainerMenu screenHandler) {
        if (MC.gameMode == null || MC.player == null || sourceIndex == hotbarIndex) {
            return;
        }

        MC.gameMode.handleInventoryMouseClick(screenHandler.containerId, invIndexToScreenSlot(sourceIndex, screenHandler), hotbarIndex, ClickType.SWAP, MC.player);
    }

    public static void quickMove(int index, AbstractContainerMenu screenHandler) {
        if (MC.gameMode == null || MC.player == null) {
            return;
        }

        MC.gameMode.handleInventoryMouseClick(screenHandler.containerId, invIndexToScreenSlot(index, screenHandler), 0, ClickType.QUICK_MOVE, MC.player);
    }

    public static int findEmptyHotbarSlot() {
        if (MC.player == null) {
            return -1;
        }
        int emptySlot = MC.player.getInventory().getFreeSlot();
        return emptySlot < 9 ? emptySlot : -1;
    }

    // Converts an index in the player's inventory container to a slot id for the provided screen
    // Returns -1 if the specified slot is unavailable in the provided screen
    private static int invIndexToScreenSlot(int index, AbstractContainerMenu screenHandler) {
        // Loop through all slots in the screen
        for (Slot slot : screenHandler.slots) {
            // Skip all slots not in the player's inventory
            if (!(slot.container instanceof Inventory))
                continue;
            // If the container slot matches, return the screen slot
            if (slot.getContainerSlot() == index)
                return slot.index;
        }
        return -1;
    }

    public static boolean canSwapSlot(int slotIndex, AbstractContainerMenu screenHandler) {
        return invIndexToScreenSlot(slotIndex, screenHandler) != -1;
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
        //? if >=1.20.6 {
        ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments storedEnchantments = itemStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        itemEnchantments.keySet().forEach(enchantment -> {
            enchantments.put(enchantment.value(), itemEnchantments.getLevel(
                    enchantment
                            //? if <1.21
                            //.value()
            ));
        });
        storedEnchantments.keySet().forEach(enchantment -> {
            enchantments.put(enchantment.value(), itemEnchantments.getLevel(
                    enchantment
                            //? if <1.21
                            //.value()
            ));
        });
        return enchantments;
        //? } else
        //return EnchantmentHelper.getEnchantments(itemStack);
    }

    //? if >=1.21 {
    public static Enchantment getEnchantment(ResourceKey<Enchantment> enchantment) {
        assert MC.level != null;
        Registry<Enchantment>
                registry = MC.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Optional<Holder.Reference<Enchantment>> optionalEnchantment = registry.get(enchantment);
        if (optionalEnchantment.isEmpty())
            return null;
        return optionalEnchantment.get().value();
    }
    //? } else {
    /*// For compatibility with <1.21
    public static Enchantment getEnchantment(Enchantment enchantment) {
        return enchantment;
    }
    *///? }

    public static List<Component> getItemLore(ItemStack item) {
        List<Component> result = new ArrayList<>();
        //? if >1.20.4 {
        ItemLore lore = item.get(DataComponents.LORE);

        if (lore == null)
            return result;

        result = lore.lines();
        //? } else {
        /*CompoundTag nbt = item.getTag();
        if (nbt == null || !nbt.contains(ItemStack.TAG_DISPLAY))
            return result;

        CompoundTag displayNbt = nbt.getCompound(ItemStack.TAG_DISPLAY);
        if (!displayNbt.contains(ItemStack.TAG_LORE))
            return result;

        ListTag nbtList = displayNbt.getList(ItemStack.TAG_LORE, Tag.TAG_STRING);
        for (int i = 0; i < nbtList.size(); i++) {
            result.add(Component.Serializer.fromJson(nbtList.getString(i)));
        }
        *///? }
        return result;
    }

    public static void setItemLore(ItemStack item, List<Component> lore) {
        //? if >1.20.4 {
        ItemLore itemLore = new ItemLore(lore, lore);
        item.set(DataComponents.LORE, itemLore);
        //? } else {
        /*ListTag nbtList = new ListTag();
        lore.stream().map(line -> StringTag.valueOf(Component.Serializer.toJson(line))).forEach((nbtList::add));
        item.addTagElement(ItemStack.TAG_LORE, nbtList);
        *///? }
    }

    public static int getSelectedSlot(LocalPlayer player) {
        //? if >=1.21.11 {
        return player.getInventory().getSelectedSlot();
        //? } else
        //return player.getInventory().selected;
    }

    public static void setSelectedSlot(LocalPlayer player, int slot) {
        //? if >=1.21.11 {
        player.getInventory().setSelectedSlot(slot);
        //? } else
        //player.getInventory().selected = slot;
    }

    public static ItemStack getSelectedItem(LocalPlayer player) {
        //? if >=1.21.11 {
        return player.getInventory().getSelectedItem();
        //? } else
        //return player.getInventory().getSelected();
    }
}
