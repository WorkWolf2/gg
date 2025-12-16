package com.minegolem.hypingNations.service;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.CityRef;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.PactManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

public class PersistenceService {

    private final HypingNations plugin;
    private final Gson gson;
    private final File dataFolder;

    public PersistenceService(HypingNations plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(UUID.class, new UUIDAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    /**
     * Save all nations to JSON file
     */
    public void saveNations(Collection<Nation> nations) {
        File file = new File(dataFolder, "nations.json");

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            JsonArray nationsArray = new JsonArray();

            for (Nation nation : nations) {
                JsonObject nationObj = serializeNation(nation);
                nationsArray.add(nationObj);
            }

            gson.toJson(nationsArray, writer);
            plugin.getLogger().info("Saved " + nations.size() + " nations to disk");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save nations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load all nations from JSON file
     */
    public List<Nation> loadNations() {
        File file = new File(dataFolder, "nations.json");

        if (!file.exists()) {
            plugin.getLogger().info("No nations data file found, starting fresh");
            return new ArrayList<>();
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonArray nationsArray = gson.fromJson(reader, JsonArray.class);
            List<Nation> nations = new ArrayList<>();

            if (nationsArray != null) {
                for (JsonElement element : nationsArray) {
                    try {
                        Nation nation = deserializeNation(element.getAsJsonObject());
                        nations.add(nation);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load a nation: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            plugin.getLogger().info("Loaded " + nations.size() + " nations from disk");
            return nations;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load nations: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private JsonObject serializeNation(Nation nation) {
        JsonObject obj = new JsonObject();

        obj.addProperty("id", nation.getId().toString());
        obj.addProperty("name", nation.getName());
        obj.addProperty("chief", nation.getChief().toString());
        obj.addProperty("capital", nation.getCapital().teamName());
        obj.addProperty("treasury", nation.getTreasury());
        obj.addProperty("unpaidDays", nation.getUnpaidDays());

        // Member cities
        JsonArray citiesArray = new JsonArray();
        for (CityRef city : nation.getMemberCities()) {
            citiesArray.add(city.teamName());
        }
        obj.add("memberCities", citiesArray);

        // Roles
        JsonObject rolesObj = new JsonObject();
        nation.getRolesManager().getPlayerRoles().forEach((uuid, role) -> {
            rolesObj.addProperty(uuid.toString(), role.def().id());
        });
        obj.add("roles", rolesObj);

        // Pacts
        JsonArray pactsArray = new JsonArray();
        for (PactManager.Pact pact : nation.getActivePacts().values()) {
            JsonObject pactObj = new JsonObject();
            pactObj.addProperty("targetNationId", pact.getTarget().getId().toString());
            pactObj.addProperty("durationDays", pact.getDurationDays());
            pactObj.addProperty("startDate", pact.getStartDate() != null ? pact.getStartDate().toString() : null);
            pactObj.addProperty("active", pact.isActive());
            pactsArray.add(pactObj);
        }
        obj.add("pacts", pactsArray);

        return obj;
    }

    private Nation deserializeNation(JsonObject obj) {
        UUID id = UUID.fromString(obj.get("id").getAsString());
        String name = obj.get("name").getAsString();
        UUID chief = UUID.fromString(obj.get("chief").getAsString());
        String capitalName = obj.get("capital").getAsString();

        CityRef capital = new CityRef(capitalName);
        Nation nation = new Nation(id, name, chief, capital);

        nation.setTreasury(obj.get("treasury").getAsDouble());
        nation.setUnpaidDays(obj.get("unpaidDays").getAsInt());

        JsonArray citiesArray = obj.getAsJsonArray("memberCities");
        for (JsonElement cityElement : citiesArray) {
            String cityName = cityElement.getAsString();
            if (!cityName.equals(capitalName)) {
                try {
                    CityRef city = new CityRef(cityName);
                    nation.getMemberCities().add(city);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("City no longer exists: " + cityName);
                }
            }
        }

        // Load roles
        nation.getRolesManager().loadRoles(plugin.getConfigManager().getNationConfig());
        JsonObject rolesObj = obj.getAsJsonObject("roles");
        if (rolesObj != null) {
            rolesObj.entrySet().forEach(entry -> {
                UUID playerUuid = UUID.fromString(entry.getKey());
                String roleId = entry.getValue().getAsString();
                nation.getRolesManager().assignRole(playerUuid, roleId);
            });
        }

        return nation;
    }

    // Adapter for UUID serialization
    private static class UUIDAdapter extends TypeAdapter<UUID> {
        @Override
        public void write(JsonWriter out, UUID value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public UUID read(JsonReader in) throws IOException {
            return UUID.fromString(in.nextString());
        }
    }

    // Adapter for LocalDate serialization
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            out.value(value != null ? value.toString() : null);
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            String str = in.nextString();
            return str != null ? LocalDate.parse(str) : null;
        }
    }
}
