package com.minegolem.hypingNations.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

@Getter
public class NationConfig {
    // ECONOMY
    private int minMembers;
    private int minClaims;
    private int creationPrice;

    private boolean taxEnabled;
    private String taxCollectionPeriod;
    private double taxPerChunk;
    private int maxUnpaidDays;
    private String taxChargeSource;

    // RANGE
    private int baseRecruitRange;
    private int perCityBonusRange;

    // DIPLOMACY
    private boolean pactsEnabled;
    private int maxPactDuration;
    private boolean blockOverclaimWithPact;

    // OVERCLAIM
    private boolean overclaimEnabled;

    // STRUCTURE - Roles
    private RoleConfig chiefRole;
    private RoleConfig mayorRole;
    private RoleConfig deputyMayorRole;

    public NationConfig(ConfigurationSection section) {
        loadEconomy(Objects.requireNonNull(section.getConfigurationSection("economy")));
        loadRange(Objects.requireNonNull(section.getConfigurationSection("range")));
        loadDiplomacy(Objects.requireNonNull(section.getConfigurationSection("diplomacy")));
        loadOverclaim(Objects.requireNonNull(section.getConfigurationSection("overclaim")));
        loadStructure(Objects.requireNonNull(section.getConfigurationSection("structure")));
    }

    private void loadEconomy(ConfigurationSection economy) {
        ConfigurationSection creation = economy.getConfigurationSection("creation");
        assert creation != null;
        this.minMembers = creation.getInt("minimum-member");
        this.minClaims = creation.getInt("minimum-claim");
        this.creationPrice = creation.getInt("price");

        ConfigurationSection tax = economy.getConfigurationSection("tax");
        assert tax != null;
        this.taxEnabled = tax.getBoolean("enabled");
        this.taxCollectionPeriod = tax.getString("collection_period");
        this.taxPerChunk = tax.getDouble("per_chunk_price");
        this.maxUnpaidDays = tax.getInt("max_unpaid_days_before_dissolution");
        this.taxChargeSource = tax.getString("charge_source");
    }

    private void loadRange(ConfigurationSection range) {
        this.baseRecruitRange = range.getInt("base_recruit_range_blocks");
        this.perCityBonusRange = range.getInt("per_city_bonus_range_blocks");
    }

    private void loadDiplomacy(ConfigurationSection diplomacy) {
        ConfigurationSection pacts = diplomacy.getConfigurationSection("pacts");

        assert pacts != null;
        this.pactsEnabled = pacts.getBoolean("enabled");
        this.maxPactDuration = pacts.getInt("max_duration_days");
        this.blockOverclaimWithPact = pacts.getBoolean("block_overclaim_between_partners");
    }

    private void loadOverclaim(ConfigurationSection overclaim) {
        this.overclaimEnabled = overclaim.getBoolean("enabled");
    }

    private void loadStructure(ConfigurationSection structure) {
        ConfigurationSection roles = structure.getConfigurationSection("roles");
        assert roles != null;
        ConfigurationSection nationRoles = roles.getConfigurationSection("nation");
        ConfigurationSection cityRoles = roles.getConfigurationSection("city");

        assert nationRoles != null;
        this.chiefRole = new RoleConfig(Objects.requireNonNull(nationRoles.getConfigurationSection("chief")));

        assert cityRoles != null;
        this.mayorRole = new RoleConfig(Objects.requireNonNull(cityRoles.getConfigurationSection("mayor")));
        this.deputyMayorRole = new RoleConfig(Objects.requireNonNull(cityRoles.getConfigurationSection("deputy_mayor")));
    }

    @Getter
    public static class RoleConfig {
        private final String displayName;
        private final boolean canAcceptCity;
        private final boolean canRemoveCity;
        private final boolean canCreatePact;
        private final boolean canBreakPact;
        private final int maxPactDurationDays;
        private final boolean canTransferLeadership;
        private final boolean canDeleteNation;
        private final boolean canInvitePlayer;
        private final boolean canKickPlayer;
        private final boolean canInviteCityToNation;
        private final int inviteCityRangeBlocks;
        private final boolean canProposePact;

        public RoleConfig(ConfigurationSection section) {
            this.displayName = section.getString("display_name", "Role");

            this.canAcceptCity = section.getBoolean("permissions.can_accept_city", false);
            this.canRemoveCity = section.getBoolean("permissions.can_remove_city", false);
            this.canCreatePact = section.getBoolean("permissions.can_create_pact", false);
            this.canBreakPact = section.getBoolean("permissions.can_break_pact", false);
            this.maxPactDurationDays = section.getInt("permissions.max_pact_duration_days", 30);
            this.canTransferLeadership = section.getBoolean("permissions.can_transfer_leadership", false);
            this.canDeleteNation = section.getBoolean("permissions.can_delete_nation", false);
            this.canInvitePlayer = section.getBoolean("permissions.can_invite_player", false);
            this.canKickPlayer = section.getBoolean("permissions.can_kick_player", false);
            this.canInviteCityToNation = section.getBoolean("permissions.can_invite_city_to_nation", false);
            this.inviteCityRangeBlocks = section.getInt("permissions.invite_city_range_blocks", 1000);
            this.canProposePact = section.getBoolean("permissions.can_propose_pact", false);
        }
    }
}
