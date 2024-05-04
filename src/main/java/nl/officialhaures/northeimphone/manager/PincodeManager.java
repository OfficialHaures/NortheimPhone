package nl.officialhaures.northeimphone.manager;

import nl.officialhaures.northeimphone.Northeim_Phone;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PincodeManager {
    public static Northeim_Phone plugin;
    private static final String DB_USER = plugin.getConfig().getString("database.username");
    private static final String DB_PASS = plugin.getConfig().getString("database.password");
    private static final String DB_PORT = plugin.getConfig().getString("database.port");
    private static final String DB_IP   = plugin.getConfig().getString("database.ip");
    private static final String DB_NAME = plugin.getConfig().getString("database.name");
    private static final String DB_URL  = "jdbc:mysql://"+DB_IP+":"+DB_PORT+"/"+DB_NAME;



    public static Map<UUID, String> pincodes = new HashMap<>();
    private static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            createTable();
            loadPincodesFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setPincode(UUID playerId, String pincode) {
        pincodes.put(playerId, pincode);
        addPincode(playerId, pincode);
    }

    public static boolean checkPincode(UUID playerId, String pincode) {
        String storedPincode = getPincodeFromDatabase(playerId);
        return storedPincode != null && storedPincode.equals(pincode);
    }

    public static boolean hasPincode(UUID playerId) {
        return pincodes.containsKey(playerId);
    }

    public static void createTable() {
        try {
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS pincodes (player_id VARCHAR(36) PRIMARY KEY, pincode VARCHAR(4))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addPincode(UUID playerId, String pincode) {
        try {
            String sql = "INSERT INTO pincodes (player_id, pincode) VALUES (?, ?) ON DUPLICATE KEY UPDATE pincode = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, playerId.toString());
            stmt.setString(2, pincode);
            stmt.setString(3, pincode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getPincodeFromDatabase(UUID playerId) {
        try {
            String sql = "SELECT pincode FROM pincodes WHERE player_id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, playerId.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("pincode");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void loadPincodesFromDatabase() {
        try {
            String sql = "SELECT player_id, pincode FROM pincodes";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UUID playerId = UUID.fromString(rs.getString("player_id"));
                String pincode = rs.getString("pincode");
                pincodes.put(playerId, pincode);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
