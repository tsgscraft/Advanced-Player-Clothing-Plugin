package de.tsgscraft.advancedClothingPlugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Handle player join event
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Handle player quit event
        ClothingRegistry.remove(event.getPlayer().getUniqueId().toString());
        ClothingRegistry.removeModdedPlayer(event.getPlayer().getUniqueId().toString());
    }
}
