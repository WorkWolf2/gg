package com.minegolem.hypingNations.listener;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.Nation;
import dev.canable.hypingteams.HypingTeams;
import dev.canable.hypingteams.api.events.TeamClaimEvent;
import dev.canable.hypingteams.manager.ClaimManager;
import dev.canable.hypingteams.object.Team;
import dev.canable.hypingteams.object.claim.Chunk;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class TeamClaimListener implements Listener {

    private final HypingNations plugin;

    @EventHandler
    public void onTeamClaim(TeamClaimEvent event) {
        if (!plugin.getConfigManager().getNationConfig().isBlockOverclaimWithPact()) {
            return;
        }

        plugin.getPactManager().cleanupExpiredPacts();

        Player player = event.getPlayer();
        String claimingTeamName = event.getTeam().getName();
        Chunk chunk = event.getChunk();

        try {
            HypingTeams hypingTeams = HypingTeams.getInstance();
            if (hypingTeams == null) return;

            ClaimManager claimManager = hypingTeams.getClaimManager();
            if (claimManager == null) return;

            if (!claimManager.isChunkClaimed(chunk)) return;

            String targetTeamName = claimManager.getChunkOwner(chunk);
            if (targetTeamName == null) return;

            if (targetTeamName.equals(claimingTeamName)) return;

            Nation claimingNation = plugin.getNationManager().getNationByCity(claimingTeamName);
            Nation targetNation = plugin.getNationManager().getNationByCity(targetTeamName);

            if (claimingNation == null || targetNation == null) return;

            if (plugin.getPactManager().isOverclaimBlocked(claimingNation, targetNation)) {
                event.setCancelled(true);

                player.sendMessage("§8§m                                                ");
                player.sendMessage("§c§lClaim Blocked!");
                player.sendMessage("");
                player.sendMessage("§7You cannot claim territory from §e" + targetNation.getName());
                player.sendMessage("§7because your nation §e" + claimingNation.getName() + " §7has an");
                player.sendMessage("§7active diplomatic pact with them.");
                player.sendMessage("");
                player.sendMessage("§7To claim this territory, your nation chief must");
                player.sendMessage("§7first break the pact using §f/hnations pact break");
                player.sendMessage("§8§m                                                ");

                plugin.getLogger().info(
                        String.format("Blocked overclaim attempt: %s (nation: %s) tried to claim chunk from %s (nation: %s) - Active pact exists",
                                claimingTeamName,
                                claimingNation.getName(),
                                targetTeamName,
                                targetNation.getName()
                        )
                );
            }
        } catch (Exception e) {
            // Log error but don't crash - allow claim to proceed if there's an error
            plugin.getLogger().warning("Error checking pact protection for claim: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
