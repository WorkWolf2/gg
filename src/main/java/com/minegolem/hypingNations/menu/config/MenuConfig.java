package com.minegolem.hypingNations.menu.config;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MenuConfig {

    // Manage Nation Menu
    private String manageNationTitle;
    private int manageNationSize;
    private MenuItem dissolveButton;
    private MenuItem renameButton;
    private MenuItem infoButton;
    private MenuItem taxHistoryButton;
    private List<CustomMenuItem> manageNationCustomItems;

    // Manage Cities Menu
    private String manageCitiesTitle;
    private int manageCitiesSize;
    private List<Integer> citySlots;
    private MenuItem inviteCityButton;
    private MenuItem cityInfoButton;
    private MenuItem excludeCityButton;
    private List<CustomMenuItem> manageCitiesCustomItems;

    // Manage Pacts Menu
    private String managePactsTitle;
    private int managePactsSize;
    private List<Integer> pactSlots;
    private MenuItem createPactButton;
    private MenuItem pactInfoButton;
    private MenuItem breakPactButton;
    private List<CustomMenuItem> managePactsCustomItems;

    // Confirmation Menu
    private String confirmationTitle;
    private MenuItem confirmButton;
    private MenuItem cancelButton;
    private MenuItem infoDisplayButton;

    // Tax History Menu
    private String taxHistoryTitle;
    private int taxHistorySize;
    private MenuItem taxEntryButton;
    private MenuItem previousPageButton;
    private MenuItem nextPageButton;

    // View Members Menu
    private String viewMembersTitle;
    private int viewMembersSize;
    private List<Integer> memberSlots;
    private MenuItem memberButton;

    // Role Selection Menu
    private String roleSelectionTitle;
    private int roleSelectionSize;
    private MenuItem roleSelectionInfoButton;
    private MenuItem roleMayorButton;
    private MenuItem roleDeputyMayorButton;
    private MenuItem roleCitizenButton;

    // Navigation
    private MenuItem previousPageButtonGeneral;
    private MenuItem nextPageButtonGeneral;
    private MenuItem backButton;

    public MenuConfig(ConfigurationSection section) {
        loadManageNationMenu(section.getConfigurationSection("manage-nation"));
        loadManageCitiesMenu(section.getConfigurationSection("manage-cities"));
        loadManagePactsMenu(section.getConfigurationSection("manage-pacts"));
        loadConfirmationMenu(section.getConfigurationSection("confirmation"));
        loadTaxHistoryMenu(section.getConfigurationSection("tax-history"));
        loadViewMembersMenu(section.getConfigurationSection("view-members"));
        loadRoleSelectionMenu(section.getConfigurationSection("role-selection"));
        loadNavigation(section.getConfigurationSection("navigation"));
    }

    private void loadManageNationMenu(ConfigurationSection section) {
        if (section == null) return;

        manageNationTitle = section.getString("title", "&6&lManage Nation");
        manageNationSize = section.getInt("size", 54);

        dissolveButton = loadMenuItem(section.getConfigurationSection("dissolve-button"));
        renameButton = loadMenuItem(section.getConfigurationSection("rename-button"));
        infoButton = loadMenuItem(section.getConfigurationSection("info-button"));
        taxHistoryButton = loadMenuItem(section.getConfigurationSection("tax-history-button"));

        manageNationCustomItems = loadCustomMenuItems(section.getConfigurationSection("custom-items"));
    }

    private void loadManageCitiesMenu(ConfigurationSection section) {
        if (section == null) return;

        manageCitiesTitle = section.getString("title", "&6&lManage Cities");
        manageCitiesSize = section.getInt("size", 54);

        citySlots = section.getIntegerList("city-slots");
        if (citySlots.isEmpty()) {
            for (int i = 0; i < 45; i++) {
                citySlots.add(i);
            }
        }

        inviteCityButton = loadMenuItem(section.getConfigurationSection("invite-city-button"));
        cityInfoButton = loadMenuItem(section.getConfigurationSection("city-info-button"));
        excludeCityButton = loadMenuItem(section.getConfigurationSection("exclude-city-button"));

        manageCitiesCustomItems = loadCustomMenuItems(section.getConfigurationSection("custom-items"));
    }

    private void loadManagePactsMenu(ConfigurationSection section) {
        if (section == null) return;

        managePactsTitle = section.getString("title", "&6&lManage Pacts");
        managePactsSize = section.getInt("size", 54);

        pactSlots = section.getIntegerList("pact-slots");
        if (pactSlots.isEmpty()) {
            for (int i = 0; i < 45; i++) {
                pactSlots.add(i);
            }
        }

        createPactButton = loadMenuItem(section.getConfigurationSection("create-pact-button"));
        pactInfoButton = loadMenuItem(section.getConfigurationSection("pact-info-button"));
        breakPactButton = loadMenuItem(section.getConfigurationSection("break-pact-button"));

        managePactsCustomItems = loadCustomMenuItems(section.getConfigurationSection("custom-items"));
    }

    private void loadConfirmationMenu(ConfigurationSection section) {
        if (section == null) return;

        confirmationTitle = section.getString("title", "&c&lConfirm Action");
        confirmButton = loadMenuItem(section.getConfigurationSection("confirm-button"));
        cancelButton = loadMenuItem(section.getConfigurationSection("cancel-button"));
        infoDisplayButton = loadMenuItem(section.getConfigurationSection("info-display"));
    }

    private void loadTaxHistoryMenu(ConfigurationSection section) {
        if (section == null) return;

        taxHistoryTitle = section.getString("title", "&6&lTax History");
        taxHistorySize = section.getInt("size", 54);
        taxEntryButton = loadMenuItem(section.getConfigurationSection("tax-entry"));
        previousPageButton = loadMenuItem(section.getConfigurationSection("previous-page"));
        nextPageButton = loadMenuItem(section.getConfigurationSection("next-page"));
    }

    private void loadViewMembersMenu(ConfigurationSection section) {
        if (section == null) {
            // Default values
            viewMembersTitle = "&6&lView Members: &e{city} &7(Page {page})";
            viewMembersSize = 54;
            memberSlots = new ArrayList<>();
            for (int i = 0; i < 45; i++) {
                memberSlots.add(i);
            }
            memberButton = new MenuItem(0, Material.PLAYER_HEAD, "&e{player}",
                    List.of("&7Role: &f{role}", "", "&eClick to manage"), 0, false);
            return;
        }

        viewMembersTitle = section.getString("title", "&6&lView Members: &e{city} &7(Page {page})");
        viewMembersSize = section.getInt("size", 54);

        memberSlots = section.getIntegerList("member-slots");
        if (memberSlots.isEmpty()) {
            for (int i = 0; i < 45; i++) {
                memberSlots.add(i);
            }
        }

        memberButton = loadMenuItem(section.getConfigurationSection("member-button"));
    }

    private void loadRoleSelectionMenu(ConfigurationSection section) {
        if (section == null) {
            // Default values
            roleSelectionTitle = "&6&lSelect Role";
            roleSelectionSize = 27;
            roleSelectionInfoButton = new MenuItem(4, Material.PLAYER_HEAD, "&e{player}",
                    List.of("&7Select a role below"), 0, false);
            roleMayorButton = new MenuItem(11, Material.DIAMOND, "&e&lMayor",
                    List.of("", "&7Permissions:", "&a✓ &7Invite Cities", "", "&eClick to assign"), 0, false);
            roleDeputyMayorButton = new MenuItem(13, Material.GOLD_INGOT, "&e&lDeputy Mayor",
                    List.of("", "&7Permissions:", "&a✓ &7Invite Cities", "", "&eClick to assign"), 0, false);
            roleCitizenButton = new MenuItem(15, Material.IRON_INGOT, "&e&lCitizen",
                    List.of("", "&7No special permissions", "", "&eClick to assign"), 0, false);
            return;
        }

        roleSelectionTitle = section.getString("title", "&6&lSelect Role");
        roleSelectionSize = section.getInt("size", 27);

        roleSelectionInfoButton = loadMenuItem(section.getConfigurationSection("info-button"));
        roleMayorButton = loadMenuItem(section.getConfigurationSection("role-mayor"));
        roleDeputyMayorButton = loadMenuItem(section.getConfigurationSection("role-deputy-mayor"));
        roleCitizenButton = loadMenuItem(section.getConfigurationSection("role-citizen"));
    }

    private void loadNavigation(ConfigurationSection section) {
        if (section == null) return;

        previousPageButtonGeneral = loadMenuItem(section.getConfigurationSection("previous-page"));
        nextPageButtonGeneral = loadMenuItem(section.getConfigurationSection("next-page"));
        backButton = loadMenuItem(section.getConfigurationSection("back-button"));
    }

    private MenuItem loadMenuItem(ConfigurationSection section) {
        if (section == null) return null;

        return new MenuItem(
                section.getInt("slot", 0),
                Material.valueOf(section.getString("material", "STONE")),
                section.getString("name", ""),
                section.getStringList("lore"),
                section.getInt("custom-model-data", 0),
                section.getBoolean("glowing", false)
        );
    }

    private List<CustomMenuItem> loadCustomMenuItems(ConfigurationSection section) {
        List<CustomMenuItem> items = new ArrayList<>();
        if (section == null) return items;

        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            if (itemSection == null) continue;

            items.add(new CustomMenuItem(
                    itemSection.getInt("slot", 0),
                    Material.valueOf(itemSection.getString("material", "STONE")),
                    itemSection.getString("name", ""),
                    itemSection.getStringList("lore"),
                    itemSection.getInt("custom-model-data", 0),
                    itemSection.getBoolean("glowing", false),
                    itemSection.getStringList("commands")
            ));
        }

        return items;
    }

    @Getter
    public static class MenuItem {
        private final int slot;
        private final Material material;
        private final String name;
        private final List<String> lore;
        private final int customModelData;
        private final boolean glowing;

        public MenuItem(int slot, Material material, String name, List<String> lore,
                        int customModelData, boolean glowing) {
            this.slot = slot;
            this.material = material;
            this.name = name;
            this.lore = lore;
            this.customModelData = customModelData;
            this.glowing = glowing;
        }
    }

    @Getter
    public static class CustomMenuItem extends MenuItem {
        private final List<String> commands;

        public CustomMenuItem(int slot, Material material, String name, List<String> lore,
                              int customModelData, boolean glowing, List<String> commands) {
            super(slot, material, name, lore, customModelData, glowing);
            this.commands = commands;
        }
    }
}
