package com.minegolem.hypingNations.menu.menus;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.PactManager;
import com.minegolem.hypingNations.menu.MenuManager;
import com.minegolem.hypingNations.menu.config.MenuConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManagePactsMenu {

    private final HypingNations plugin;
    private final MenuManager menuManager;

    public ManagePactsMenu(HypingNations plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }

    public Inventory create(Player player, Nation nation, int page) {
        MenuConfig config = menuManager.getMenuConfig();
        String title = color(config.getManagePactsTitle()
                .replace("{nation}", nation.getName())
                .replace("{page}", String.valueOf(page + 1)));

        Inventory inv = Bukkit.createInventory(null, config.getManagePactsSize(), title);

        List<PactManager.Pact> pacts = new ArrayList<>(nation.getActivePacts().values());
        pacts = pacts.stream()
                .filter(p -> p.isActive() && !p.isExpired())
                .collect(Collectors.toList());

        List<Integer> slots = config.getPactSlots();

        int startIndex = page * slots.size();
        int endIndex = Math.min(startIndex + slots.size(), pacts.size());

        boolean isChief = nation.getChief().equals(player.getUniqueId());
        boolean canManagePacts = plugin.getPermissionManager().hasPermission(
                player.getUniqueId(), nation, "can_break_pact"
        );

        // Display pacts
        for (int i = startIndex; i < endIndex; i++) {
            PactManager.Pact pact = pacts.get(i);
            int slotIndex = i - startIndex;

            if (slotIndex < slots.size()) {
                ItemStack pactItem = createPactItem(pact, nation, canManagePacts);
                inv.setItem(slots.get(slotIndex), pactItem);
            }
        }

        // Create pact button (next available slot)
        if (canManagePacts && endIndex < slots.size()) {
            inv.setItem(slots.get(endIndex - startIndex), createCreatePactButton(config));
        }

        // Navigation buttons
        if (page > 0 && config.getPreviousPageButtonGeneral() != null) {
            inv.setItem(config.getPreviousPageButtonGeneral().getSlot(),
                    createNavigationItem(config.getPreviousPageButtonGeneral(), "Previous Page"));
        }

        if (endIndex < pacts.size() && config.getNextPageButtonGeneral() != null) {
            inv.setItem(config.getNextPageButtonGeneral().getSlot(),
                    createNavigationItem(config.getNextPageButtonGeneral(), "Next Page"));
        }

        // Custom items
        for (MenuConfig.CustomMenuItem customItem : config.getManagePactsCustomItems()) {
            inv.setItem(customItem.getSlot(), createCustomItem(customItem, nation, player));
        }

        return inv;
    }

    private ItemStack createPactItem(PactManager.Pact pact, Nation myNation, boolean canManage) {
        Nation targetNation = pact.getTarget();

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (meta != null) {
            // Set head to chief of target nation
            meta.setOwningPlayer(plugin.getServer().getOfflinePlayer(targetNation.getChief()));
            meta.setDisplayName(color("&b" + targetNation.getName()));

            List<String> lore = new ArrayList<>();
            lore.add(color("&7Members: &f" + targetNation.getAllMembers().size()));
            lore.add(color("&7Claims: &f" + targetNation.getTotalChunks()));
            lore.add("");

            // Calculate remaining time
            LocalDate endDate = pact.getStartDate().plusDays(pact.getDurationDays());
            Duration remaining = Duration.between(LocalDate.now().atStartOfDay(), endDate.atStartOfDay());

            long days = remaining.toDays();
            long hours = remaining.toHours() % 24;

            if (days > 0) {
                lore.add(color("&7Time Remaining: &f" + days + "d " + hours + "h"));
            } else if (hours > 0) {
                lore.add(color("&7Time Remaining: &f" + hours + "h"));
            } else {
                lore.add(color("&7Time Remaining: &cExpiring soon"));
            }

            lore.add(color("&7Duration: &f" + pact.getDurationDays() + " days"));
            lore.add("");

            if (canManage) {
                lore.add(color("&cLeft Click &7» &fBreak Pact"));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createCreatePactButton(MenuConfig config) {
        MenuConfig.MenuItem createBtn = config.getCreatePactButton();
        if (createBtn == null) {
            ItemStack item = new ItemStack(Material.LIME_DYE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(color("&a+ Create Pact"));
                item.setItemMeta(meta);
            }
            return item;
        }

        ItemStack item = new ItemStack(createBtn.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color(createBtn.getName()));

            List<String> lore = new ArrayList<>();
            for (String line : createBtn.getLore()) {
                lore.add(color(line));
            }
            meta.setLore(lore);

            if (createBtn.getCustomModelData() > 0) {
                meta.setCustomModelData(createBtn.getCustomModelData());
            }

            if (createBtn.isGlowing()) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

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

    private ItemStack createCustomItem(MenuConfig.CustomMenuItem menuItem, Nation nation, Player player) {
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

    public void handleClick(Player player, int slot, Nation nation, MenuManager.ActiveMenu activeMenu) {
        MenuConfig config = menuManager.getMenuConfig();
        List<Integer> slots = config.getPactSlots();

        // Navigation
        if (config.getPreviousPageButtonGeneral() != null &&
                slot == config.getPreviousPageButtonGeneral().getSlot() &&
                activeMenu.getPage() > 0) {
            player.closeInventory();
            menuManager.openManagePactsMenu(player, activeMenu.getPage() - 1);
            return;
        }

        if (config.getNextPageButtonGeneral() != null &&
                slot == config.getNextPageButtonGeneral().getSlot()) {
            player.closeInventory();
            menuManager.openManagePactsMenu(player, activeMenu.getPage() + 1);
            return;
        }

        // Check if clicked on pact slot
        int slotIndex = slots.indexOf(slot);
        if (slotIndex == -1) return;

        List<PactManager.Pact> pacts = nation.getActivePacts().values().stream()
                .filter(p -> p.isActive() && !p.isExpired())
                .collect(Collectors.toList());

        int pactIndex = activeMenu.getPage() * slots.size() + slotIndex;

        boolean canManagePacts = plugin.getPermissionManager().hasPermission(
                player.getUniqueId(), nation, "can_break_pact"
        );

        // Create pact button
        if (pactIndex >= pacts.size()) {
            if (canManagePacts) {
                player.closeInventory();
                player.sendMessage(color("&7Use &f/hnations pact propose <nation> <days> &7to create a pact."));
            }
            return;
        }

        // Clicked on pact
        PactManager.Pact pact = pacts.get(pactIndex);

        if (canManagePacts) {
            // Break pact
            player.closeInventory();
            menuManager.openConfirmationMenu(player, "BREAK_PACT", () -> {
                breakPact(player, nation, pact);
            }, pact);
        }
    }

    private void breakPact(Player player, Nation nation, PactManager.Pact pact) {
        boolean success = plugin.getPactManager().breakPact(nation, pact.getTarget());

        if (success) {
            player.sendMessage(color("&cPact with &e" + pact.getTarget().getName() + " &chas been broken!"));

            // Notify other nation's chief
            Player targetChief = plugin.getServer().getPlayer(pact.getTarget().getChief());
            if (targetChief != null && targetChief.isOnline()) {
                targetChief.sendMessage(color("&e" + nation.getName() + " &chas broken the pact with your nation!"));
            }

            // Save changes
            plugin.getPersistenceService().saveNation(nation);
            plugin.getPersistenceService().saveNation(pact.getTarget());
        } else {
            player.sendMessage(color("&cFailed to break pact!"));
        }
    }

    private void executeCommands(List<String> commands, Player player, Nation nation) {
        for (String cmd : commands) {
            String processedCmd = cmd
                    .replace("{player}", player.getName())
                    .replace("{nation}", nation.getName());

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCmd);
        }
    }

    private String replacePlaceholders(String text, Nation nation) {
        return text
                .replace("{nation}", nation.getName())
                .replace("{pacts}", String.valueOf(nation.getActivePacts().size()));
    }

    private String color(String text) {
        return text.replace("&", "§");
    }
}
