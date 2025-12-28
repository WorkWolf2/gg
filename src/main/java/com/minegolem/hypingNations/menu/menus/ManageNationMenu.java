package com.minegolem.hypingNations.menu.menus;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.menu.MenuManager;
import com.minegolem.hypingNations.menu.config.MenuConfig;
import com.minegolem.hypingNations.menu.sign.SignMenuFactory;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ManageNationMenu {

    private final HypingNations plugin;
    private final MenuManager menuManager;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public ManageNationMenu(HypingNations plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }

    public Inventory create(Player player, Nation nation, int page) {
        MenuConfig config = menuManager.getMenuConfig();
        String title = color(config.getManageNationTitle()
                .replace("{nation}", nation.getName()));

        Inventory inv = Bukkit.createInventory(null, config.getManageNationSize(), title);

        if (config.getDissolveButton() != null &&
                plugin.getPermissionManager().hasPermission(player.getUniqueId(), nation, "can_delete_nation")) {
            inv.setItem(config.getDissolveButton().getSlot(),
                    createItem(config.getDissolveButton(), nation, player));
        }

        if (config.getRenameButton() != null &&
                nation.getChief().equals(player.getUniqueId())) {
            inv.setItem(config.getRenameButton().getSlot(),
                    createItem(config.getRenameButton(), nation, player));
        }

        if (config.getInfoButton() != null) {
            inv.setItem(config.getInfoButton().getSlot(),
                    createInfoItem(config.getInfoButton(), nation));
        }

        if (config.getTaxHistoryButton() != null) {
            inv.setItem(config.getTaxHistoryButton().getSlot(),
                    createItem(config.getTaxHistoryButton(), nation, player));
        }

        for (MenuConfig.CustomMenuItem customItem : config.getManageNationCustomItems()) {
            inv.setItem(customItem.getSlot(), createCustomItem(customItem, nation, player));
        }

        return inv;
    }

    private ItemStack createInfoItem(MenuConfig.MenuItem menuItem, Nation nation) {
        ItemStack item = new ItemStack(menuItem.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color(menuItem.getName()));

            List<String> lore = new ArrayList<>();
            for (String line : menuItem.getLore()) {
                lore.add(color(replacePlaceholders(line, nation)));
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

    private ItemStack createItem(MenuConfig.MenuItem menuItem, Nation nation, Player player) {
        return createInfoItem(menuItem, nation);
    }

    private ItemStack createCustomItem(MenuConfig.CustomMenuItem menuItem, Nation nation, Player player) {
        return createInfoItem(menuItem, nation);
    }

    private String replacePlaceholders(String text, Nation nation) {
        double nextTax = plugin.getTaxManager().calculateTax(nation);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextTaxTime = now.plusDays(1).withHour(0).withMinute(0).withSecond(0);
        Duration duration = Duration.between(now, nextTaxTime);

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        String timeUntilTax = String.format("%dh %dm", hours, minutes);

        return text
                .replace("{nation}", nation.getName())
                .replace("{chief}", plugin.getServer().getOfflinePlayer(nation.getChief()).getName())
                .replace("{capital}", nation.getCapital().teamName())
                .replace("{members}", String.valueOf(nation.getAllMembers().size()))
                .replace("{cities}", String.valueOf(nation.getMemberCities().size()))
                .replace("{treasury}", df.format(nation.getTreasury()))
                .replace("{total_chunks}", String.valueOf(nation.getTotalChunks()))
                .replace("{tax_per_chunk}", df.format(plugin.getTaxManager().getPerChunkPrice()))
                .replace("{next_tax}", df.format(nextTax))
                .replace("{time_until_tax}", timeUntilTax)
                .replace("{unpaid_days}", String.valueOf(nation.getUnpaidDays()))
                .replace("{max_unpaid_days}", String.valueOf(plugin.getTaxManager().getMaxUnpaidDaysBeforeDissolution()));
    }

    public void handleClick(Player player, int slot, Nation nation, MenuManager.ActiveMenu activeMenu) {
        MenuConfig config = menuManager.getMenuConfig();

        if (config.getDissolveButton() != null && slot == config.getDissolveButton().getSlot()) {
            if (plugin.getPermissionManager().hasPermission(player.getUniqueId(), nation, "can_delete_nation")) {
                player.closeInventory();
                menuManager.openConfirmationMenu(player, "DISSOLVE_NATION", () -> {
                    deleteNation(player, nation);
                }, nation);
            }
            return;
        }


        if (config.getRenameButton() != null && slot == config.getRenameButton().getSlot()) {
            if (nation.getChief().equals(player.getUniqueId())) {
                openRenameSign(player, nation);
            }
            return;
        }

        if (config.getTaxHistoryButton() != null && slot == config.getTaxHistoryButton().getSlot()) {
            player.closeInventory();
            menuManager.openTaxHistoryMenu(player, 0);
            return;
        }

        for (MenuConfig.CustomMenuItem customItem : config.getManageNationCustomItems()) {
            if (slot == customItem.getSlot()) {
                player.closeInventory();
                executeCommands(customItem.getCommands(), player, nation);
                return;
            }
        }
    }

    private void openRenameSign(Player player, Nation nation) {
        player.closeInventory();

        SignMenuFactory signMenuFactory = new SignMenuFactory(plugin);
        signMenuFactory.newMenu()
                .withLines("", "Enter new", "nation name", "")
                .response((p, lines) -> {
                    String newName = lines[0];

                    if (newName == null || newName.trim().isEmpty()) {
                        p.sendMessage(color("&cInvalid name!"));
                        return false;
                    }

                    if (plugin.getNationManager().isNameTaken(newName) &&
                            !nation.getName().equalsIgnoreCase(newName)) {
                        p.sendMessage(color("&cThat nation name is already taken!"));
                        return false;
                    }

                    renameNation(p, nation, newName);
                    return true;
                })
                .open(player);
    }

    private void renameNation(Player player, Nation nation, String newName) {
        String oldName = nation.getName();

        nation.setName(newName);

        plugin.getNationManager().deleteNation(oldName);

        plugin.getPersistenceService().saveNation(nation);

        player.sendMessage(color("&aNation renamed from &e" + oldName + " &ato &e" + newName));

        plugin.getFoliaLib().getScheduler().runLater(() -> {
            menuManager.openManageNationMenu(player);
        }, 1L);
    }

    private void deleteNation(Player player, Nation nation) {
        String nationName = nation.getName();
        int memberCities = nation.getMemberCities().size();
        int totalMembers = nation.getAllMembers().size();

        plugin.getNationManager().deleteNation(nationName);
        plugin.getPersistenceService().deleteNation(nation.getId());

        player.sendMessage(color("&cNation &e" + nationName + " &chas been dissolved!"));
        player.sendMessage(color("&7Former cities: &f" + memberCities));
        player.sendMessage(color("&7Former members: &f" + totalMembers));
    }

    private void executeCommands(List<String> commands, Player player, Nation nation) {
        for (String cmd : commands) {
            String processedCmd = cmd
                    .replace("{player}", player.getName())
                    .replace("{nation}", nation.getName())
                    .replace("{chief}", plugin.getServer().getOfflinePlayer(nation.getChief()).getName());

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCmd);
        }
    }

    private String color(String text) {
        return text.replace("&", "§");
    }
}
