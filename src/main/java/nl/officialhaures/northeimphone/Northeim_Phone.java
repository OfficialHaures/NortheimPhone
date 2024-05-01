package nl.officialhaures.northeimphone;

import nl.officialhaures.northeimphone.listeners.PhoneClick;
import nl.officialhaures.northeimphone.manager.PincodeManager;
import nl.officialhaures.northeimphone.manager.PlayerData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class Northeim_Phone extends JavaPlugin {

    private PincodeManager pincodeManager;
    private PlayerData playerData;
    private Northeim_Phone plugin;
    UUID playerId;

    @Override
    public void onEnable() {
        plugin = this;
        playerData = new PlayerData(playerId);
        pincodeManager = new PincodeManager();
        pincodeManager.createTable();
        getServer().getPluginManager().registerEvents(new PhoneClick(plugin), this);
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public PlayerData getPlayerData() {
        return playerData;
    }
}
