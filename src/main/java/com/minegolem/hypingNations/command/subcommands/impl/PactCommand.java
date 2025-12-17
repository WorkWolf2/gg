package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.PactManager;
import org.bukkit.entity.Player;

import java.util.List;

public class PactCommand implements SubCommand {
    private final HypingNations plugin;

    public PactCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /hnations pact <propose|accept|deny|break> ...");
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "propose" -> handlePropose(player, args);
            case "accept" -> handleAccept(player, args);
            case "deny" -> handleDeny(player, args);
            case "break" -> handleBreak(player, args);
            default -> player.sendMessage("§cUnknown action. Use: propose, accept, deny, break");
        }
    }

    private void handlePropose(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /hnations pact propose <nation> <days>");
            return;
        }

        Nation playerNation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (playerNation == null) {
            player.sendMessage("§cYou are not part of any nation!");
            return;
        }

        // AGGIORNATO: Usa il nuovo sistema di permessi
        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), playerNation, "can_propose_pact")) {
            player.sendMessage("§cYou don't have permission to propose pacts!");
            player.sendMessage("§7Only the Chief or authorized members can propose pacts.");
            return;
        }

        String targetName = args[1];
        Nation targetNation = plugin.getNationManager().getNation(targetName);
        if (targetNation == null) {
            player.sendMessage("§cNation not found: " + targetName);
            return;
        }

        if (targetNation.equals(playerNation)) {
            player.sendMessage("§cYou cannot create a pact with your own nation!");
            return;
        }

        int days;
        try {
            days = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid number of days!");
            return;
        }

        int maxDuration = plugin.getConfigManager().getNationConfig().getMaxPactDuration();
        if (days <= 0 || days > maxDuration) {
            player.sendMessage("§cPact duration must be between 1 and " + maxDuration + " days!");
            return;
        }

        PactManager.Pact pact = plugin.getPactManager().proposePact(playerNation, targetNation, days);

        player.sendMessage("§aPact proposed to §e" + targetName + " §afor §e" + days + " days§a!");

        Player targetChief = plugin.getServer().getPlayer(targetNation.getChief());
        if (targetChief != null && targetChief.isOnline()) {
            targetChief.sendMessage("§8§m                                                ");
            targetChief.sendMessage("§b§lPact Proposal");
            targetChief.sendMessage("");
            targetChief.sendMessage("§e" + playerNation.getName() + " §7wants to form a pact!");
            targetChief.sendMessage("§7Duration: §f" + days + " days");
            targetChief.sendMessage("");
            targetChief.sendMessage("§7§f/hnations pact accept " + playerNation.getName());
            targetChief.sendMessage("§7§f/hnations pact deny " + playerNation.getName());
            targetChief.sendMessage("§8§m                                                ");
        }
    }

    private void handleAccept(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /hnations pact accept <nation>");
            return;
        }

        Nation playerNation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (playerNation == null) {
            player.sendMessage("§cYou are not part of any nation!");
            return;
        }

        // AGGIORNATO: Usa il nuovo sistema di permessi
        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), playerNation, "can_create_pact")) {
            player.sendMessage("§cYou don't have permission to accept pacts!");
            player.sendMessage("§7Only the Chief or authorized members can accept pacts.");
            return;
        }

        String proposerName = args[1];
        Nation proposerNation = plugin.getNationManager().getNation(proposerName);
        if (proposerNation == null) {
            player.sendMessage("§cNation not found: " + proposerName);
            return;
        }

        boolean success = plugin.getPactManager().acceptPact(proposerNation, playerNation);
        if (success) {
            player.sendMessage("§aPact accepted with §e" + proposerName + "§a!");
            player.sendMessage("§7Your nations are now allies and cannot overclaim each other.");

            Player proposerChief = plugin.getServer().getPlayer(proposerNation.getChief());
            if (proposerChief != null && proposerChief.isOnline()) {
                proposerChief.sendMessage("§a" + playerNation.getName() + " §aaccepted your pact proposal!");
                proposerChief.sendMessage("§7You are now allies and cannot overclaim each other.");
            }
        } else {
            player.sendMessage("§cNo pending pact from that nation!");
        }
    }

    private void handleDeny(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /hnations pact deny <nation>");
            return;
        }

        Nation playerNation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (playerNation == null) {
            player.sendMessage("§cYou are not part of any nation!");
            return;
        }

        // I patti possono essere negati da chiunque abbia permessi di gestione
        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), playerNation, "can_create_pact")) {
            player.sendMessage("§cYou don't have permission to deny pacts!");
            return;
        }

        String proposerName = args[1];
        Nation proposerNation = plugin.getNationManager().getNation(proposerName);
        if (proposerNation == null) {
            player.sendMessage("§cNation not found: " + proposerName);
            return;
        }

        boolean success = plugin.getPactManager().denyPact(proposerNation, playerNation);
        if (success) {
            player.sendMessage("§cPact denied from §e" + proposerName + "§c!");

            Player proposerChief = plugin.getServer().getPlayer(proposerNation.getChief());
            if (proposerChief != null && proposerChief.isOnline()) {
                proposerChief.sendMessage("§c" + playerNation.getName() + " §cdenied your pact proposal!");
            }
        } else {
            player.sendMessage("§cNo pending pact from that nation!");
        }
    }

    private void handleBreak(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /hnations pact break <nation>");
            return;
        }

        Nation playerNation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (playerNation == null) {
            player.sendMessage("§cYou are not part of any nation!");
            return;
        }

        // AGGIORNATO: Usa il nuovo sistema di permessi
        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), playerNation, "can_break_pact")) {
            player.sendMessage("§cYou don't have permission to break pacts!");
            player.sendMessage("§7Only the Chief or authorized members can break pacts.");
            return;
        }

        String targetName = args[1];
        Nation targetNation = plugin.getNationManager().getNation(targetName);
        if (targetNation == null) {
            player.sendMessage("§cNation not found: " + targetName);
            return;
        }

        if (!playerNation.hasActivePactWith(targetNation.getId())) {
            player.sendMessage("§cYou don't have an active pact with §e" + targetName + "§c!");
            return;
        }

        boolean success = plugin.getPactManager().breakPact(playerNation, targetNation);
        if (success) {
            player.sendMessage("§8§m                                                ");
            player.sendMessage("§c§lPact Broken!");
            player.sendMessage("");
            player.sendMessage("§7Your nation has broken the pact with §e" + targetName);
            player.sendMessage("§7You can now overclaim their territory.");
            player.sendMessage("§8§m                                                ");

            Player targetChief = plugin.getServer().getPlayer(targetNation.getChief());
            if (targetChief != null && targetChief.isOnline()) {
                targetChief.sendMessage("§8§m                                                ");
                targetChief.sendMessage("§c§lPact Broken!");
                targetChief.sendMessage("");
                targetChief.sendMessage("§e" + playerNation.getName() + " §7has broken their pact");
                targetChief.sendMessage("§7with your nation!");
                targetChief.sendMessage("§7They can now overclaim your territory.");
                targetChief.sendMessage("§8§m                                                ");
            }

            plugin.getLogger().info(
                    String.format("Pact broken: %s broke pact with %s",
                            playerNation.getName(),
                            targetNation.getName()
                    )
            );
        } else {
            player.sendMessage("§cFailed to break pact. Please contact an administrator.");
        }
    }

    @Override
    public String getName() {
        return "pact";
    }

    @Override
    public String getDescription() {
        return "Manage diplomatic pacts";
    }

    @Override
    public String getUsage() {
        return "/hnations pact <propose|accept|deny|break> ...";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 1) {
            return List.of("propose", "accept", "deny", "break").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            return plugin.getNationManager().getAllNations().stream()
                    .map(Nation::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}