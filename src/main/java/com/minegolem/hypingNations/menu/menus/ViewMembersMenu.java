package com.minegolem.hypingNations.menu.menus;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.CityRef;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.menu.config.MenuConfig;
import com.minegolem.hypingNations.role.NationRole;
import com.minegolem.hypingNations.menu.MenuManager;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.Team;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ViewMembersMenu {

    private final HypingNations plugin;
    private final MenuManager menuManager;

    public ViewMembersMenu(HypingNations plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }

    public Inventory create(Player player, Nation nation, CityRef city, int page) {
        Team team = TeamAPI.getTeamByName(city.teamName());
        if (team == null) {
            return Bukkit.createInventory(null, 27, color("&cCity not found"));
        }

        MenuConfig config = menuManager.getMenuConfig();
        String title = color(config.getViewMembersTitle()
                .replace("{city}", city.teamName())
                .replace("{page}", String.valueOf(page + 1)));

        Inventory inv = Bukkit.createInventory(null, config.getViewMembersSize(), title);

        Set<UUID> members = team.getMembers();
        List<UUID> memberList = new ArrayList<>(members);

        UUID chief = team.getOwner();
        if (memberList.contains(chief)) {
            memberList.remove(chief);
            memberList.add(0, chief);
        }

        List<Integer> slots = config.getMemberSlots();
        int startIndex = page * slots.size();
        int endIndex = Math.min(startIndex + slots.size(), memberList.size());

        boolean canManageRoles = plugin.getNationMemberManager()
                .hasPermission(nation.getId(), player.getUniqueId(), "can_manage_roles");

        for (int i = startIndex; i < endIndex; i++) {
            UUID memberId = memberList.get(i);
            int slotIndex = i - startIndex;

            if (slotIndex < slots.size()) {
                ItemStack memberItem = createMemberItem(config, nation, memberId, chief, canManageRoles);
                inv.setItem(slots.get(slotIndex), memberItem);
            }
        }

        if (page > 0 && config.getPreviousPageButtonGeneral() != null) {
            inv.setItem(config.getPreviousPageButtonGeneral().getSlot(),
                    createNavigationItem(config.getPreviousPageButtonGeneral(), "Previous Page"));
        }

        if (endIndex < memberList.size() && config.getNextPageButtonGeneral() != null) {
            inv.setItem(config.getNextPageButtonGeneral().getSlot(),
                    createNavigationItem(config.getNextPageButtonGeneral(), "Next Page"));
        }

        // Back button
        if (config.getBackButton() != null) {
            inv.setItem(config.getBackButton().getSlot(),
                    createNavigationItem(config.getBackButton(), "Back to Cities"));
        }

        return inv;
    }

    private ItemStack createMemberItem(MenuConfig config, Nation nation, UUID memberId, UUID chief, boolean canManageRoles) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberId);
        MenuConfig.MenuItem memberButton = config.getMemberButton();

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (meta != null) {
            meta.setOwningPlayer(offlinePlayer);

            String name = offlinePlayer.getName();
            if (name == null) name = "Unknown";

            if (memberId.equals(chief)) {
                meta.setDisplayName(color("&6" + name + " - Chief"));
            } else if (memberId.equals(nation.getChief())) {
                meta.setDisplayName(color("&6" + name + " - Chief of Nation"));
            } else {
                meta.setDisplayName(color("&e" + name));
            }

            List<String> lore = new ArrayList<>();

            NationRole role = plugin.getNationMemberManager().getRole(nation.getId(), memberId);
            lore.add(color("&7Role: &f" + role.getDisplayName()));
            lore.add("");

            if (canManageRoles && !memberId.equals(nation.getChief()) && !memberId.equals(chief)) {
                lore.add(color("&eClick to change role"));
            }

            meta.setLore(lore);

            if (memberButton != null && memberButton.isGlowing()) {
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

            if (menuItem.isGlowing()) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public void handleClick(Player player, int slot, Nation nation, CityRef city, MenuManager.ActiveMenu activeMenu) {
        Team team = TeamAPI.getTeamByName(city.teamName());
        if (team == null) return;

        MenuConfig config = menuManager.getMenuConfig();

        if (config.getPreviousPageButtonGeneral() != null &&
                slot == config.getPreviousPageButtonGeneral().getSlot() &&
                activeMenu.getPage() > 0) {
            player.closeInventory();
            Inventory inv = create(player, nation, city, activeMenu.getPage() - 1);
            player.openInventory(inv);
            activeMenu.setPage(activeMenu.getPage() - 1);
            return;
        }

        if (config.getNextPageButtonGeneral() != null &&
                slot == config.getNextPageButtonGeneral().getSlot()) {
            player.closeInventory();
            Inventory inv = create(player, nation, city, activeMenu.getPage() + 1);
            player.openInventory(inv);
            activeMenu.setPage(activeMenu.getPage() + 1);
            return;
        }

        if (config.getBackButton() != null && slot == config.getBackButton().getSlot()) {
            player.closeInventory();
            menuManager.openManageCitiesMenu(player, 0);
            return;
        }

        List<Integer> slots = config.getMemberSlots();
        int slotIndex = slots.indexOf(slot);
        if (slotIndex == -1) return;

        Set<UUID> members = team.getMembers();
        List<UUID> memberList = new ArrayList<>(members);

        UUID chief = team.getOwner();
        if (memberList.contains(chief)) {
            memberList.remove(chief);
            memberList.add(0, chief);
        }

        int memberIndex = activeMenu.getPage() * slots.size() + slotIndex;
        if (memberIndex < memberList.size()) {
            UUID memberId = memberList.get(memberIndex);

            boolean canManageRoles = plugin.getNationMemberManager()
                    .hasPermission(nation.getId(), player.getUniqueId(), "can_manage_roles");

            if (canManageRoles && !memberId.equals(nation.getChief()) && !memberId.equals(chief)) {
                openRoleSelectionMenu(player, nation, memberId, city);
            }
        }
    }

    private void openRoleSelectionMenu(Player player, Nation nation, UUID targetId, CityRef city) {
        MenuConfig config = menuManager.getMenuConfig();
        String title = color(config.getRoleSelectionTitle());

        Inventory inv = Bukkit.createInventory(null, config.getRoleSelectionSize(), title);

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);
        String targetName = target.getName() != null ? target.getName() : "Unknown";

        if (config.getRoleSelectionInfoButton() != null) {
            ItemStack info = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta infoMeta = (SkullMeta) info.getItemMeta();
            if (infoMeta != null) {
                infoMeta.setOwningPlayer(target);
                infoMeta.setDisplayName(color(config.getRoleSelectionInfoButton().getName()
                        .replace("{player}", targetName)));

                List<String> lore = new ArrayList<>();
                for (String line : config.getRoleSelectionInfoButton().getLore()) {
                    lore.add(color(line.replace("{player}", targetName)));
                }
                infoMeta.setLore(lore);
                info.setItemMeta(infoMeta);
            }
            inv.setItem(config.getRoleSelectionInfoButton().getSlot(), info);
        }

        if (config.getRoleMayorButton() != null) {
            inv.setItem(config.getRoleMayorButton().getSlot(),
                    createRoleItem(config.getRoleMayorButton(), NationRole.MAYOR));
        }
        if (config.getRoleDeputyMayorButton() != null) {
            inv.setItem(config.getRoleDeputyMayorButton().getSlot(),
                    createRoleItem(config.getRoleDeputyMayorButton(), NationRole.DEPUTY_MAYOR));
        }
        if (config.getRoleCitizenButton() != null) {
            inv.setItem(config.getRoleCitizenButton().getSlot(),
                    createRoleItem(config.getRoleCitizenButton(), NationRole.CITIZEN));
        }

        if (config.getBackButton() != null) {
            ItemStack cancel = new ItemStack(config.getBackButton().getMaterial());
            ItemMeta cancelMeta = cancel.getItemMeta();
            if (cancelMeta != null) {
                cancelMeta.setDisplayName(color(config.getBackButton().getName()));
                cancel.setItemMeta(cancelMeta);
            }
            inv.setItem(config.getBackButton().getSlot(), cancel);
        }

        player.openInventory(inv);

        MenuManager.ActiveMenu menu = new MenuManager.ActiveMenu(
                MenuManager.MenuType.ROLE_SELECTION, nation.getId(), 0
        );
        menu.setActionData(new RoleSelectionData(targetId, city));
        menuManager.getActiveMenus().put(player.getUniqueId(), menu);
    }

    private ItemStack createRoleItem(MenuConfig.MenuItem menuItem, NationRole role) {
        ItemStack item = new ItemStack(menuItem.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color(menuItem.getName().replace("{role}", role.getDisplayName())));

            List<String> lore = new ArrayList<>();
            for (String line : menuItem.getLore()) {
                String processedLine = line.replace("{role}", role.getDisplayName());
                lore.add(color(processedLine));
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

    public void handleRoleSelection(Player player, int slot, Nation nation, RoleSelectionData data) {
        MenuConfig config = menuManager.getMenuConfig();
        NationRole selectedRole = null;

        if (config.getRoleMayorButton() != null && slot == config.getRoleMayorButton().getSlot()) {
            selectedRole = NationRole.MAYOR;
        } else if (config.getRoleDeputyMayorButton() != null && slot == config.getRoleDeputyMayorButton().getSlot()) {
            selectedRole = NationRole.DEPUTY_MAYOR;
        } else if (config.getRoleCitizenButton() != null && slot == config.getRoleCitizenButton().getSlot()) {
            selectedRole = NationRole.CITIZEN;
        } else if (config.getBackButton() != null && slot == config.getBackButton().getSlot()) {
            player.closeInventory();
            Inventory inv = create(player, nation, data.city, 0);
            player.openInventory(inv);
            return;
        }

        if (selectedRole != null) {
            plugin.getNationMemberManager().setRole(nation.getId(), data.targetId, selectedRole);

            OfflinePlayer target = Bukkit.getOfflinePlayer(data.targetId);
            String targetName = target.getName() != null ? target.getName() : "Unknown";

            player.sendMessage(color("&aSet role of &e" + targetName + " &ato &e" + selectedRole.getDisplayName()));

            player.closeInventory();
            Inventory inv = create(player, nation, data.city, 0);
            player.openInventory(inv);
        }
    }

    private String color(String text) {
        return text.replace("&", "§");
    }

    public record RoleSelectionData(UUID targetId, CityRef city) {}
}
