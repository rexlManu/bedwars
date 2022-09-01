/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.misc;

import lombok.Getter;
import org.bukkit.Color;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 06:58                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

@Getter
public enum TeamColor {

    RED("Rot", '4', 14, Color.RED),
    BLUE("Blau", '1', 11, Color.BLUE),
    GREEN("Grün", '2', 13, Color.GREEN),
    YELLOW("Gelb", 'e', 4, Color.YELLOW),
    ORANGE("Orange", '6', 1, Color.ORANGE),
    PURPLE("Purple", '5', 10, Color.PURPLE),
    PINK("Pink", 'd', 6, Color.FUCHSIA),
    AQUA("Aqua", 'b', 3, Color.AQUA),
    CYAN("Cyan", '3', 9, Color.TEAL),
    LIME("Lime", 'a', 5, Color.LIME),
    WHITE("Weiß", 'f', 0, Color.WHITE),
    BLACK("Schwarz", '0', 15, Color.BLACK);

    private final String displayName;
    private final char key;
    private final int colorByte;
    private Color color;

    TeamColor(String displayName, char key, int colorByte, Color color) {
        this.displayName = displayName;
        this.key = key;
        this.colorByte = colorByte;
        this.color = color;
    }

}
