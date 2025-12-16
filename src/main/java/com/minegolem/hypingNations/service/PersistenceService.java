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

public interface PersistenceService {

    List<Nation> loadNations();

    void saveNations(Collection<Nation> nations);

    void saveNation(Nation nation);

    void deleteNation(UUID nationId);
}

