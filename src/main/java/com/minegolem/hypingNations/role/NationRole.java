package com.minegolem.hypingNations.role;

import lombok.Getter;

@Getter
public enum NationRole {
    CHIEF("Chief", 4, true, true, true, true, true, true),
    MAYOR("Mayor", 3, true, true, false, false, false, false),
    DEPUTY_MAYOR("Deputy Mayor", 2, true, true, false, false, false, false),
    CITIZEN("Citizen", 1, false, false, false, false, false, false);

    private final String displayName;
    private final int priority;
    private final boolean canInviteCities;
    private final boolean canCreatePacts;
    private final boolean canManageCities;
    private final boolean canManageRoles;
    private final boolean canDeleteNation;
    private final boolean canManageTreasury;

    NationRole(String displayName, int priority, boolean canInviteCities,
               boolean canCreatePacts, boolean canManageCities,
               boolean canManageRoles, boolean canDeleteNation,
               boolean canManageTreasury) {
        this.displayName = displayName;
        this.priority = priority;
        this.canInviteCities = canInviteCities;
        this.canCreatePacts = canCreatePacts;
        this.canManageCities = canManageCities;
        this.canManageRoles = canManageRoles;
        this.canDeleteNation = canDeleteNation;
        this.canManageTreasury = canManageTreasury;
    }

    public boolean hasPermission(String permission) {
        return switch (permission.toLowerCase()) {
            case "can_invite_cities" -> canInviteCities;
            case "can_create_pacts" -> canCreatePacts;
            case "can_manage_cities" -> canManageCities;
            case "can_manage_roles" -> canManageRoles;
            case "can_delete_nation" -> canDeleteNation;
            case "can_manage_treasury" -> canManageTreasury;
            default -> false;
        };
    }

    public static NationRole fromString(String name) {
        for (NationRole role : values()) {
            if (role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return CITIZEN;
    }
}
