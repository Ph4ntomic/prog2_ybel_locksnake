package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.Direction;
import de.hsbi.lockgame.model.Level;
import de.hsbi.lockgame.model.Snake;
import de.hsbi.lockgame.ui.GamePanel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GameEngine implements GameObserver<Direction> {
    private final List<GameObserver<GameState>> stateObservers = new ArrayList<>();
    private GameState state;

    public GameEngine(Level level) {
        level = Objects.requireNonNull(level);
        this.state =
                new GameState(
                        level,
                        new Snake(List.of(level.snakeStart())),
                        level.pins(),
                        GameState.Status.RUNNING,
                        Direction.NONE);
    }

    public GameState state() {
        return state;
    }

    public void setGamePanel(GamePanel panel) {
        addObserver(panel);
    }

    public void addObserver(GameObserver<GameState> observer) {
        stateObservers.add(Objects.requireNonNull(observer));
    }

    public void removeObserver(GameObserver<GameState> observer) {
        stateObservers.remove(observer);
    }

    @Override
    public void update(Direction d) {
        if (d == null || !state.status().isRunning()) return;

        var nextState = state.withPendingDirection(d);
        updateState(nextState);
    }

    public void tick() {
        updateState(state.tick());
    }

    private void updateState(GameState nextState) {
        if (nextState == state) return;
        state = nextState;
        notifyObservers();
    }

    private void notifyObservers() {
        stateObservers.forEach(observer -> observer.update(state));
    }
}
