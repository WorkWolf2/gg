package com.minegolem.hypingNations.menu.menus;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.CityRef;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.role.NationRole;
import com.minegolem.hypingNations.menu.MenuManager;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.Team;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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

        String title = color("&6Members: &e" + city.teamName() + " &7(Page " + (page + 1) + ")");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        Set<UUID> members = team.getMembers();
        List<UUID> memberList = new ArrayList<>(members);

        // Always put chief first
        UUID chief = team.getOwner();
        if (memberList.contains(chief)) {
            memberList.remove(chief);
            memberList.add(0, chief);
        }

        int itemsPerPage = 45; // Slots 0-44
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, memberList.size());

        boolean canManageRoles = plugin.getNationMemberManager()
                .hasPermission(nation.getId(), player.getUniqueId(), "can_manage_roles");

        // Display members
        for (int i = startIndex; i < endIndex; i++) {
            UUID memberId = memberList.get(i);
            int slot = i - startIndex;

            ItemStack memberItem = createMemberItem(nation, memberId, chief, canManageRoles);
            inv.setItem(slot, memberItem);
        }

        // Navigation
        if (page > 0) {
            inv.setItem(45, createNavigationItem(Material.ARROW, "&e← Previous Page"));
        }

        if (endIndex < memberList.size()) {
            inv.setItem(53, createNavigationItem(Material.ARROW, "&eNext Page →"));
        }

        // Back button
        inv.setItem(49, createNavigationItem(Material.BARRIER, "&cBack to Cities"));

        return inv;
    }

    private ItemStack createMemberItem(Nation nation, UUID memberId, UUID chief, boolean canManageRoles) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberId);

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (meta != null) {
            meta.setOwningPlayer(offlinePlayer);

            String name = offlinePlayer.getName();
            if (name == null) name = "Unknown";

            // Chief gets special formatting
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
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createNavigationItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        item.getItemMeta().setDisplayName(color(name));
        return item;
    }

    public void handleClick(Player player, int slot, Nation nation, CityRef city, MenuManager.ActiveMenu activeMenu) {
        Team team = TeamAPI.getTeamByName(city.teamName());
        if (team == null) return;

        // Navigation
        if (slot == 45 && activeMenu.getPage() > 0) {
            player.closeInventory();
            Inventory inv = create(player, nation, city, activeMenu.getPage() - 1);
            player.openInventory(inv);
            activeMenu.setPage(activeMenu.getPage() - 1);
            return;
        }

        if (slot == 53) {
            player.closeInventory();
            Inventory inv = create(player, nation, city, activeMenu.getPage() + 1);
            player.openInventory(inv);
            activeMenu.setPage(activeMenu.getPage() + 1);
            return;
        }

        // Back button
        if (slot == 49) {
            player.closeInventory();
            menuManager.openManageCitiesMenu(player, 0);
            return;
        }

        // Member slot clicked
        if (slot >= 0 && slot < 45) {
            Set<UUID> members = team.getMembers();
            List<UUID> memberList = new ArrayList<>(members);

            // Always put chief first
            UUID chief = team.getOwner();
            if (memberList.contains(chief)) {
                memberList.remove(chief);
                memberList.add(0, chief);
            }

            int memberIndex = activeMenu.getPage() * 45 + slot;
            if (memberIndex < memberList.size()) {
                UUID memberId = memberList.get(memberIndex);

                boolean canManageRoles = plugin.getNationMemberManager()
                        .hasPermission(nation.getId(), player.getUniqueId(), "can_manage_roles");

                // Can't change chief or city owner role
                if (canManageRoles && !memberId.equals(nation.getChief()) && !memberId.equals(chief)) {
                    openRoleSelectionMenu(player, nation, memberId, city);
                }
            }
        }
    }

    private void openRoleSelectionMenu(Player player, Nation nation, UUID targetId, CityRef city) {
        Inventory inv = Bukkit.createInventory(null, 27, color("&6Select Role"));

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);
        String targetName = target.getName() != null ? target.getName() : "Unknown";

        // Info item
        ItemStack info = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta infoMeta = (SkullMeta) info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setOwningPlayer(target);
            infoMeta.setDisplayName(color("&e" + targetName));
            infoMeta.setLore(List.of(color("&7Select a role below")));
            info.setItemMeta(infoMeta);
        }
        inv.setItem(4, info);

        // Role options
        inv.setItem(11, createRoleItem(NationRole.MAYOR));
        inv.setItem(13, createRoleItem(NationRole.DEPUTY_MAYOR));
        inv.setItem(15, createRoleItem(NationRole.CITIZEN));

        // Cancel
        ItemStack cancel = new ItemStack(Material.BARRIER);
        cancel.getItemMeta().setDisplayName(color("&cCancel"));
        inv.setItem(22, cancel);

        player.openInventory(inv);

        // Store context
        MenuManager.ActiveMenu menu = new MenuManager.ActiveMenu(
                MenuManager.MenuType.ROLE_SELECTION, nation.getId(), 0
        );
        menu.setActionData(new RoleSelectionData(targetId, city));
        menuManager.getActiveMenus().put(player.getUniqueId(), menu);
    }

    private ItemStack createRoleItem(NationRole role) {
        Material material = switch (role) {
            case MAYOR -> Material.DIAMOND;
            case DEPUTY_MAYOR -> Material.GOLD_INGOT;
            case CITIZEN -> Material.IRON_INGOT;
            default -> Material.STONE;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color("&e" + role.getDisplayName()));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(color("&7Permissions:"));
            if (role.isCanInviteCities()) lore.add(color("&a✓ &7Invite Cities"));
            if (role.isCanCreatePacts()) lore.add(color("&a✓ &7Create Pacts"));
            if (role.isCanManageCities()) lore.add(color("&a✓ &7Manage Cities"));

            if (!role.isCanInviteCities() && !role.isCanCreatePacts() && !role.isCanManageCities()) {
                lore.add(color("&7No special permissions"));
            }

            lore.add("");
            lore.add(color("&eClick to assign"));

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    public void handleRoleSelection(Player player, int slot, Nation nation, RoleSelectionData data) {
        NationRole selectedRole = null;

        switch (slot) {
            case 11 -> selectedRole = NationRole.MAYOR;
            case 13 -> selectedRole = NationRole.DEPUTY_MAYOR;
            case 15 -> selectedRole = NationRole.CITIZEN;
            case 22 -> {
                player.closeInventory();
                Inventory inv = create(player, nation, data.city, 0);
                player.openInventory(inv);
                return;
            }
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
