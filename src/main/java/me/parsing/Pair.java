package me.parsing;

public class Pair<T, U> {

    public final T first;
    public final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public static <T, U> Pair<T, U> of(T first, U second) {
        return new Pair<>(first, second);
    }

}
