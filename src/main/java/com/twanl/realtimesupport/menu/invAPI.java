package com.twanl.realtimesupport.menu;

import com.twanl.realtimesupport.util.XMaterial;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Twan
 */
public class invAPI {

    // DEFAULT
    public void addItem(Inventory inv, String ItemName, int Amount, int slot, Material itemType, int Byte, List<String> list) {
        // Initialize
        ItemStack I;
        ItemMeta IMeta;

        // CHeck if there is an item
        if (Byte == 0) {
            I = new ItemStack(itemType, Amount);
            IMeta = I.getItemMeta();
            IMeta.setDisplayName(ItemName);
        } else {
            I = new ItemStack(itemType, Amount, (short) Byte);
            IMeta = I.getItemMeta();
            IMeta.setDisplayName(ItemName);
        }

        // Checck if there is an lore, this lore is a LIST
        if (list != null) {
            ArrayList<String> lore = new ArrayList();
            for (String s : list) {
                lore.add(s);
            }
            IMeta.setLore(lore);
        }

        I.setItemMeta(IMeta);
        inv.setItem(slot, I);
    }

    public void addItemGlow(Inventory inv, String ItemName, int Amount, int slot, String itemType, List<String> list) {
        // Initialize
        ItemStack I = new ItemStack(XMaterial.fromString(itemType.toString()).parseItem());

        ItemMeta IMeta = I.getItemMeta();
        IMeta.setDisplayName(ItemName);
        IMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);


        // Checck if there is an lore, this lore is a LIST
        if (list != null) {
            ArrayList<String> lore = new ArrayList();
            for (String s : list) {
                lore.add(s);
            }
            IMeta.setLore(lore);
        }

        I.setItemMeta(IMeta);
        I.addUnsafeEnchantment(Enchantment.LUCK, 1);
        inv.setItem(slot, I);
    }


    public void addItemV2(Inventory inv, String ItemName, int Amount, int slot, String itemType, List<String> list) {

        ItemStack I = new ItemStack(XMaterial.fromString(itemType.toString()).parseItem());
        I.setAmount(Amount);

        ItemMeta IMeta = I.getItemMeta();
        IMeta.setDisplayName(ItemName);


        // Checck if there is an lore, this lore is a LIST
        if (list != null) {
            ArrayList<String> lore = new ArrayList();
            for (String s : list) {
                lore.add(s);
            }
            IMeta.setLore(lore);
        }

        I.setItemMeta(IMeta);
        inv.setItem(slot, I);
    }






}
