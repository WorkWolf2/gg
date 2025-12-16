package com.minegolem.hypingNations.role;

import com.minegolem.hypingNations.config.NationConfig;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NationRolesManager {

    @Getter
    private final Map<UUID, NationRole> playerRoles = new HashMap<>();
    private final Map<String, NationRoleDefinition> roleDefinitions = new HashMap<>();

    public void loadRoles(NationConfig config) {
        roleDefinitions.put("chief", new NationRoleDefinition("chief", config.getChiefRole()));
        roleDefinitions.put("mayor", new NationRoleDefinition("mayor", config.getMayorRole()));
        roleDefinitions.put("deputy_mayor", new NationRoleDefinition("deputy_mayor", config.getDeputyMayorRole()));
    }

    public void assignRole(UUID player, String roleId) {
        NationRoleDefinition def = roleDefinitions.get(roleId);
        if (def != null) {
            playerRoles.put(player, new NationRole(def));
        }
    }

    public void removeRole(UUID player) {
        playerRoles.remove(player);
    }

    public NationRole getPlayerRole(UUID player) {
        return playerRoles.get(player);
    }

    public boolean hasPermission(UUID player, String permission) {
        NationRole role = playerRoles.get(player);
        return role != null && role.hasPermission(permission);
    }

    public String getRoleId(UUID player) {
        NationRole role = playerRoles.get(player);
        return role != null ? role.def().id() : null;
    }

    public String getDisplayName(UUID player) {
        NationRole role = playerRoles.get(player);
        return role != null ? role.def().config().getDisplayName() : "None";
    }

    public record NationRoleDefinition(String id, NationConfig.RoleConfig config) {}

    public record NationRole(NationRoleDefinition def) {

        public boolean hasPermission(String perm) {
            return switch (perm.toLowerCase()) {
                case "can_accept_city" -> def.config().isCanAcceptCity();
                case "can_remove_city" -> def.config().isCanRemoveCity();
                case "can_create_pact" -> def.config().isCanCreatePact();
                case "can_break_pact" -> def.config().isCanBreakPact();
                case "can_transfer_leadership" -> def.config().isCanTransferLeadership();
                case "can_delete_nation" -> def.config().isCanDeleteNation();
                case "can_invite_player" -> def.config().isCanInvitePlayer();
                case "can_kick_player" -> def.config().isCanKickPlayer();
                case "can_invite_city_to_nation" -> def.config().isCanInviteCityToNation();
                case "can_propose_pact" -> def.config().isCanProposePact();
                default -> false;
            };
        }
    }
}
