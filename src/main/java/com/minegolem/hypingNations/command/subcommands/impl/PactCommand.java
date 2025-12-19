package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.MessageManager;
import com.minegolem.hypingNations.manager.PactManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PactCommand implements SubCommand {
    private final HypingNations plugin;

    public PactCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args.length < 1) {
            plugin.getMessageManager().sendMessage(player, "pact.usage");
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "propose" -> handlePropose(player, args);
            case "accept" -> handleAccept(player, args);
            case "deny" -> handleDeny(player, args);
            case "break" -> handleBreak(player, args);
            default -> plugin.getMessageManager().sendMessage(player, "pact.unknown-action");
        }
    }

    private void handlePropose(Player player, String[] args) {
        if (args.length < 3) {
            plugin.getMessageManager().sendMessage(player, "pact.propose.usage");
            return;
        }

        Nation playerNation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (playerNation == null) {
            plugin.getMessageManager().sendMessage(player, "pact.propose.not-in-nation");
            return;
        }

        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), playerNation, "can_propose_pact")) {
            plugin.getMessageManager().sendMessage(player, "pact.propose.no-permission");
            return;
        }

        String targetName = args[1];
        Nation targetNation = plugin.getNationManager().getNation(targetName);
        if (targetNation == null) {
            plugin.getMessageManager().sendMessage(player, "pact.propose.nation-not-found",
                    MessageManager.placeholder()
                            .add("nation_name", targetName)
                            .build()
            );
            return;
        }

        if (targetNation.equals(playerNation)) {
            plugin.getMessageManager().sendMessage(player, "pact.propose.cannot-pact-self");
            return;
        }

        int days;
        try {
            days = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendMessage(player, "pact.propose.invalid-days");
            return;
        }

        int maxDuration = plugin.getConfigManager().getNationConfig().getMaxPactDuration();
        if (days <= 0 || days > maxDuration) {
            plugin.getMessageManager().sendMessage(player, "pact.propose.invalid-duration",
                    MessageManager.placeholder()
                            .add("max_duration", maxDuration)
                            .build()
            );
            return;
        }

        PactManager.Pact pact = plugin.getPactManager().proposePact(playerNation, targetNation, days);

        plugin.getMessageManager().sendMessage(player, "pact.propose.success",
                MessageManager.placeholder()
                        .add("nation_name", targetName)
                        .add("days", days)
                        .build()
        );

        Player targetChief = plugin.getServer().getPlayer(targetNation.getChief());
        if (targetChief != null && targetChief.isOnline()) {
            plugin.getMessageManager().sendMessage(targetChief, "pact.propose.notify-target",
                    MessageManager.placeholder()
                            .add("proposer_nation", playerNation.getName())
                            .add("days", days)
                            .build()
            );
        }
    }

    private void handleAccept(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getMessageManager().sendMessage(player, "pact.accept.usage");
            return;
        }

        Nation playerNation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (playerNation == null) {
            plugin.getMessageManager().sendMessage(player, "pact.accept.not-in-nation");
            return;
        }

        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), playerNation, "can_create_pact")) {
            plugin.getMessageManager().sendMessage(player, "pact.accept.no-permission");
            return;
        }

        String proposerName = args[1];
        Nation proposerNation = plugin.getNationManager().getNation(proposerName);
        if (proposerNation == null) {
            plugin.getMessageManager().sendMessage(player, "pact.accept.nation-not-found",
                    MessageManager.placeholder()
                            .add("nation_name", proposerName)
                            .build()
            );
            return;
        }

        boolean success = plugin.getPactManager().acceptPact(proposerNation, playerNation);
        if (success) {
            plugin.getMessageManager().sendMessage(player, "pact.accept.success",
                    MessageManager.placeholder()
                            .add("nation_name", proposerName)
                            .build()
            );

            Player proposerChief = plugin.getServer().getPlayer(proposerNation.getChief());
            if (proposerChief != null && proposerChief.isOnline()) {
                plugin.getMessageManager().sendMessage(proposerChief, "pact.accept.notify-proposer",
                        MessageManager.placeholder()
                                .add("target_nation", playerNation.getName())
                                .build()
                );
            }
        } else {
            plugin.getMessageManager().sendMessage(player, "pact.accept.no-pending");
        }
    }

    private void handleDeny(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getMessageManager().sendMessage(player, "pact.deny.usage");
            return;
        }

        Nation playerNation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (playerNation == null) {
            plugin.getMessageManager().sendMessage(player, "pact.deny.not-in-nation");
            return;
        }

        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), playerNation, "can_create_pact")) {
            plugin.getMessageManager().sendMessage(player, "pact.deny.no-permission");
            return;
        }

        String proposerName = args[1];
        Nation proposerNation = plugin.getNationManager().getNation(proposerName);
        if (proposerNation == null) {
            plugin.getMessageManager().sendMessage(player, "pact.deny.nation-not-found",
                    MessageManager.placeholder()
                            .add("nation_name", proposerName)
                            .build()
            );
            return;
        }

        boolean success = plugin.getPactManager().denyPact(proposerNation, playerNation);
        if (success) {
            plugin.getMessageManager().sendMessage(player, "pact.deny.success",
                    MessageManager.placeholder()
                            .add("nation_name", proposerName)
                            .build()
            );

            Player proposerChief = plugin.getServer().getPlayer(proposerNation.getChief());
            if (proposerChief != null && proposerChief.isOnline()) {
                plugin.getMessageManager().sendMessage(proposerChief, "pact.deny.notify-proposer",
                        MessageManager.placeholder()
                                .add("target_nation", playerNation.getName())
                                .build()
                );
            }
        } else {
            plugin.getMessageManager().sendMessage(player, "pact.deny.no-pending");
        }
    }

    private void handleBreak(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getMessageManager().sendMessage(player, "pact.break.usage");
            return;
        }

        Nation playerNation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (playerNation == null) {
            plugin.getMessageManager().sendMessage(player, "pact.break.not-in-nation");
            return;
        }

        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), playerNation, "can_break_pact")) {
            plugin.getMessageManager().sendMessage(player, "pact.break.no-permission");
            return;
        }

        String targetName = args[1];
        Nation targetNation = plugin.getNationManager().getNation(targetName);
        if (targetNation == null) {
            plugin.getMessageManager().sendMessage(player, "pact.break.nation-not-found",
                    MessageManager.placeholder()
                            .add("nation_name", targetName)
                            .build()
            );
            return;
        }

        if (!playerNation.hasActivePactWith(targetNation.getId())) {
            plugin.getMessageManager().sendMessage(player, "pact.break.no-active-pact",
                    MessageManager.placeholder()
                            .add("nation_name", targetName)
                            .build()
            );
            return;
        }

        boolean success = plugin.getPactManager().breakPact(playerNation, targetNation);
        if (success) {
            plugin.getMessageManager().sendMessage(player, "pact.break.success",
                    MessageManager.placeholder()
                            .add("nation_name", targetName)
                            .build()
            );

            Player targetChief = plugin.getServer().getPlayer(targetNation.getChief());
            if (targetChief != null && targetChief.isOnline()) {
                plugin.getMessageManager().sendMessage(targetChief, "pact.break.notify-target",
                        MessageManager.placeholder()
                                .add("initiator_nation", playerNation.getName())
                                .build()
                );
            }

            plugin.getLogger().info(
                    String.format("Pact broken: %s broke pact with %s",
                            playerNation.getName(),
                            targetNation.getName()
                    )
            );
        } else {
            plugin.getMessageManager().sendMessage(player, "pact.break.failed");
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