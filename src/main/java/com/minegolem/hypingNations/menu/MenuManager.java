package com.minegolem.hypingNations.menu;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.CityRef;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.menu.config.MenuConfig;
import com.minegolem.hypingNations.menu.menus.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuManager implements Listener {

    private final HypingNations plugin;
    @Getter
    private MenuConfig menuConfig;

    // Active menu tracking
    @Getter
    private final Map<UUID, ActiveMenu> activeMenus = new ConcurrentHashMap<>();

    // Menu types
    private ManageNationMenu manageNationMenu;
    private ManageCitiesMenu manageCitiesMenu;
    private ManagePactsMenu managePactsMenu;
    private ConfirmationMenu confirmationMenu;
    private TaxHistoryMenu taxHistoryMenu;
    private ViewMembersMenu viewMembersMenu;

    public MenuManager(HypingNations plugin) {
        this.plugin = plugin;
        loadConfig();
        initializeMenus();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void loadConfig() {
        this.menuConfig = new MenuConfig(
                Objects.requireNonNull(plugin.getConfig().getConfigurationSection("menus"))
        );
    }

    private void initializeMenus() {
        manageNationMenu = new ManageNationMenu(plugin, this);
        manageCitiesMenu = new ManageCitiesMenu(plugin, this);
        managePactsMenu = new ManagePactsMenu(plugin, this);
        confirmationMenu = new ConfirmationMenu(plugin, this);
        taxHistoryMenu = new TaxHistoryMenu(plugin, this);
        viewMembersMenu = new ViewMembersMenu(plugin, this);
    }

    public void openViewMembersMenu(Player player, Nation nation, CityRef city, int page) {
        Inventory inv = viewMembersMenu.create(player, nation, city, page);
        player.openInventory(inv);

        ActiveMenu menu = new ActiveMenu(MenuType.VIEW_MEMBERS, nation.getId(), page);
        menu.setActionData(city);
        activeMenus.put(player.getUniqueId(), menu);
    }

    public void openManageNationMenu(Player player) {
        Nation nation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (nation == null) {
            plugin.getMessageManager().sendMessage(player, "info.not-in-nation");
            return;
        }

        Inventory inv = manageNationMenu.create(player, nation, 0);
        player.openInventory(inv);
        activeMenus.put(player.getUniqueId(), new ActiveMenu(MenuType.MANAGE_NATION, nation.getId(), 0));
    }

    public void openManageCitiesMenu(Player player, int page) {
        Nation nation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (nation == null) {
            plugin.getMessageManager().sendMessage(player, "info.not-in-nation");
            return;
        }

        Inventory inv = manageCitiesMenu.create(player, nation, page);
        player.openInventory(inv);
        activeMenus.put(player.getUniqueId(), new ActiveMenu(MenuType.MANAGE_CITIES, nation.getId(), page));
    }

    public void openManagePactsMenu(Player player, int page) {
        Nation nation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (nation == null) {
            plugin.getMessageManager().sendMessage(player, "info.not-in-nation");
            return;
        }

        Inventory inv = managePactsMenu.create(player, nation, page);
        player.openInventory(inv);
        activeMenus.put(player.getUniqueId(), new ActiveMenu(MenuType.MANAGE_PACTS, nation.getId(), page));
    }

    public void openConfirmationMenu(Player player, String actionType, Runnable onConfirm, Object data) {
        Nation nation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (nation == null) return;

        Inventory inv = confirmationMenu.create(player, actionType, nation, data);
        player.openInventory(inv);

        ActiveMenu menu = new ActiveMenu(MenuType.CONFIRMATION, nation.getId(), 0);
        menu.setConfirmAction(onConfirm);
        menu.setActionData(data);
        activeMenus.put(player.getUniqueId(), menu);
    }

    public void openTaxHistoryMenu(Player player, int page) {
        Nation nation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (nation == null) {
            plugin.getMessageManager().sendMessage(player, "info.not-in-nation");
            return;
        }

        Inventory inv = taxHistoryMenu.create(player, nation, page);
        player.openInventory(inv);
        activeMenus.put(player.getUniqueId(), new ActiveMenu(MenuType.TAX_HISTORY, nation.getId(), page));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        ActiveMenu activeMenu = activeMenus.get(player.getUniqueId());
        if (activeMenu == null) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;

        Nation nation = plugin.getNationManager().getNation(activeMenu.getNationId());
        if (nation == null) {
            player.closeInventory();
            return;
        }

        switch (activeMenu.getType()) {
            case MANAGE_NATION -> manageNationMenu.handleClick(player, e.getSlot(), nation, activeMenu);
            case MANAGE_CITIES -> manageCitiesMenu.handleClick(player, e.getSlot(), nation, activeMenu);
            case MANAGE_PACTS -> managePactsMenu.handleClick(player, e.getSlot(), nation, activeMenu);
            case CONFIRMATION -> confirmationMenu.handleClick(player, e.getSlot(), activeMenu);
            case TAX_HISTORY -> taxHistoryMenu.handleClick(player, e.getSlot(), nation, activeMenu);
            case VIEW_MEMBERS -> {
                CityRef city = (CityRef) activeMenu.getActionData();
                if (city != null) {
                    viewMembersMenu.handleClick(player, e.getSlot(), nation, city, activeMenu);
                }
            }
            case CITY_ACTION -> {
                CityRef city = (CityRef) activeMenu.getActionData();
                if (city != null) {
                    manageCitiesMenu.handleCityAction(player, e.getSlot(), nation, city);
                }
            }
            case ROLE_SELECTION -> {
                ViewMembersMenu.RoleSelectionData data =
                        (ViewMembersMenu.RoleSelectionData) activeMenu.getActionData();
                if (data != null) {
                    viewMembersMenu.handleRoleSelection(player, e.getSlot(), nation, data);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player player) {
            activeMenus.remove(player.getUniqueId());
        }
    }

    public void reload() {
        loadConfig();
        initializeMenus();
    }

    @Getter
    public static class ActiveMenu {
        private final MenuType type;
        private final UUID nationId;
        @Setter
        private int page;
        @Setter
        private Runnable confirmAction;
        @Setter
        private Object actionData;

        public ActiveMenu(MenuType type, UUID nationId, int page) {
            this.type = type;
            this.nationId = nationId;
            this.page = page;
        }

    }

    public enum MenuType {
        MANAGE_NATION,
        MANAGE_CITIES,
        MANAGE_PACTS,
        CONFIRMATION,
        TAX_HISTORY,
        VIEW_MEMBERS,
        CITY_ACTION,
        ROLE_SELECTION
    }

}
