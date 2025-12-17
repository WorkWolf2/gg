package com.minegolem.hypingNations.placeholder;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.Nation;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HypingNationsExpansion extends PlaceholderExpansion {

    private final HypingNations plugin;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public HypingNationsExpansion(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "hnations";
    }

    @Override
    public @NotNull String getAuthor() {
        return "WorkWolf_2";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        Nation nation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());

        switch (params.toLowerCase()) {
            case "nation_name":
                return nation != null ? nation.getName() : "None";

            case "role":
                if (nation == null) return "None";
                // AGGIORNATO: Usa il nuovo sistema di permessi
                return plugin.getPermissionManager().getRoleDisplayName(player.getUniqueId(), nation);

            case "tax_next_time":
                if (nation == null) return "N/A";
                LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0);
                return timeFormatter.format(tomorrow);

            case "tax_amount":
                if (nation == null) return "0";
                double tax = plugin.getTaxManager().calculateTax(nation);
                return df.format(tax);

            case "tax_chunks_count":
                if (nation == null) return "0";
                return String.valueOf(nation.getTotalChunks());

            case "tax_unpaid_days":
                if (nation == null) return "0";
                return String.valueOf(nation.getUnpaidDays());

            case "pacts_active_count":
                if (nation == null) return "0";
                return String.valueOf(nation.getActivePacts().size());

            case "range_effective_blocks":
                if (nation == null) return "0";
                return String.valueOf(plugin.getPermissionManager().getInviteRange(player.getUniqueId(), nation));

            case "capital_name":
                if (nation == null) return "None";
                return nation.getCapital().teamName();

            case "member_cities_count":
                if (nation == null) return "0";
                return String.valueOf(nation.getMemberCities().size());

            case "total_members_count":
                if (nation == null) return "0";
                return String.valueOf(nation.getAllMembers().size());

            case "treasury":
                if (nation == null) return "0";
                return df.format(nation.getTreasury());

            case "chief_name":
                if (nation == null) return "None";
                return plugin.getServer().getOfflinePlayer(nation.getChief()).getName();

            default:
                return null;
        }
    }
}