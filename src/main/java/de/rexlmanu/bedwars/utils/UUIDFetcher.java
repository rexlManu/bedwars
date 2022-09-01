/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.utils;

import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 18.06.2019 / 06:45                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

public final class UUIDFetcher {

    private static final String API_URL = "https://api.minetools.eu/uuid/";
    private static final JsonParser JSON_PARSER = new JsonParser();

    public static CompletableFuture<UUID> getUUID(String playerName) {
        return CompletableFuture.supplyAsync(() ->
                insertDashUUID(JSON_PARSER.parse(
                        new InputStreamReader(Objects.requireNonNull(createUrlAndGetStream(API_URL + playerName)))
                ).getAsJsonObject().get("id").getAsString()));
    }

    public static CompletableFuture<String> getName(UUID uuid) {
        return CompletableFuture.supplyAsync(() ->
                JSON_PARSER.parse(new InputStreamReader(Objects.requireNonNull(createUrlAndGetStream(API_URL + uuid.toString().replace("-", ""))))
                ).getAsJsonObject().get("name").getAsString());
    }

    private static InputStream createUrlAndGetStream(String rawUrl) {
        try {
            return new URL(rawUrl).openStream();
        } catch (IOException ignored) {
            return null;
        }
    }

    private static UUID insertDashUUID(String uuid) {
        StringBuffer buffer = new StringBuffer(uuid);
        buffer.insert(8, "-");
        buffer = new StringBuffer(buffer.toString());
        buffer.insert(13, "-");
        buffer = new StringBuffer(buffer.toString());
        buffer.insert(18, "-");
        buffer = new StringBuffer(buffer.toString());
        buffer.insert(23, "-");
        return UUID.fromString(buffer.toString());
    }
}
