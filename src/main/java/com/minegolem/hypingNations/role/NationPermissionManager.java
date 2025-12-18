package com.minegolem.hypingNations.role;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.config.NationConfig;
import com.minegolem.hypingNations.data.Nation;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.RoleMember;
import dev.canable.hypingteams.object.Team;

import java.util.UUID;

public class NationPermissionManager {

    private final HypingNations plugin;

    private static final String OWNER_ROLE = "Owner";
    private static final String DEPUTY_MAYOR_ROLE = "deputy_mayor";

    public NationPermissionManager(HypingNations plugin) {
        this.plugin = plugin;
    }

    public boolean hasPermission(UUID player, Nation nation, String permission) {
        if (nation == null) {
            return false;
        }

        if (nation.getChief().equals(player)) {
            return true;
        }

        String cityName = getCityName(player, nation);
        if (cityName == null) {
            return false;
        }

        Team team = TeamAPI.getTeamByName(cityName);
        if (team == null) {
            return false;
        }

        NationConfig config = plugin.getConfigManager().getNationConfig();

        if (team.getOwner().equals(player)) {
            return checkPermission(config.getMayorRole(), permission);
        }

        RoleMember roleMember = team.getRoleMembers().get(player);
        if (roleMember != null && roleMember.hasRole(DEPUTY_MAYOR_ROLE)) {
            return checkPermission(config.getDeputyMayorRole(), permission);
        }

        return false;
    }

    private String getCityName(UUID player, Nation nation) {
        return nation.getMemberCities().stream()
                .filter(city -> city.getAllMembers().contains(player))
                .map(city -> city.teamName())
                .findFirst()
                .orElse(null);
    }

    private boolean checkPermission(NationConfig.RoleConfig roleConfig, String permission) {
        if (roleConfig == null) {
            return false;
        }

        return switch (permission.toLowerCase()) {
            case "can_invite_city_to_nation" -> roleConfig.isCanInviteCityToNation();
            case "can_accept_city" -> roleConfig.isCanAcceptCity();
            case "can_remove_city" -> roleConfig.isCanRemoveCity();
            case "can_propose_pact" -> roleConfig.isCanProposePact();
            case "can_create_pact" -> roleConfig.isCanCreatePact();
            case "can_break_pact" -> roleConfig.isCanBreakPact();
            case "can_transfer_leadership" -> roleConfig.isCanTransferLeadership();
            case "can_delete_nation" -> roleConfig.isCanDeleteNation();
            case "can_invite_player" -> roleConfig.isCanInvitePlayer();
            case "can_kick_player" -> roleConfig.isCanKickPlayer();
            default -> false;
        };
    }

    public int getInviteRange(UUID player, Nation nation) {
        if (nation == null) {
            return 0;
        }

        NationConfig config = plugin.getConfigManager().getNationConfig();

        if (nation.getChief().equals(player)) {
            int chiefRange = config.getChiefRole().getInviteCityRangeBlocks();
            if (chiefRange > 0) {
                return chiefRange;
            }
        }

        return config.getBaseRecruitRange() +
                config.getPerCityBonusRange() * (nation.getMemberCities().size() - 1);
    }

    public String getRoleDisplayName(UUID player, Nation nation) {
        if (nation == null) {
            return "None";
        }

        NationConfig config = plugin.getConfigManager().getNationConfig();

        if (nation.getChief().equals(player)) {
            return config.getChiefRole().getDisplayName();
        }

        String cityName = getCityName(player, nation);
        if (cityName == null) {
            return "None";
        }

        Team team = TeamAPI.getTeamByName(cityName);
        if (team == null) {
            return "None";
        }

        if (team.getOwner().equals(player)) {
            return config.getMayorRole().getDisplayName();
        }

        RoleMember roleMember = team.getRoleMembers().get(player);
        if (roleMember != null && roleMember.hasRole(DEPUTY_MAYOR_ROLE)) {
            return config.getDeputyMayorRole().getDisplayName();
        }

        return "Member";
    }

    public boolean isDeputyMayor(UUID player, String cityName) {
        Team team = TeamAPI.getTeamByName(cityName);
        if (team == null) {
            return false;
        }

        RoleMember roleMember = team.getRoleMembers().get(player);
        return roleMember != null && roleMember.hasRole(DEPUTY_MAYOR_ROLE);
    }

    public boolean isMayor(UUID player, String cityName) {
        Team team = TeamAPI.getTeamByName(cityName);
        if (team == null) {
            return false;
        }

        return team.getOwner().equals(player);
    }
}
