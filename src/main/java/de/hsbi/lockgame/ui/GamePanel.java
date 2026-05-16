package de.hsbi.lockgame.ui;

import de.hsbi.lockgame.logic.GameEngine;
import de.hsbi.lockgame.logic.GameObserver;
import de.hsbi.lockgame.logic.GameState;
import de.hsbi.lockgame.model.Direction;
import de.hsbi.lockgame.settings.GameConstants;
import de.hsbi.lockgame.settings.InputConstants;
import de.hsbi.lockgame.ui.render.GameRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.*;

public class GamePanel extends JPanel implements GameObserver<GameState> {
    private GameState state;
    private final GameRenderer renderer;
    private final List<GameObserver<Direction>> directionObservers = new ArrayList<>();

    public GamePanel(GameState initialState, GameRenderer renderer) {
        this.state = initialState;
        this.renderer = renderer;

        var width = initialState.level().width() * GameConstants.TILE_SIZE;
        var height = initialState.level().height() * GameConstants.TILE_SIZE;

        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);

        setFocusable(true);
        InputConstants.BINDINGS.forEach(this::setupKeyBindings);
    }

    @Override
    public void update(GameState newState) {
        this.state = newState;
        repaint();
    }

    public void setGameEngine(GameEngine engine) {
        addObserver(engine);
    }

    public void addObserver(GameObserver<Direction> observer) {
        directionObservers.add(Objects.requireNonNull(observer));
    }

    public void removeObserver(GameObserver<Direction> observer) {
        directionObservers.remove(observer);
    }

    private void setupKeyBindings(Direction direction, Iterable<Integer> keyCodes) {
        var inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        var actionMap = getActionMap();
        var actionKey = "move_" + direction.name();

        var swingAction =
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        notifyDirectionObservers(direction);
                    }
                };

        keyCodes.forEach(keyCode -> inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), actionKey));
        actionMap.put(actionKey, swingAction);
    }

    private void notifyDirectionObservers(Direction direction) {
        directionObservers.forEach(observer -> observer.update(direction));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.render((Graphics2D) g, state, GameConstants.TILE_SIZE);
    }
}
