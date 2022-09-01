/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;
import java.util.UUID;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 01.09.2019 / 02:26                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class Stats {

    private static final DecimalFormat WINRATE_FORMAT = new DecimalFormat("#0.00");

    private UUID uuid;
    private int kills, deaths, beds, games, wins, rank;
    private boolean exists;

    public void addKill() {
        kills++;
    }

    public void addBed() {
        beds++;
    }

    public void addDeath() {
        deaths++;
    }

    public void addGame() {
        games++;
    }

    public void addWin() {
        wins++;
    }

    public double getKD() {
        double kd = 0.0;
        if (deaths == 0) kd = kills;
        if (kills != 0 && deaths != 0) kd = Math.round(((double) kills / deaths * 100D)) / 100;
        return kd;
    }

    public String winrate() {
        if (wins == 0) return "0";
        if ((games - wins) == 0) return "100";
        return WINRATE_FORMAT.format((wins / games) * 100);
    }

    public static Stats emptyStats(UUID uniqueId) {
        return new Stats(uniqueId, 0, 0, 0, 0, 0, 0, false);
    }
}
