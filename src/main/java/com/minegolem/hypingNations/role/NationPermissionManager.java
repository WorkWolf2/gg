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

    // Mapping dei ruoli HypingTeams alle configurazioni della nazione
    private static final String OWNER_ROLE = "Owner";      // Chief della nazione
    private static final String DEPUTY_MAYOR_ROLE = "deputy_mayor";

    public NationPermissionManager(HypingNations plugin) {
        this.plugin = plugin;
    }

    /**
     * Verifica se un player ha un permesso specifico della nazione.
     * Utilizza i ruoli di HypingTeams per determinare i permessi.
     *
     * @param player UUID del player
     * @param nation Nazione di riferimento
     * @param permission Permesso da verificare (es: "can_invite_city_to_nation")
     * @return true se il player ha il permesso
     */
    public boolean hasPermission(UUID player, Nation nation, String permission) {
        if (nation == null) {
            return false;
        }

        // Il chief ha sempre tutti i permessi
        if (nation.getChief().equals(player)) {
            return true;
        }

        // Trova la città del player nella nazione
        String cityName = getCityName(player, nation);
        if (cityName == null) {
            return false;
        }

        Team team = TeamAPI.getTeamByName(cityName);
        if (team == null) {
            return false;
        }

        // Determina il tipo di ruolo e verifica il permesso
        NationConfig config = plugin.getConfigManager().getNationConfig();

        // Owner della città = Mayor nella nazione
        if (team.getOwner().equals(player)) {
            return checkPermission(config.getMayorRole(), permission);
        }

        // Deputy Mayor
        RoleMember roleMember = team.getRoleMembers().get(player);
        if (roleMember != null && roleMember.hasRole(DEPUTY_MAYOR_ROLE)) {
            return checkPermission(config.getDeputyMayorRole(), permission);
        }

        // Membri normali non hanno permessi di gestione nazione
        return false;
    }

    /**
     * Ottiene il nome della città di un player in una nazione
     */
    private String getCityName(UUID player, Nation nation) {
        return nation.getMemberCities().stream()
                .filter(city -> city.getAllMembers().contains(player))
                .map(city -> city.teamName())
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica se una configurazione di ruolo ha un permesso specifico
     */
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

    /**
     * Ottiene il range di invito effettivo per un player
     */
    public int getInviteRange(UUID player, Nation nation) {
        if (nation == null) {
            return 0;
        }

        NationConfig config = plugin.getConfigManager().getNationConfig();

        // Il chief usa la configurazione chief
        if (nation.getChief().equals(player)) {
            int chiefRange = config.getChiefRole().getInviteCityRangeBlocks();
            if (chiefRange > 0) {
                return chiefRange;
            }
        }

        // Calcola il range base + bonus per città
        return config.getBaseRecruitRange() +
                config.getPerCityBonusRange() * (nation.getMemberCities().size() - 1);
    }

    /**
     * Ottiene il nome visualizzato del ruolo di un player nella nazione
     */
    public String getRoleDisplayName(UUID player, Nation nation) {
        if (nation == null) {
            return "None";
        }

        NationConfig config = plugin.getConfigManager().getNationConfig();

        // Chief
        if (nation.getChief().equals(player)) {
            return config.getChiefRole().getDisplayName();
        }

        // Trova la città del player
        String cityName = getCityName(player, nation);
        if (cityName == null) {
            return "None";
        }

        Team team = TeamAPI.getTeamByName(cityName);
        if (team == null) {
            return "None";
        }

        // Mayor (owner della città)
        if (team.getOwner().equals(player)) {
            return config.getMayorRole().getDisplayName();
        }

        // Deputy Mayor
        RoleMember roleMember = team.getRoleMembers().get(player);
        if (roleMember != null && roleMember.hasRole(DEPUTY_MAYOR_ROLE)) {
            return config.getDeputyMayorRole().getDisplayName();
        }

        // Membro normale
        return "Member";
    }

    /**
     * Verifica se un player è un deputy mayor nella sua città
     */
    public boolean isDeputyMayor(UUID player, String cityName) {
        Team team = TeamAPI.getTeamByName(cityName);
        if (team == null) {
            return false;
        }

        RoleMember roleMember = team.getRoleMembers().get(player);
        return roleMember != null && roleMember.hasRole(DEPUTY_MAYOR_ROLE);
    }

    /**
     * Verifica se un player è il mayor (owner) della sua città
     */
    public boolean isMayor(UUID player, String cityName) {
        Team team = TeamAPI.getTeamByName(cityName);
        if (team == null) {
            return false;
        }

        return team.getOwner().equals(player);
    }
}
