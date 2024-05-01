package nl.officialhaures.northeimphone.listeners;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import nl.officialhaures.northeimphone.Northeim_Phone;
import nl.officialhaures.northeimphone.manager.LineManager;
import nl.officialhaures.northeimphone.manager.PincodeManager;
import nl.officialhaures.northeimphone.manager.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class PhoneClick implements Listener {

    private static Map<UUID, Map<String, String>> contacts = new HashMap<>();
    private static Map<UUID, Map<String, String>> messages = new HashMap<>();
    public static Map<UUID, PlayerData> playerData = new HashMap<>();
    private static Northeim_Phone plugin;

    public PhoneClick(Northeim_Phone plugin) {
        this.plugin = plugin;
    }

    public PlayerData getPlayerData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, PlayerData::new);
    }

    @EventHandler
    public void onPhoneClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() != null && event.getItem().getType() == Material.INK_SAC) {
                if (!PincodeManager.hasPincode(playerId)) {
                    openPincodeSetupGui(player);
                } else {
                    openPincodeInputGui(player);
                }
            }
        }
    }

    private void openPincodeSetupGui(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerData playerData = getPlayerData(playerId);
        PaginatedGui gui = Gui.paginated()
                .title(Component.text(ChatColor.BLUE + "Set Pincode"))
                .rows(6)
                .pageSize(45)
                .disableAllInteractions()
                .create();

        for (int i = 0; i < 10; i++) {
            ItemBuilder itemBuilder = ItemBuilder.from(new ItemStack(Material.LIME_STAINED_GLASS_PANE))
                    .name(Component.text(ChatColor.WHITE + String.valueOf(i)));
            gui.addItem(new GuiItem(itemBuilder.build(), inventoryClickEvent -> {
                Player clickedPlayer = (Player) inventoryClickEvent.getWhoClicked();
                StringBuilder pincode = playerData.getPincodeBuilder();

                if (!PincodeManager.hasPincode(playerId)) {
                    String clickedNumber = ChatColor.stripColor(inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName());

                    if (pincode.length() < 4) {
                        pincode.append(clickedNumber);
                        clickedPlayer.sendMessage(ChatColor.GREEN + "Pincode: " + pincode.toString());
                    }

                    if (pincode.length() == 4) {
                        PincodeManager.setPincode(playerId, pincode.toString());
                        clickedPlayer.sendMessage(ChatColor.GREEN + "Pincode set successfully!");
                        inventoryClickEvent.getView().close();
                        playerData.resetPincodeBuilder(); // Reset pincode builder
                    }
                } else {
                    clickedPlayer.sendMessage(ChatColor.RED + "You already have a pincode set!");
                }
            }));
        }
        gui.open(player);
    }

    private void openPincodeInputGui(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerData playerData = getPlayerData(playerId);
        PaginatedGui gui = Gui.paginated()
                .title(Component.text(ChatColor.BLUE + "Enter Pincode"))
                .rows(6)
                .pageSize(45)
                .disableAllInteractions()
                .create();

        for (int i = 0; i < 10; i++) {
            ItemBuilder itemBuilder = ItemBuilder.from(new ItemStack(Material.LIME_STAINED_GLASS_PANE))
                    .name(Component.text(ChatColor.WHITE + String.valueOf(i)));
            gui.addItem(new GuiItem(itemBuilder.build(), inventoryClickEvent -> {
                Player clickedPlayer = (Player) inventoryClickEvent.getWhoClicked();
                StringBuilder pincodeBuilder = playerData.getPincodeBuilder();

                String clickedNumber = ChatColor.stripColor(inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName());

                if (pincodeBuilder.length() < 4) {
                    pincodeBuilder.append(clickedNumber);
                    clickedPlayer.sendMessage(ChatColor.GREEN + "Pincode: " + pincodeBuilder.toString());
                }

                if (pincodeBuilder.length() == 4) {
                    String enteredPincode = pincodeBuilder.toString();
                    if (PincodeManager.checkPincode(playerId, enteredPincode)) {
                        clickedPlayer.sendMessage(ChatColor.GREEN + "Pincode correct!");
                        openPhoneMenu(clickedPlayer);
                        inventoryClickEvent.getView().close();
                        playerData.resetPincodeBuilder();
                        openPhoneMenu(player);
                    } else {
                        clickedPlayer.sendMessage(ChatColor.RED + "Incorrect pincode!");
                        playerData.resetPincodeBuilder();
                    }
                }
            }));
        }
        gui.open(player);
    }

    private void openPhoneMenu(Player player) {
        PaginatedGui gui = Gui.paginated()
                .title(Component.text(ChatColor.GREEN + "Telefoon Menu"))
                .rows(6)
                .pageSize(45)
                .disableAllInteractions()
                .create();

        ItemBuilder gpsTracker = ItemBuilder.from(Material.PLAYER_HEAD)
                .name(Component.text(ChatColor.YELLOW + "Online Spelers"));
        gui.addItem(new GuiItem(gpsTracker.build(), inventoryClickEvent -> {
            openPlayerGPS(player);
        }));
        gui.open(player);
    }

    private static final Map<Player, Player> playerLines = new HashMap<>();

    public static void openPlayerGPS(Player player) {
        PaginatedGui gui = Gui.paginated()
                .title(Component.text(ChatColor.GREEN + "GPS Menu"))
                .rows(6)
                .pageSize(45)
                .disableAllInteractions()
                .create();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
            meta.setOwningPlayer(onlinePlayer);
            meta.setDisplayName((ChatColor.GREEN + onlinePlayer.getName()));
            playerHead.setItemMeta(meta);

            gui.addItem(new GuiItem(playerHead, inventoryClickEvent -> {
                Player clickedPlayer = (Player) inventoryClickEvent.getWhoClicked();
                Player targetPlayer = onlinePlayer;
                drawLine(clickedPlayer, targetPlayer);
            }));
            ItemBuilder cancelTracker = ItemBuilder.from(Material.REDSTONE)
                    .name(Component.text(ChatColor.RED + "Cancel Tracker"));
            gui.setItem(53, new GuiItem(cancelTracker.build(), inventoryClickEvent -> {
                //TODO CANCEL THE PARTIAL LINE DRAWING
            }));
        }

        gui.open(player);
    }

    private static void drawLine(Player sender, Player target) {
        playerLines.put(sender, target);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (sender.hasLineOfSight(target)) {
                    removeLine(sender);
                    cancel();
                } else {
                    LineManager.drawLine(sender, target);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void removeLine(Player player) {
        Player target = playerLines.remove(player);
        if (target != null) {
            if (player.hasLineOfSight(target)) {
                LineManager.playerLines.remove(player, 100);
            }
        }
    }

}
