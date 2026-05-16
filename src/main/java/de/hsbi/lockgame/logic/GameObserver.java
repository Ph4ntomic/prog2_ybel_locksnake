package de.hsbi.lockgame.logic;

@FunctionalInterface
public interface GameObserver<T> {
    void update(T value);
}
