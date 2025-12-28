package com.minegolem.hypingNations.menu.menus;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.CityRef;
import com.minegolem.hypingNations.data.Nation;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ManageCitiesMenu {

    private final HypingNations plugin;
    private final MenuManager menuManager;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public ManageCitiesMenu(HypingNations plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }

    public Inventory create(Player player, Nation nation, int page) {
        MenuConfig config = menuManager.getMenuConfig();
        String title = color(config.getManageCitiesTitle()
                .replace("{nation}", nation.getName())
                .replace("{page}", String.valueOf(page + 1)));

        Inventory inv = Bukkit.createInventory(null, config.getManageCitiesSize(), title);

        List<CityRef> cities = new ArrayList<>(nation.getMemberCities());
        List<Integer> slots = config.getCitySlots();

        int startIndex = page * slots.size();
        int endIndex = Math.min(startIndex + slots.size(), cities.size());

        boolean isChief = nation.getChief().equals(player.getUniqueId());

        for (int i = startIndex; i < endIndex; i++) {
            CityRef city = cities.get(i);
            int slotIndex = i - startIndex;

            if (slotIndex < slots.size()) {
                ItemStack cityItem = createCityItem(city, nation, isChief);
                inv.setItem(slots.get(slotIndex), cityItem);
            }
        }

        if (isChief && endIndex < slots.size()) {
            inv.setItem(slots.get(endIndex - startIndex), createInviteButton(config));
        }

        if (page > 0 && config.getPreviousPageButtonGeneral() != null) {
            inv.setItem(config.getPreviousPageButtonGeneral().getSlot(),
                    createNavigationItem(config.getPreviousPageButtonGeneral(), "Previous Page"));
        }

        if (endIndex < cities.size() && config.getNextPageButtonGeneral() != null) {
            inv.setItem(config.getNextPageButtonGeneral().getSlot(),
                    createNavigationItem(config.getNextPageButtonGeneral(), "Next Page"));
        }

        for (MenuConfig.CustomMenuItem customItem : config.getManageCitiesCustomItems()) {
            inv.setItem(customItem.getSlot(), createCustomItem(customItem, nation, player));
        }

        return inv;
    }

    private ItemStack createCityItem(CityRef city, Nation nation, boolean isChief) {
        Team team = TeamAPI.getTeamByName(city.teamName());
        if (team == null) return new ItemStack(Material.BARRIER);

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (meta != null) {
            meta.setOwningPlayer(plugin.getServer().getOfflinePlayer(team.getOwner()));
            meta.setDisplayName(color("&e" + city.teamName()));

            List<String> lore = new ArrayList<>();
            lore.add(color("&7Members: &f" + team.getMembers().size()));
            lore.add(color("&7Claims: &f" + city.getChunksCount()));

            double dailyCost = city.getChunksCount() * plugin.getTaxManager().getPerChunkPrice();
            lore.add(color("&7Daily Cost: &f$" + df.format(dailyCost)));
            lore.add("");

            boolean isCapital = nation.getCapital().equals(city);
            if (isCapital) {
                lore.add(color("&6★ Capital City ★"));
            } else if (isChief) {
                lore.add(color("&cLeft Click &7» &fExclude from Nation"));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createInviteButton(MenuConfig config) {
        MenuConfig.MenuItem inviteBtn = config.getInviteCityButton();
        if (inviteBtn == null) {
            ItemStack item = new ItemStack(Material.LIME_DYE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(color("&a+ Invite City"));
                item.setItemMeta(meta);
            }
            return item;
        }

        ItemStack item = new ItemStack(inviteBtn.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color(inviteBtn.getName()));

            List<String> lore = new ArrayList<>();
            for (String line : inviteBtn.getLore()) {
                lore.add(color(line));
            }
            meta.setLore(lore);

            if (inviteBtn.getCustomModelData() > 0) {
                meta.setCustomModelData(inviteBtn.getCustomModelData());
            }

            if (inviteBtn.isGlowing()) {
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
        List<Integer> slots = config.getCitySlots();

        if (config.getPreviousPageButtonGeneral() != null &&
                slot == config.getPreviousPageButtonGeneral().getSlot() &&
                activeMenu.getPage() > 0) {
            player.closeInventory();
            menuManager.openManageCitiesMenu(player, activeMenu.getPage() - 1);
            return;
        }

        if (config.getNextPageButtonGeneral() != null &&
                slot == config.getNextPageButtonGeneral().getSlot()) {
            player.closeInventory();
            menuManager.openManageCitiesMenu(player, activeMenu.getPage() + 1);
            return;
        }

        int slotIndex = slots.indexOf(slot);
        if (slotIndex == -1) return;

        List<CityRef> cities = new ArrayList<>(nation.getMemberCities());
        int cityIndex = activeMenu.getPage() * slots.size() + slotIndex;

        boolean isChief = nation.getChief().equals(player.getUniqueId());

        if (cityIndex >= cities.size()) {
            if (isChief) {
                player.closeInventory();
                player.sendMessage(color("&7Use &f/hnations invitecity <city_name> &7to invite a city to your nation."));
            }
            return;
        }

        CityRef city = cities.get(cityIndex);

        openCityActionMenu(player, nation, city, isChief);
    }

    private void openCityActionMenu(Player player, Nation nation, CityRef city, boolean isChief) {
        Inventory inv = Bukkit.createInventory(null, 27, color("&6Manage: &e" + city.teamName()));

        Team team = TeamAPI.getTeamByName(city.teamName());
        if (team == null) {
            player.sendMessage(color("&cCity not found!"));
            return;
        }

        ItemStack viewMembers = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta viewMeta = viewMembers.getItemMeta();
        if (viewMeta != null) {
            viewMeta.setDisplayName(color("&e&lView Members"));
            viewMeta.setLore(List.of(
                    color("&7Members: &f" + team.getMembers().size()),
                    "",
                    color("&eClick to view members")
            ));
            viewMembers.setItemMeta(viewMeta);
        }
        inv.setItem(11, viewMembers);

        boolean isCapital = nation.getCapital().equals(city);
        if (isChief && !isCapital) {
            ItemStack exclude = new ItemStack(Material.RED_WOOL);
            ItemMeta excludeMeta = exclude.getItemMeta();
            if (excludeMeta != null) {
                excludeMeta.setDisplayName(color("&c&lExclude City"));
                excludeMeta.setLore(List.of(
                        color("&7Remove this city from"),
                        color("&7the nation"),
                        "",
                        color("&cClick to exclude")
                ));
                exclude.setItemMeta(excludeMeta);
            }
            inv.setItem(15, exclude);
        }

        // Back button
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(color("&cBack"));
            back.setItemMeta(backMeta);
        }
        inv.setItem(22, back);

        player.openInventory(inv);

        // Store context
        MenuManager.ActiveMenu menu = new MenuManager.ActiveMenu(
                MenuManager.MenuType.CITY_ACTION, nation.getId(), 0
        );
        menu.setActionData(city);
        menuManager.getActiveMenus().put(player.getUniqueId(), menu);
    }

    public void handleCityAction(Player player, int slot, Nation nation, CityRef city) {
        boolean isChief = nation.getChief().equals(player.getUniqueId());

        switch (slot) {
            case 11 -> {
                player.closeInventory();
                menuManager.openViewMembersMenu(player, nation, city, 0);
            }
            case 15 -> {
                if (isChief && !city.equals(nation.getCapital())) {
                    player.closeInventory();
                    menuManager.openConfirmationMenu(player, "EXCLUDE_CITY", () -> {
                        excludeCity(player, nation, city);
                    }, city);
                }
            }
            case 22 -> {
                player.closeInventory();
                menuManager.openManageCitiesMenu(player, 0);
            }
        }
    }

    private void excludeCity(Player player, Nation nation, CityRef city) {
        boolean success = plugin.getNationManager().removeCityFromNation(nation.getName(), city.teamName());

        if (success) {
            player.sendMessage(color("&aCity &e" + city.teamName() + " &ahas been excluded from the nation!"));

            Team team = TeamAPI.getTeamByName(city.teamName());
            if (team != null) {
                Player mayor = plugin.getServer().getPlayer(team.getOwner());
                if (mayor != null && mayor.isOnline()) {
                    mayor.sendMessage(color("&cYour city has been excluded from &e" + nation.getName()));
                }
            }
        } else {
            player.sendMessage(color("&cFailed to exclude city!"));
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
                .replace("{cities}", String.valueOf(nation.getMemberCities().size()));
    }

    private String color(String text) {
        return text.replace("&", "§");
    }
}
