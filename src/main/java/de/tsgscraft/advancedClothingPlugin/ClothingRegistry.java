package de.tsgscraft.advancedClothingPlugin;

import javax.net.ssl.SSLContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClothingRegistry {

    public static List<String> moddedPlayers = List.of();

    public static Map<String, Map<String, String>> activeClothingElementsByPlayer = new HashMap<>();

    public static void addModdedPlayer(String playerUUID) {
        if (!moddedPlayers.contains(playerUUID)) {
            moddedPlayers.add(playerUUID);
        }
    }

    public static void removeModdedPlayer(String playerUUID) {
        moddedPlayers.remove(playerUUID);
    }

    public static Map<String, String> getActiveClothingElements(String playerUUID) {
        return activeClothingElementsByPlayer.computeIfAbsent(playerUUID, k -> new HashMap<>());
    }

    public static void setActiveClothingData(String playerUUID, Map<String, String> clothingData) {
        activeClothingElementsByPlayer.put(playerUUID, clothingData);
    }

    public static void setClothing(String playerUUID, String clothingType, String clothingId) {
        if (clothingId == null || clothingId.isEmpty()) {
            getActiveClothingElements(playerUUID).remove(clothingType);
        } else {
            getActiveClothingElements(playerUUID).put(clothingType, clothingId);
        }
    }

    public static void remove(String string) {
        activeClothingElementsByPlayer.remove(string);
    }
}
