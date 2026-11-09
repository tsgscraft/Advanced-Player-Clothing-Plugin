package de.tsgscraft.advancedClothingPlugin;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class AdvancedClothingPlugin extends JavaPlugin implements PluginMessageListener {

    public static String set_full_clothing = "advancedclothing:set_full_clothing";
    public static String set_full_clothing_sync = "advancedclothing:set_full_clothing_sync";
    public static String set_clothing = "advancedclothing:set_clothing";
    public static String set_clothing_sync = "advancedclothing:set_clothing_sync";
    public static String clothing_sync = "advancedclothing:clothing_sync";

    public static String test_for_server = "advancedclothing:test_for_server";
    public static String test_for_server_return = "advancedclothing:test_for_server_return";

    private static AdvancedClothingPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        getServer().getMessenger().registerIncomingPluginChannel(
                this,
                set_clothing,
                this
        );

        getServer().getMessenger().registerIncomingPluginChannel(
                this,
                set_full_clothing,
                this
        );

        getServer().getMessenger().registerIncomingPluginChannel(
                this,
                test_for_server,
                this
        );



        getServer().getMessenger().registerOutgoingPluginChannel(
                this,
                set_clothing_sync
        );

        getServer().getMessenger().registerOutgoingPluginChannel(
                this,
                set_full_clothing_sync
        );

        getServer().getMessenger().registerOutgoingPluginChannel(
                this,
                clothing_sync
        );

        getServer().getMessenger().registerOutgoingPluginChannel(
                this,
                test_for_server_return
        );
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    public static AdvancedClothingPlugin getInstance() {
        return instance;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        switch (channel) {
            case "advancedclothing:test_for_server" -> {
                if (message.length == 0) return;
                if (message[0] == 0) {
                    // Modded Client detected!
                    getLogger().info("AdvancedClothingPlugin detected on client: " + player.getName());
                    player.sendPluginMessage(
                            this,
                            "advancedclothing:test_for_server_return",
                            new byte[]{1}
                    );
                    AdvancedClothingPlugin.getInstance().getLogger().info("Sending clothing sync to player: " + player.getName());
                    AdvancedClothingPlugin.getInstance().sendClothingSync(player);
                }
            }
            case "advancedclothing:set_clothing" -> {
                // Handle set clothing message
                SetClothingData data = deserializeSetClothingData(message);
                getLogger().info("Received set clothing message from client: " + player.getName() +
                        ", UUID: " + data.uuid +
                        ", Clothing Type: " + data.clothingType +
                        ", Clothing ID: " + data.clothingId);

                ClothingRegistry.setClothing(data.uuid, data.clothingType, data.clothingId);

                for (Player p : getServer().getOnlinePlayers()) {
                    if (!p.equals(player)) {
                        p.sendPluginMessage(
                                this,
                                "advancedclothing:set_clothing_sync",
                                serializeSetClothingData(data.uuid, data.clothingType, data.clothingId)
                        );
                    }
                }
            }
            case "advancedclothing:set_full_clothing" -> {
                // Handle set full clothing message
                SetFullClothingData data = deserializeSetFullClothingData(message);
                getLogger().info("Received set full clothing message from client: " + player.getName() +
                        ", UUID: " + data.uuid +
                        ", Clothing Data: " + data.clothingData);

                ClothingRegistry.setActiveClothingData(data.uuid, data.clothingData);

                for (Player p : getServer().getOnlinePlayers()) {
                    if (!p.equals(player)) {
                        p.sendPluginMessage(
                                this,
                                "advancedclothing:set_full_clothing_sync",
                                serializeSetFullClothingData(data.uuid, data.clothingData)
                        );
                    }
                }
            }
        }
    }

    public void sendClothingSync(Player player) {
        Map<String, Map<String, String>> clothingData = new HashMap<>();
        for (Player p : getServer().getOnlinePlayers()) {
            if (p.equals(player)) continue;
            clothingData.put(p.getUniqueId().toString(), ClothingRegistry.getActiveClothingElements(p.getUniqueId().toString()));
        }
        player.sendPluginMessage(
                this,
                "advancedclothing:clothing_sync",
                serializeClothingSyncData(clothingData)
        );
    }

    private byte[] serializeClothingSyncData(Map<String, Map<String, String>> clothingData) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);

            writeVarInt(out, clothingData.size());
            for (Map.Entry<String, Map<String, String>> entry : clothingData.entrySet()) {
                writeString(out, entry.getKey());
                Map<String, String> innerMap = entry.getValue();
                writeVarInt(out, innerMap.size());
                for (Map.Entry<String, String> innerEntry : innerMap.entrySet()) {
                    writeString(out, innerEntry.getKey());
                    writeString(out, innerEntry.getValue());
                }
            }

            return bytes.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] serializeSetClothingData(String uuid, String clothingType, String clothingId) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);

            writeString(out, uuid);
            writeString(out, clothingType);
            writeString(out, clothingId);

            return bytes.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] serializeSetFullClothingData(String uuid, Map<String, String> clothingData) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);


            writeString(out, uuid);
            writeVarInt(out, clothingData.size());
            for (Map.Entry<String, String> entry : clothingData.entrySet()) {
                writeString(out, entry.getKey());
                writeString(out, entry.getValue());
            }

            return bytes.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SetClothingData deserializeSetClothingData(byte[] message) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

            String uuid = readString(in);
            String clothingType = readString(in);
            String clothingId = readString(in);

            return new SetClothingData(uuid, clothingType, clothingId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SetFullClothingData deserializeSetFullClothingData(byte[] message) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

            String uuid = readString(in);
            int size = readVarInt(in);

            Map<String, String> clothingData = new java.util.HashMap<>();
            for (int i = 0; i < size; i++) {
                String key = readString(in);
                String value = readString(in);
                clothingData.put(key, value);
            }

            return new SetFullClothingData(uuid, clothingData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class SetClothingData {
        public String uuid;
        public String clothingType;
        public String clothingId;

        public SetClothingData(String uuid, String clothingType, String clothingId) {
            this.uuid = uuid;
            this.clothingType = clothingType;
            this.clothingId = clothingId;
        }
    }

    public static class SetFullClothingData {
        public String uuid;
        public Map<String, String> clothingData;

        public SetFullClothingData(String uuid, Map<String, String> clothingData) {
            this.uuid = uuid;
            this.clothingData = clothingData;
        }
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = readVarInt(in);

        if (length < 0 || length > 32767) {
            throw new IOException("Invalid string length: " + length);
        }

        byte[] bytes = new byte[length];
        in.readFully(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int result = 0;
        int shift = 0;

        byte current;

        do {
            current = in.readByte();
            result |= (current & 0x7F) << shift;
            shift += 7;

            if (shift >= 35) {
                throw new IOException("VarInt too big");
            }
        } while ((current & 0x80) != 0);

        return result;
    }

    public static void writeString(DataOutputStream out, String value)
            throws IOException {

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private static void writeVarInt(DataOutputStream out, int value)
            throws IOException {

        while ((value & ~0x7F) != 0) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }

        out.writeByte(value);
    }
}
