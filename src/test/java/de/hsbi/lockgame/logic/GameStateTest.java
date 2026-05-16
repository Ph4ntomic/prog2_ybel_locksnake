package de.hsbi.lockgame.logic;

import static org.junit.jupiter.api.Assertions.*;

import de.hsbi.lockgame.model.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameStateTest {

    @Test
    void givenInitialData_whenStateIsCreated_thenValuesAreReadable() {
        var level = level("#####", "#S..#", "#..v#", "#####");
        var state = runningState(level, Direction.NONE);

        assertAll(
                () -> assertSame(level, state.level()),
                () -> assertEquals(new Position(1, 1), state.snake().head()),
                () -> assertEquals(1, state.snake().body().size()),
                () -> assertEquals(1, state.pins().size()),
                () -> assertEquals(GameState.Status.RUNNING, state.status()),
                () -> assertEquals(Direction.NONE, state.pendingDirection()));
    }

    @Test
    void givenNoPendingDirection_whenTickRuns_thenStateStaysUnchanged() {
        var level = level("#####", "#S..#", "#..v#", "#####");
        var state = runningState(level, Direction.NONE);

        var next = state.tick();

        assertSame(state, next);
    }

    @Test
    void givenFreeCellAhead_whenTickRuns_thenSnakeMovesAndGrows() {
        var level = level("#####", "#S..#", "#..v#", "#####");
        var state = runningState(level, Direction.RIGHT);

        var next = state.tick();

        assertAll(
                () -> assertEquals(new Position(2, 1), next.snake().head()),
                () ->
                        assertEquals(
                                List.of(new Position(2, 1), new Position(1, 1)),
                                next.snake().body()),
                () -> assertEquals(GameState.Status.RUNNING, next.status()),
                () -> assertEquals(Direction.RIGHT, next.pendingDirection()));
    }

    @Test
    void givenWallAhead_whenTickRuns_thenSnakeIsBlockedAndDirectionIsCleared() {
        var level = level("#####", "#S#v#", "#####");
        var state = runningState(level, Direction.RIGHT);

        var next = state.tick();

        assertAll(
                () -> assertEquals(new Position(1, 1), next.snake().head()),
                () -> assertEquals(1, next.snake().body().size()),
                () -> assertEquals(GameState.Status.RUNNING, next.status()),
                () -> assertEquals(Direction.NONE, next.pendingDirection()),
                () -> assertEquals(Pin.State.LOW, next.pins().getFirst().state()));
    }

    @Test
    void givenMoveLeavesLevel_whenTickRuns_thenGameIsLostOutOfBounds() {
        var level = level("S.v");
        var state = runningState(level, Direction.LEFT);

        var next = state.tick();

        assertAll(
                () -> assertEquals(GameState.Status.LOST_OUT_OF_BOUNDS, next.status()),
                () -> assertEquals(new Position(0, 0), next.snake().head()));
    }

    @Test
    void givenSnakeBodyAhead_whenTickRuns_thenGameIsLostBySelfCollision() {
        var level = level("#####", "#v..#", "#.S.#", "#####");
        var snake = new Snake(List.of(new Position(2, 2), new Position(1, 2), new Position(1, 1)));
        var state =
                new GameState(level, snake, level.pins(), GameState.Status.RUNNING, Direction.LEFT);

        var next = state.tick();

        assertAll(
                () -> assertEquals(GameState.Status.LOST_SELF_COLLISION, next.status()),
                () -> assertEquals(new Position(2, 2), next.snake().head()));
    }

    @Test
    void givenPinFromWrongDirection_whenTickRuns_thenPinBlocksMovement() {
        var level = level("#####", "#S>.#", "#..v#", "#####");
        var state = runningState(level, Direction.RIGHT);

        var next = state.tick();

        assertAll(
                () -> assertEquals(new Position(1, 1), next.snake().head()),
                () -> assertEquals(Pin.State.LOW, pinAt(next, new Position(2, 1)).state()),
                () -> assertEquals(GameState.Status.RUNNING, next.status()),
                () -> assertEquals(Direction.NONE, next.pendingDirection()));
    }

    @Test
    void givenLowPinFromCorrectDirection_whenTickRuns_thenPinIsSetAndSnakeStaysInPlace() {
        var level = level("######", "#S<.v#", "######");
        var state = runningState(level, Direction.RIGHT);

        var next = state.tick();

        assertAll(
                () -> assertEquals(new Position(1, 1), next.snake().head()),
                () -> assertEquals(1, next.snake().body().size()),
                () -> assertEquals(Pin.State.HIGH, pinAt(next, new Position(2, 1)).state()),
                () -> assertEquals(Pin.State.LOW, pinAt(next, new Position(4, 1)).state()),
                () -> assertEquals(GameState.Status.RUNNING, next.status()),
                () -> assertEquals(Direction.NONE, next.pendingDirection()));
    }

    @Test
    void givenHighPinAhead_whenTickRuns_thenPinBlocksMovement() {
        var level = level("######", "#S<.v#", "######");
        var pins =
                level.pins().stream()
                        .map(
                                pin ->
                                        pin.position().equals(new Position(2, 1))
                                                ? pin.withState(Pin.State.HIGH)
                                                : pin)
                        .toList();
        var state =
                new GameState(
                        level,
                        new Snake(List.of(level.snakeStart())),
                        pins,
                        GameState.Status.RUNNING,
                        Direction.RIGHT);

        var next = state.tick();

        assertAll(
                () -> assertEquals(new Position(1, 1), next.snake().head()),
                () -> assertEquals(Pin.State.HIGH, pinAt(next, new Position(2, 1)).state()),
                () -> assertEquals(Pin.State.LOW, pinAt(next, new Position(4, 1)).state()),
                () -> assertEquals(GameState.Status.RUNNING, next.status()),
                () -> assertEquals(Direction.NONE, next.pendingDirection()));
    }

    @Test
    void givenLastLowPinIsSet_whenTickRuns_thenGameIsWon() {
        var level = level("#####", "#S<.#", "#####");
        var state = runningState(level, Direction.RIGHT);

        var next = state.tick();

        assertAll(
                () -> assertEquals(Pin.State.HIGH, next.pins().getFirst().state()),
                () -> assertEquals(GameState.Status.WON, next.status()),
                () -> assertEquals(new Position(1, 1), next.snake().head()));
    }

    @Test
    void givenAllPinsAlreadySet_whenTickRuns_thenGameIsWon() {
        var level = level("#####", "#S<.#", "#####");
        var pins = level.pins().stream().map(pin -> pin.withState(Pin.State.HIGH)).toList();
        var state =
                new GameState(
                        level,
                        new Snake(List.of(level.snakeStart())),
                        pins,
                        GameState.Status.RUNNING,
                        Direction.NONE);

        var next = state.tick();

        assertEquals(GameState.Status.WON, next.status());
    }

    @Test
    void givenGameAlreadyEnded_whenTickRuns_thenStateStaysUnchanged() {
        var level = level("#####", "#S<.#", "#####");
        var state =
                new GameState(
                        level,
                        new Snake(List.of(level.snakeStart())),
                        level.pins(),
                        GameState.Status.LOST_OUT_OF_BOUNDS,
                        Direction.RIGHT);

        var next = state.tick();

        assertSame(state, next);
    }

    private static GameState runningState(Level level, Direction direction) {
        return new GameState(
                level,
                new Snake(List.of(level.snakeStart())),
                level.pins(),
                GameState.Status.RUNNING,
                direction);
    }

    private static Pin pinAt(GameState state, Position position) {
        return state.pins().stream()
                .filter(pin -> pin.position().equals(position))
                .findFirst()
                .orElseThrow();
    }

    private static Level level(String... rows) {
        var height = rows.length;
        var width = rows[0].length();
        var cells = new CellType[width][height];
        var pins = new ArrayList<Pin>();
        Position start = null;

        for (var y = 0; y < height; y++) {
            if (rows[y].length() != width)
                throw new IllegalArgumentException("rows must have same width");

            for (var x = 0; x < width; x++) {
                var position = new Position(x, y);

                switch (rows[y].charAt(x)) {
                    case '#' -> cells[x][y] = CellType.WALL;
                    case '^' -> {
                        cells[x][y] = CellType.PIN_SLOT;
                        pins.add(new Pin(position, Pin.State.LOW, Direction.DOWN));
                    }
                    case 'v' -> {
                        cells[x][y] = CellType.PIN_SLOT;
                        pins.add(new Pin(position, Pin.State.LOW, Direction.UP));
                    }
                    case '<' -> {
                        cells[x][y] = CellType.PIN_SLOT;
                        pins.add(new Pin(position, Pin.State.LOW, Direction.RIGHT));
                    }
                    case '>' -> {
                        cells[x][y] = CellType.PIN_SLOT;
                        pins.add(new Pin(position, Pin.State.LOW, Direction.LEFT));
                    }
                    case 'S' -> {
                        cells[x][y] = CellType.EMPTY;
                        start = position;
                    }
                    default -> cells[x][y] = CellType.EMPTY;
                }
            }
        }

        if (start == null) throw new IllegalArgumentException("level needs a start position");

        return new Level(width, height, cells, List.copyOf(pins), start);
    }
}
