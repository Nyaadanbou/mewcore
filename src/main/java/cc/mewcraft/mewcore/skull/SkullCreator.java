package cc.mewcraft.mewcore.skull;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * A library for the Bukkit API to create player skulls from names, base64 strings, and texture URLs.
 * <p>
 * Modified by Nailm.
 *
 * @author Dean B on 12/28/2016.
 */
public class SkullCreator {

    private SkullCreator() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Modifies a skull to use base64 string retrieved from Mojang API for the given player's UUID.
     * <p>
     * This is a blocking operation.
     *
     * @param item The item to apply the name to. Must be a player skull
     * @param id   The Player's UUID
     * @return The head of the player, or as-is if something wrong
     */
    public static ItemStack itemWithUuid(ItemStack item, UUID id) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(id, "id");

        String base64 = base64(id);
        if (base64 != null) {
            return itemWithBase64(item, base64);
        } else {
            return item;
        }
    }

    /**
     * Modifies a skull to use the skin at the given Mojang URL.
     *
     * @param item The item to apply the skin to. Must be a player skull
     * @param url  The URL of the Mojang skin
     * @return The head associated with the URL
     */
    public static ItemStack itemWithUrl(ItemStack item, String url) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(url, "url");

        return itemWithBase64(item, urlToBase64(url));
    }

    /**
     * Modifies a skull to use the skin based on the given base64 string.
     *
     * @param item   The ItemStack to put the base64 onto. Must be a player skull
     * @param base64 The base64 string containing the texture
     * @return The head with a custom texture
     */
    public static ItemStack itemWithBase64(ItemStack item, String base64) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(base64, "base64");

        if (!(item.getItemMeta() instanceof SkullMeta meta)) {
            return null;
        }
        mutateItemMeta(meta, base64);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Sets the block to a skull with the given UUID.
     *
     * @param block The block to set.
     * @param id    The player to set it to.
     */
    public static void blockWithUuid(Block block, UUID id) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(id, "id");

        setToSkull(block);
        String base64 = base64(id);
        if (base64 != null) {
            blockWithBase64(block, base64);
        }
    }

    /**
     * Sets the block to a skull with the skin found at the provided mojang URL.
     *
     * @param block The block to set.
     * @param url   The mojang URL to set it to use.
     */
    public static void blockWithUrl(Block block, String url) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(url, "url");

        blockWithBase64(block, urlToBase64(url));
    }

    /**
     * Sets the block to a skull with the skin for the base64 string.
     *
     * @param block  The block to set.
     * @param base64 The base64 to set it to use.
     */
    public static void blockWithBase64(Block block, String base64) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(base64, "base64");

        setToSkull(block);
        Skull state = (Skull) block.getState();
        mutateBlockState(state, base64);
        state.update(false, false);
    }

    private static void setToSkull(Block block) {
        block.setType(Material.PLAYER_HEAD, false);
    }

    @SuppressWarnings("BusyWait")
    private static String base64(UUID id) {
        short retryCount = 10;
        while (true) {
            try {
                URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + id.toString() + "?unsigned=false");
                InputStreamReader in = new InputStreamReader(url.openStream());
                JsonObject textureProperty = JsonParser.parseReader(in)
                        .getAsJsonObject().get("properties")
                        .getAsJsonArray().get(0)
                        .getAsJsonObject();
                return textureProperty.get("value").getAsString();
            } catch (IOException e) {
                // cannot connect to mojang, retry
                if (--retryCount < 0) {
                    Bukkit.getLogger().warning("SKULLCREATOR API - Retry over 10 times, aborted");
                    return null;
                }
                try {
                    Bukkit.getLogger().info("SKULLCREATOR API - Cannot connect to Mojang, retrying");
                    Thread.sleep(TimeUnit.SECONDS.toMillis(60));
                } catch (InterruptedException ignored) {
                }
            } catch (IllegalStateException e) {
                // connection is good, but the JsonObject retrieved is not expected,
                // which means that the player is cracked
                // return as-is
                Bukkit.getLogger().warning("SKULLCREATOR API - UUID might be a cracked player, aborted");
                return null;
            }
        }
    }

    private static String urlToBase64(String url) {
        URI actualUrl;
        try {
            actualUrl = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String toEncode = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl + "\"}}}";
        return Base64.getEncoder().encodeToString(toEncode.getBytes());
    }

    private static PlayerProfile makeProfile(String b64) {
        // random uuid based on the b64 string
        UUID id = new UUID(
                b64.substring(b64.length() - 20).hashCode(),
                b64.substring(b64.length() - 10).hashCode()
        );
        PlayerProfile profile = Bukkit.createProfile(id);
        profile.getProperties().add(new ProfileProperty("textures", b64));
        return profile;
    }

    private static void mutateBlockState(Skull block, String b64) {
        block.setPlayerProfile(makeProfile(b64));
    }

    private static void mutateItemMeta(SkullMeta meta, String b64) {
        meta.setPlayerProfile(makeProfile(b64));
    }

}
