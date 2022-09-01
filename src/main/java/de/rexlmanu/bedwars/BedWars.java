package de.rexlmanu.bedwars;

import de.rexlmanu.bedwars.gamestates.GameState;
import de.rexlmanu.bedwars.gamestates.states.EndingState;
import de.rexlmanu.bedwars.gamestates.states.IngameState;
import de.rexlmanu.bedwars.gamestates.states.LobbyState;

public final class BedWars extends ManagerPlugin {

    @Override
    public void onEnable() {
        init();
        getGameManager().registerState(new LobbyState(this));
        getGameManager().registerState(new IngameState(this));
        getGameManager().registerState(new EndingState(this));
        getGameManager().setCurrentState(GameState.LOBBY_STATE);
    }

    @Override
    public void onDisable() {
    }
}
