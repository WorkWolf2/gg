package com.minegolem.hypingNations.menu.menus;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.CityRef;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.PactManager;
import com.minegolem.hypingNations.menu.MenuManager;
import com.minegolem.hypingNations.menu.config.MenuConfig;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.Team;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ConfirmationMenu {

    private final HypingNations plugin;
    private final MenuManager menuManager;

    public ConfirmationMenu(HypingNations plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }

    public Inventory create(Player player, String actionType, Nation nation, Object data) {
        MenuConfig config = menuManager.getMenuConfig();
        String title = color(config.getConfirmationTitle());

        Inventory inv = Bukkit.createInventory(null, 27, title);

        if (config.getConfirmButton() != null) {
            ItemStack confirmItem = createConfirmItem(config.getConfirmButton(), actionType);
            inv.setItem(config.getConfirmButton().getSlot(), confirmItem);
        }

        if (config.getCancelButton() != null) {
            ItemStack cancelItem = createCancelItem(config.getCancelButton());
            inv.setItem(config.getCancelButton().getSlot(), cancelItem);
        }

        if (config.getInfoDisplayButton() != null) {
            ItemStack infoItem = createInfoItem(config.getInfoDisplayButton(), actionType, nation, data);
            inv.setItem(config.getInfoDisplayButton().getSlot(), infoItem);
        }

        return inv;
    }

    private ItemStack createConfirmItem(MenuConfig.MenuItem menuItem, String actionType) {
        ItemStack item = new ItemStack(menuItem.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color(menuItem.getName()));

            List<String> lore = new ArrayList<>();
            for (String line : menuItem.getLore()) {
                lore.add(color(line.replace("{action}", getActionName(actionType))));
            }
            meta.setLore(lore);

            if (menuItem.getCustomModelData() > 0) {
                meta.setCustomModelData(menuItem.getCustomModelData());
            }

            if (menuItem.isGlowing()) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createCancelItem(MenuConfig.MenuItem menuItem) {
        ItemStack item = new ItemStack(menuItem.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color(menuItem.getName()));

            List<String> lore = new ArrayList<>();
            for (String line : menuItem.getLore()) {
                lore.add(color(line));
            }
            meta.setLore(lore);

            if (menuItem.getCustomModelData() > 0) {
                meta.setCustomModelData(menuItem.getCustomModelData());
            }

            if (menuItem.isGlowing()) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createInfoItem(MenuConfig.MenuItem menuItem, String actionType, Nation nation, Object data) {
        ItemStack item;

        switch (actionType) {
            case "DISSOLVE_NATION" -> {
                item = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(plugin.getServer().getOfflinePlayer(nation.getChief()));
                    meta.setDisplayName(color("&c&lDissolve Nation"));

                    List<String> lore = new ArrayList<>();
                    lore.add(color("&7Nation: &e" + nation.getName()));
                    lore.add(color("&7Chief: &f" + plugin.getServer().getOfflinePlayer(nation.getChief()).getName()));
                    lore.add("");
                    lore.add(color("&7Cities: &f" + nation.getMemberCities().size()));
                    lore.add(color("&7Total Members: &f" + nation.getAllMembers().size()));
                    lore.add("");
                    lore.add(color("&c&lWARNING: This action cannot be undone!"));

                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                return item;
            }
            case "EXCLUDE_CITY" -> {
                CityRef city = (CityRef) data;
                Team team = TeamAPI.getTeamByName(city.teamName());

                item = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                if (meta != null && team != null) {
                    meta.setOwningPlayer(plugin.getServer().getOfflinePlayer(team.getOwner()));
                    meta.setDisplayName(color("&c&lExclude City"));

                    List<String> lore = new ArrayList<>();
                    lore.add(color("&7City: &e" + city.teamName()));
                    lore.add(color("&7Mayor: &f" + plugin.getServer().getOfflinePlayer(team.getOwner()).getName()));
                    lore.add("");
                    lore.add(color("&7Members: &f" + team.getMembers().size()));
                    lore.add(color("&7Claims: &f" + city.getChunksCount()));
                    lore.add("");
                    lore.add(color("&7This city will be removed from"));
                    lore.add(color("&7the nation &e" + nation.getName()));

                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                return item;
            }
            case "BREAK_PACT" -> {
                PactManager.Pact pact = (PactManager.Pact) data;
                Nation targetNation = pact.getTarget();

                item = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(plugin.getServer().getOfflinePlayer(targetNation.getChief()));
                    meta.setDisplayName(color("&c&lBreak Pact"));

                    List<String> lore = new ArrayList<>();
                    lore.add(color("&7Nation: &e" + targetNation.getName()));
                    lore.add(color("&7Chief: &f" + plugin.getServer().getOfflinePlayer(targetNation.getChief()).getName()));
                    lore.add("");
                    lore.add(color("&7Duration: &f" + pact.getDurationDays() + " days"));
                    lore.add("");
                    lore.add(color("&7Breaking this pact will allow"));
                    lore.add(color("&7both nations to overclaim each other"));

                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                return item;
            }
            default -> {
                item = new ItemStack(menuItem.getMaterial());
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(color(menuItem.getName()));
                    item.setItemMeta(meta);
                }
                return item;
            }
        }
    }

    public void handleClick(Player player, int slot, MenuManager.ActiveMenu activeMenu) {
        MenuConfig config = menuManager.getMenuConfig();

        if (config.getConfirmButton() != null && slot == config.getConfirmButton().getSlot()) {
            player.closeInventory();
            if (activeMenu.getConfirmAction() != null) {
                activeMenu.getConfirmAction().run();
            }
            return;
        }

        if (config.getCancelButton() != null && slot == config.getCancelButton().getSlot()) {
            player.closeInventory();
            player.sendMessage(color("&7Action cancelled."));
            return;
        }
    }

    private String getActionName(String actionType) {
        return switch (actionType) {
            case "DISSOLVE_NATION" -> "Dissolve Nation";
            case "EXCLUDE_CITY" -> "Exclude City";
            case "BREAK_PACT" -> "Break Pact";
            default -> "Confirm";
        };
    }

    private String color(String text) {
        return text.replace("&", "§");
    }
}
