package com.minegolem.hypingNations.data.database;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.CityRef;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.PactManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager implements AutoCloseable {
    private final HypingNations plugin;
    @Getter
    private HikariDataSource dataSource;
    private final String tablePrefix = "hnations_";

    public DatabaseManager(HypingNations plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize database connection
     */
    public void initialize() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        HikariConfig config = new HikariConfig();

        String host = plugin.getConfig().getString("database.mariadb.host", "localhost");
        int port = plugin.getConfig().getInt("database.mariadb.port", 3306);
        String database = plugin.getConfig().getString("database.mariadb.database", "hypingnations");
        String username = plugin.getConfig().getString("database.mariadb.username", "root");
        String password = plugin.getConfig().getString("database.mariadb.password", "");

        config.setJdbcUrl(String.format("jdbc:mariadb://%s:%d/%s", host, port, database));
        config.setUsername(username);
        config.setPassword(password);

        config.setMinimumIdle(plugin.getConfig().getInt("database.mariadb.pool.minimum-idle", 2));
        config.setMaximumPoolSize(plugin.getConfig().getInt("database.mariadb.pool.maximum-pool-size", 10));
        config.setConnectionTimeout(plugin.getConfig().getLong("database.mariadb.pool.connection-timeout", 30000));
        config.setIdleTimeout(plugin.getConfig().getLong("database.mariadb.pool.idle-timeout", 600000));
        config.setMaxLifetime(plugin.getConfig().getLong("database.mariadb.pool.max-lifetime", 1800000));

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        this.dataSource = new HikariDataSource(config);

        createTables();
        plugin.getLogger().info("Database initialized successfully!");
    }

    /**
     * Create database tables
     */
    private void createTables() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            String nationsTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "nations (" +
                    "id VARCHAR(36) PRIMARY KEY," +
                    "name VARCHAR(64) UNIQUE NOT NULL," +
                    "chief VARCHAR(36) NOT NULL," +
                    "capital VARCHAR(64) NOT NULL," +
                    "treasury DOUBLE DEFAULT 0.0," +
                    "unpaid_days INT DEFAULT 0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_name (name)," +
                    "INDEX idx_chief (chief)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

            String citiesTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "cities (" +
                    "nation_id VARCHAR(36) NOT NULL," +
                    "team_name VARCHAR(64) NOT NULL," +
                    "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (nation_id, team_name)," +
                    "FOREIGN KEY (nation_id) REFERENCES " + tablePrefix + "nations(id) ON DELETE CASCADE," +
                    "INDEX idx_team_name (team_name)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

            String pactsTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "pacts (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "proposer_id VARCHAR(36) NOT NULL," +
                    "target_id VARCHAR(36) NOT NULL," +
                    "duration_days INT NOT NULL," +
                    "start_date DATE," +
                    "active BOOLEAN DEFAULT FALSE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (proposer_id) REFERENCES " + tablePrefix + "nations(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (target_id) REFERENCES " + tablePrefix + "nations(id) ON DELETE CASCADE," +
                    "UNIQUE KEY unique_pact (proposer_id, target_id)," +
                    "INDEX idx_proposer (proposer_id)," +
                    "INDEX idx_target (target_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(nationsTable);
                stmt.execute(citiesTable);
                stmt.execute(pactsTable);
            }
        }
    }

    /**
     * Save a nation to database (async)
     */
    public CompletableFuture<Void> saveNationAsync(Nation nation) {
        return CompletableFuture.runAsync(() -> {
            try {
                saveNation(nation);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save nation " + nation.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Save a nation to database (sync)
     */
    public void saveNation(Nation nation) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String nationSql = "INSERT INTO " + tablePrefix + "nations " +
                        "(id, name, chief, capital, treasury, unpaid_days) VALUES (?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name=?, chief=?, capital=?, treasury=?, unpaid_days=?";

                try (PreparedStatement stmt = conn.prepareStatement(nationSql)) {
                    String nationId = nation.getId().toString();
                    stmt.setString(1, nationId);
                    stmt.setString(2, nation.getName());
                    stmt.setString(3, nation.getChief().toString());
                    stmt.setString(4, nation.getCapital().teamName());
                    stmt.setDouble(5, nation.getTreasury());
                    stmt.setInt(6, nation.getUnpaidDays());
                    // ON DUPLICATE KEY UPDATE
                    stmt.setString(7, nation.getName());
                    stmt.setString(8, nation.getChief().toString());
                    stmt.setString(9, nation.getCapital().teamName());
                    stmt.setDouble(10, nation.getTreasury());
                    stmt.setInt(11, nation.getUnpaidDays());
                    stmt.executeUpdate();
                }

                String deleteCities = "DELETE FROM " + tablePrefix + "cities WHERE nation_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteCities)) {
                    stmt.setString(1, nation.getId().toString());
                    stmt.executeUpdate();
                }

                String insertCity = "INSERT INTO " + tablePrefix + "cities (nation_id, team_name) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertCity)) {
                    for (CityRef city : nation.getMemberCities()) {
                        stmt.setString(1, nation.getId().toString());
                        stmt.setString(2, city.teamName());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                savePactsForNation(conn, nation);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Save pacts for a nation
     */
    private void savePactsForNation(Connection conn, Nation nation) throws SQLException {
        String deletePacts = "DELETE FROM " + tablePrefix + "pacts WHERE proposer_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deletePacts)) {
            stmt.setString(1, nation.getId().toString());
            stmt.executeUpdate();
        }

        String insertPact = "INSERT INTO " + tablePrefix + "pacts " +
                "(proposer_id, target_id, duration_days, start_date, active) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insertPact)) {
            for (PactManager.Pact pact : nation.getActivePacts().values()) {
                stmt.setString(1, nation.getId().toString());
                stmt.setString(2, pact.getTarget().getId().toString());
                stmt.setInt(3, pact.getDurationDays());

                if (pact.getStartDate() != null) {
                    stmt.setDate(4, Date.valueOf(pact.getStartDate()));
                } else {
                    stmt.setNull(4, Types.DATE);
                }

                stmt.setBoolean(5, pact.isActive());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    /**
     * Load all nations from database
     */
    public List<Nation> loadAllNations() throws SQLException {
        List<Nation> nations = new ArrayList<>();
        Map<UUID, Nation> nationMap = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            String nationSql = "SELECT * FROM " + tablePrefix + "nations";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(nationSql)) {

                while (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("id"));
                    String name = rs.getString("name");
                    UUID chief = UUID.fromString(rs.getString("chief"));
                    String capitalName = rs.getString("capital");

                    try {
                        CityRef capital = new CityRef(capitalName);
                        Nation nation = new Nation(id, name, chief, capital);
                        nation.setTreasury(rs.getDouble("treasury"));
                        nation.setUnpaidDays(rs.getInt("unpaid_days"));

                        nations.add(nation);
                        nationMap.put(id, nation);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Capital city no longer exists for nation " + name + ", skipping...");
                    }
                }
            }

            String citySql = "SELECT * FROM " + tablePrefix + "cities";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(citySql)) {

                while (rs.next()) {
                    UUID nationId = UUID.fromString(rs.getString("nation_id"));
                    String teamName = rs.getString("team_name");

                    Nation nation = nationMap.get(nationId);
                    if (nation != null && !teamName.equals(nation.getCapital().teamName())) {
                        try {
                            CityRef city = new CityRef(teamName);
                            nation.getMemberCities().add(city);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("City " + teamName + " no longer exists, skipping...");
                        }
                    }
                }
            }
        }

        return nations;
    }

    /**
     * Load pacts from database
     */
    public Map<String, PactManager.Pact> loadAllPacts(Map<UUID, Nation> nationMap) throws SQLException {
        Map<String, PactManager.Pact> pacts = new HashMap<>();

        String sql = "SELECT * FROM " + tablePrefix + "pacts WHERE active = TRUE";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UUID proposerId = UUID.fromString(rs.getString("proposer_id"));
                UUID targetId = UUID.fromString(rs.getString("target_id"));
                int durationDays = rs.getInt("duration_days");
                Date startDate = rs.getDate("start_date");
                boolean active = rs.getBoolean("active");

                Nation proposer = nationMap.get(proposerId);
                Nation target = nationMap.get(targetId);

                if (proposer != null && target != null) {
                    PactManager.Pact pact = new PactManager.Pact(proposer, target, durationDays);
                    if (active && startDate != null) {
                        pact.activate();
                        // Set start date via reflection or add setter
                        java.lang.reflect.Field field = PactManager.Pact.class.getDeclaredField("startDate");
                        field.setAccessible(true);
                        field.set(pact, startDate.toLocalDate());
                    }

                    String key = proposer.getName() + "->" + target.getName();
                    pacts.put(key, pact);
                    proposer.addPact(pact);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().severe("Failed to restore pact dates: " + e.getMessage());
        }

        return pacts;
    }

    /**
     * Delete a nation from database
     */
    public CompletableFuture<Void> deleteNationAsync(UUID nationId) {
        return CompletableFuture.runAsync(() -> {
            try {
                deleteNation(nationId);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete nation: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Delete a nation from database (sync)
     */
    public void deleteNation(UUID nationId) throws SQLException {
        String sql = "DELETE FROM " + tablePrefix + "nations WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nationId.toString());
            stmt.executeUpdate();
        }
    }

    /**
     * Create database backup
     */
    public CompletableFuture<Boolean> createBackupAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createBackup();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create backup: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    /**
     * Create database backup (sync)
     */
    private boolean createBackup() throws Exception {
        String host = plugin.getConfig().getString("database.mariadb.host", "localhost");
        int port = plugin.getConfig().getInt("database.mariadb.port", 3306);
        String database = plugin.getConfig().getString("database.mariadb.database", "hypingnations");
        String username = plugin.getConfig().getString("database.mariadb.username", "root");
        String password = plugin.getConfig().getString("database.mariadb.password", "");

        java.io.File backupDir = new java.io.File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(System.currentTimeMillis()));
        java.io.File backupFile = new java.io.File(backupDir, "backup_" + timestamp + ".sql");

        String command = String.format("mysqldump -h%s -P%d -u%s -p%s %s",
                host, port, username, password, database);

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command + " > " + backupFile.getAbsolutePath());
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            plugin.getLogger().info("Database backup created: " + backupFile.getName());
            cleanOldBackups(backupDir);
            return true;
        } else {
            plugin.getLogger().warning("Backup failed with exit code: " + exitCode);
            return false;
        }
    }

    /**
     * Clean old backups
     */
    private void cleanOldBackups(java.io.File backupDir) {
        int keepBackups = plugin.getConfig().getInt("database.backup.keep-backups", 7);
        java.io.File[] backups = backupDir.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".sql"));

        if (backups != null && backups.length > keepBackups) {
            Arrays.sort(backups, Comparator.comparingLong(java.io.File::lastModified));

            for (int i = 0; i < backups.length - keepBackups; i++) {
                if (backups[i].delete()) {
                    plugin.getLogger().info("Deleted old backup: " + backups[i].getName());
                }
            }
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection closed");
        }
    }
}
