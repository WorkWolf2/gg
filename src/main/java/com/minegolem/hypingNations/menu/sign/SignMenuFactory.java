package com.minegolem.hypingNations.menu.sign;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

public final class SignMenuFactory {

    private final Plugin plugin;
    private final Map<Player, Menu> inputs;

    public SignMenuFactory(Plugin plugin) {
        this.plugin = plugin;
        this.inputs = new ConcurrentHashMap<>();

        Bukkit.getPluginManager().registerEvents(new MenuListener(), plugin);
    }

    public Menu newMenu() {
        return new Menu();
    }

    public final class Menu {

        private String[] lines;
        private BiPredicate<Player, String[]> response;

        private Menu() {
            this.lines = new String[]{"", "", "", ""};
        }

        public Menu withLines(String... lines) {
            if (lines.length > 4) {
                throw new IllegalArgumentException("Sign can only have 4 lines");
            }
            this.lines = Arrays.copyOf(lines, 4);
            return this;
        }

        public Menu response(BiPredicate<Player, String[]> response) {
            this.response = response;
            return this;
        }

        public void open(Player player) {
            Objects.requireNonNull(player, "player");
            Objects.requireNonNull(response, "response");

            inputs.put(player, this);

            try {
                Location location = player.getLocation();
                Material signType = Material.OAK_SIGN;

                Class<?> packetPlayOutOpenSignClass = getNMSClass("PacketPlayOutOpenSignEditor");
                Class<?> blockPositionClass = getNMSClass("BlockPosition");
                Class<?> packetClass = getNMSClass("Packet");

                Object blockPosition = blockPositionClass.getConstructor(int.class, int.class, int.class)
                        .newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());

                Object packet = packetPlayOutOpenSignClass.getConstructor(blockPositionClass, boolean.class)
                        .newInstance(blockPosition, true);

                sendPacket(player, packet);

            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage("§cFailed to open sign menu. Please try again.");
            }
        }

        private void handleResponse(Player player, String[] lines) {
            if (response.test(player, lines)) {
                inputs.remove(player);
            } else {
                open(player);
            }
        }
    }

    private final class MenuListener implements Listener {

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            inputs.remove(event.getPlayer());
        }
    }

    private void sendPacket(Player player, Object packet) {
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object entityPlayer = getHandle.invoke(player);

            Field connectionField = entityPlayer.getClass().getField("b");
            Object connection = connectionField.get(entityPlayer);

            Method sendPacket = connection.getClass().getMethod("a", getNMSClass("Packet"));
            sendPacket.invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Class<?> getNMSClass(String name) {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
