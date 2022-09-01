/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.managers;

import de.rexlmanu.bedwars.gamestates.GameState;

import java.util.ArrayList;
import java.util.List;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 03:46                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

public final class GameManager {

    private List<GameState> gameStates;
    private GameState currentState;

    public GameManager() {
        this.gameStates = new ArrayList<>();
    }

    public void setCurrentState(int gameStateIndex) {
        if (currentState != null)
            currentState.onStop();

        final GameState newGameState = gameStates.get(gameStateIndex);
        if (newGameState == null) {
            throw new IllegalStateException("GameState is null");
        }

        currentState = newGameState;
        currentState.onStart();
    }

    public List<GameState> getGameStates() {
        return gameStates;
    }

    public void registerState(GameState gameState) {
        this.gameStates.add(gameState);
    }
}
