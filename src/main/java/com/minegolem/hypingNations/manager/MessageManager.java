package com.minegolem.hypingNations.manager;

import com.minegolem.hypingNations.HypingNations;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final HypingNations plugin;
    /**
     * -- GETTER --
     *  Get prefix
     */
    @Getter
    private String prefix;
    private final Map<String, String> messages = new HashMap<>();

    public MessageManager(HypingNations plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    /**
     * Load all messages from config
     */
    public void loadMessages() {
        messages.clear();

        ConfigurationSection messagesSection = plugin.getConfig().getConfigurationSection("messages");
        if (messagesSection == null) {
            plugin.getLogger().warning("Messages section not found in config!");
            return;
        }

        prefix = messagesSection.getString("prefix", "§8[§6HNations§8]§r ");

        loadMessagesRecursive(messagesSection, "");

        plugin.getLogger().info("Loaded " + messages.size() + " messages");
    }

    /**
     * Recursively load messages from config sections
     */
    private void loadMessagesRecursive(ConfigurationSection section, String path) {
        for (String key : section.getKeys(false)) {
            if (key.equals("prefix")) continue;

            String currentPath = path.isEmpty() ? key : path + "." + key;

            if (section.isConfigurationSection(key)) {
                loadMessagesRecursive(section.getConfigurationSection(key), currentPath);
            } else {
                String message = section.getString(key);
                if (message != null) {
                    messages.put(currentPath, message);
                }
            }
        }
    }

    /**
     * Get a message by key
     */
    public String getMessage(String key) {
        return messages.getOrDefault(key, "§cMessage not found: " + key);
    }

    /**
     * Get a message with placeholders replaced
     */
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);

        // Always replace prefix
        message = message.replace("{prefix}", prefix);

        // Replace custom placeholders
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return message;
    }

    /**
     * Send a message to a player
     */
    public void sendMessage(Player player, String key) {
        sendMessage(player, key, null);
    }

    /**
     * Send a message to a player with placeholders
     */
    public void sendMessage(Player player, String key, Map<String, String> placeholders) {
        String message = getMessage(key, placeholders);

        // Handle multi-line messages
        if (message.contains("\n")) {
            for (String line : message.split("\n")) {
                player.sendMessage(line);
            }
        } else {
            player.sendMessage(message);
        }
    }

    /**
     * Create a placeholder map builder
     */
    public static PlaceholderBuilder placeholder() {
        return new PlaceholderBuilder();
    }

    /**
     * Builder for placeholder maps
     */
    public static class PlaceholderBuilder {
        private final Map<String, String> placeholders = new HashMap<>();

        public PlaceholderBuilder add(String key, String value) {
            placeholders.put(key, value);
            return this;
        }

        public PlaceholderBuilder add(String key, int value) {
            placeholders.put(key, String.valueOf(value));
            return this;
        }

        public PlaceholderBuilder add(String key, double value) {
            placeholders.put(key, String.valueOf(value));
            return this;
        }

        public PlaceholderBuilder add(String key, long value) {
            placeholders.put(key, String.valueOf(value));
            return this;
        }

        public Map<String, String> build() {
            return placeholders;
        }
    }

    /**
     * Reload messages
     */
    public void reload() {
        loadMessages();
    }

}
