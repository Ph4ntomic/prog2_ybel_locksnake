package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GameState {
    private final Level level;
    private final Snake snake;
    private final List<Pin> pins;
    private final Status status;
    private final Direction pendingDirection;

    public GameState(
            Level level, Snake snake, List<Pin> pins, Status status, Direction pendingDirection) {
        this.level = Objects.requireNonNull(level);
        this.snake = Objects.requireNonNull(snake);
        this.pins = List.copyOf(Objects.requireNonNull(pins));
        this.status = Objects.requireNonNull(status);
        this.pendingDirection = pendingDirection == null ? Direction.NONE : pendingDirection;
    }

    public Level level() {
        return level;
    }

    public Snake snake() {
        return snake;
    }

    public List<Pin> pins() {
        return pins;
    }

    public Status status() {
        return status;
    }

    public Direction pendingDirection() {
        return pendingDirection;
    }

    public GameState withPendingDirection(Direction newPendingDirection) {
        var direction = newPendingDirection == null ? Direction.NONE : newPendingDirection;
        if (pendingDirection == direction) return this;
        return new GameState(level, snake, pins, status, direction);
    }

    public GameState tick() {
        if (!status.isRunning()) return this;
        if (allPinsSet(pins)) return withStatus(Status.WON);
        if (pendingDirection == Direction.NONE) return this;

        var nextHead = snake.nextHead(pendingDirection);

        if (!level.isInside(nextHead)) {
            return withStatus(Status.LOST_OUT_OF_BOUNDS);
        }

        if (level.cellAt(nextHead) == CellType.WALL) {
            return withPendingDirection(Direction.NONE);
        }

        if (snake.occupies(nextHead)) {
            return withStatus(Status.LOST_SELF_COLLISION);
        }

        var pin = pinAt(nextHead);
        if (pin.isPresent()) {
            return bumpPin(pin.get());
        }

        return new GameState(level, snake.grow(pendingDirection), pins, status, pendingDirection);
    }

    private GameState bumpPin(Pin pin) {
        if (pin.state().isSet() || pin.activationDirection() != pendingDirection) {
            return withPendingDirection(Direction.NONE);
        }

        var updatedPins =
                pins.stream()
                        .map(
                                current ->
                                        current.position().equals(pin.position())
                                                ? current.withState(Pin.State.HIGH)
                                                : current)
                        .toList();
        var updatedStatus = allPinsSet(updatedPins) ? Status.WON : Status.RUNNING;

        return new GameState(level, snake, updatedPins, updatedStatus, Direction.NONE);
    }

    private Optional<Pin> pinAt(Position position) {
        return pins.stream().filter(pin -> pin.position().equals(position)).findFirst();
    }

    private GameState withStatus(Status newStatus) {
        if (status == newStatus) return this;
        return new GameState(level, snake, pins, newStatus, pendingDirection);
    }

    private static boolean allPinsSet(List<Pin> pins) {
        return pins.stream().map(Pin::state).allMatch(Pin.State::isSet);
    }

    public enum Status {
        RUNNING,
        WON,
        LOST_SELF_COLLISION,
        LOST_OUT_OF_BOUNDS;

        public boolean isRunning() {
            return this == RUNNING;
        }
    }
}
