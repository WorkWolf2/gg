package com.minegolem.hypingNations.menu.sign;

import dev.canable.hypingteams.lib.signgui.SignGUI;
import dev.canable.hypingteams.lib.signgui.SignGUIAction;
import dev.canable.hypingteams.lib.signgui.exception.SignGUIVersionException;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

public final class SignMenuFactory {

    private final Plugin plugin;

    public SignMenuFactory(Plugin plugin) {
        this.plugin = plugin;
    }

    public Menu newMenu() {
        return new Menu();
    }

    public final class Menu {

        private String[] lines = new String[]{"", "", "", ""};
        private BiPredicate<Player, String[]> response;

        private Menu() {
        }

        public Menu withLines(String... lines) {
            if (lines.length > 4) {
                throw new IllegalArgumentException("A sign can only have 4 lines");
            }
            this.lines = new String[]{"", "", "", ""};
            System.arraycopy(lines, 0, this.lines, 0, lines.length);
            return this;
        }

        public Menu response(BiPredicate<Player, String[]> response) {
            this.response = response;
            return this;
        }

        public void open(Player player) {
            Objects.requireNonNull(player, "player");
            Objects.requireNonNull(response, "response");

            try {
                SignGUI gui = SignGUI.builder()
                        .setLines(lines)
                        .setType(Material.OAK_SIGN)
                        .setColor(DyeColor.BLACK)
                        .setHandler((p, result) -> {
                            String[] input = result.getLinesWithoutColor();

                            boolean accepted = response.test(p, input);

                            if (accepted) {
                                return Collections.emptyList();
                            }

                            return List.of(
                                    SignGUIAction.displayNewLines(input)
                            );
                        })
                        .build();

                gui.open(player);

            } catch (SignGUIVersionException e) {
                player.sendMessage("§cThis server version does not support Sign GUI.");
                e.printStackTrace();
            }
        }
    }
}

