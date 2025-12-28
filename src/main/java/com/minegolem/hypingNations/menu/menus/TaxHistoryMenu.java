package com.minegolem.hypingNations.menu.menus;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.data.TaxHistory;
import com.minegolem.hypingNations.menu.MenuManager;
import com.minegolem.hypingNations.menu.config.MenuConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaxHistoryMenu {

    private final HypingNations plugin;
    private final MenuManager menuManager;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public TaxHistoryMenu(HypingNations plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }

    public Inventory create(Player player, Nation nation, int page) {
        MenuConfig config = menuManager.getMenuConfig();
        String title = color(config.getTaxHistoryTitle()
                .replace("{nation}", nation.getName())
                .replace("{page}", String.valueOf(page + 1)));

        Inventory inv = Bukkit.createInventory(null, config.getTaxHistorySize(), title);

        List<TaxHistory.TaxEntry> entries = plugin.getTaxHistoryManager()
                .getTaxHistory(nation.getId());

        int itemsPerPage = 45;
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, entries.size());

        for (int i = startIndex; i < endIndex; i++) {
            TaxHistory.TaxEntry entry = entries.get(i);
            int slot = i - startIndex;

            ItemStack entryItem = createTaxEntryItem(config.getTaxEntryButton(), entry);
            inv.setItem(slot, entryItem);
        }

        if (page > 0 && config.getPreviousPageButton() != null) {
            inv.setItem(config.getPreviousPageButton().getSlot(),
                    createNavigationItem(config.getPreviousPageButton(), "Previous Page"));
        }

        if (endIndex < entries.size() && config.getNextPageButton() != null) {
            inv.setItem(config.getNextPageButton().getSlot(),
                    createNavigationItem(config.getNextPageButton(), "Next Page"));
        }

        return inv;
    }

    private ItemStack createTaxEntryItem(MenuConfig.MenuItem menuItem, TaxHistory.TaxEntry entry) {
        Material material = entry.isSuccess() ? Material.LIME_DYE : Material.RED_DYE;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String status = entry.isSuccess() ? "&a✓ Paid" : "&c✗ Failed";
            meta.setDisplayName(color(status + " &7- &f" + dateFormatter.format(entry.getTimestamp())));

            List<String> lore = new ArrayList<>();
            lore.add(color("&7Amount: &f$" + df.format(entry.getAmount())));
            lore.add(color("&7Chunks: &f" + entry.getChunks()));
            lore.add("");

            if (entry.isSuccess()) {
                lore.add(color("&aTax successfully collected"));
            } else {
                lore.add(color("&cInsufficient funds in capital"));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createNavigationItem(MenuConfig.MenuItem menuItem, String defaultName) {
        ItemStack item = new ItemStack(menuItem.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color(menuItem.getName().isEmpty() ? defaultName : menuItem.getName()));

            if (menuItem.getCustomModelData() > 0) {
                meta.setCustomModelData(menuItem.getCustomModelData());
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public void handleClick(Player player, int slot, Nation nation, MenuManager.ActiveMenu activeMenu) {
        MenuConfig config = menuManager.getMenuConfig();

        if (config.getPreviousPageButton() != null &&
                slot == config.getPreviousPageButton().getSlot() &&
                activeMenu.getPage() > 0) {
            player.closeInventory();
            menuManager.openTaxHistoryMenu(player, activeMenu.getPage() - 1);
            return;
        }

        if (config.getNextPageButton() != null &&
                slot == config.getNextPageButton().getSlot()) {
            player.closeInventory();
            menuManager.openTaxHistoryMenu(player, activeMenu.getPage() + 1);
            return;
        }
    }

    private String color(String text) {
        return text.replace("&", "§");
    }
}