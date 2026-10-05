package com.optifinecapes;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public final class CapeService {
    private static final Pattern VALID_NAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");
    private static final Map<String, Optional<Identifier>> CAPES = new ConcurrentHashMap<>();
    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private CapeService() {
    }

    public static void request(GameProfile profile) {
        if (profile == null) {
            return;
        }
        String name = profile.name();
        if (name == null || !VALID_NAME.matcher(name).matches()) {
            return;
        }
        if (CAPES.putIfAbsent(name, Optional.empty()) != null) {
            return;
        }
        String playerName = name;
        EXECUTOR.submit(() -> download(playerName));
    }

    public static Optional<Identifier> getCapeTexture(String name) {
        Optional<Identifier> entry = CAPES.get(name);
        return entry == null ? Optional.empty() : entry;
    }

    private static void download(String name) {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI
                    .create("http://s.optifine.net/capes/" + name + ".png")
                    .toURL().openConnection(Minecraft.getInstance().getProxy());
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            if (connection.getResponseCode() / 100 != 2) {
                return;
            }
            NativeImage image;
            try (InputStream in = connection.getInputStream()) {
                image = NativeImage.read(in);
            }
            NativeImage capeImage = parseCape(image);
            Identifier id = Identifier.fromNamespaceAndPath(OptifineCapesClient.MOD_ID,
                    "capes/" + name.toLowerCase(Locale.ROOT));
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().getTextureManager().register(id,
                        new DynamicTexture(() -> "optifine_cape_" + name, capeImage));
                CAPES.put(name, Optional.of(id));
            });
        } catch (Exception e) {
            OptifineCapesClient.LOGGER.debug("No OptiFine cape for {}", name);
        }
    }

    private static NativeImage parseCape(NativeImage image) {
        int width = 64;
        int height = 32;
        while (width < image.getWidth() || height < image.getHeight()) {
            width *= 2;
            height *= 2;
        }
        NativeImage out = new NativeImage(width, height, true);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                out.setPixel(x, y, image.getPixel(x, y));
            }
        }
        image.close();
        return out;
    }
}
