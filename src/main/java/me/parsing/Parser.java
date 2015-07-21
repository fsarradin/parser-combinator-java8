package me.parsing;

import java.util.function.Function;

public interface Parser<T> extends Function<CharReader, ParseResult<T>> {

    @Override
    ParseResult<T> apply(CharReader input);

    default String getName() {
        return "";
    }

    default Parser<T> withName(String name) {
        return new Parser<T>() {
            @Override
            public ParseResult<T> apply(CharReader input) {
                return Parser.this.apply(input);
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    default <U> Parser<U> flatmap(Function<T, Parser<U>> f) {
        return new Parser<U>() {
            @Override
            public ParseResult<U> apply(CharReader input) {
                return Parser.this.apply(input).flatmapWithNext(f);
            }

            @Override
            public String getName() {
                return Parser.this.getName();
            }
        };
    }

    default <U> Parser<U> map(Function<T, U> f) {
        return new Parser<U>() {
            @Override
            public ParseResult<U> apply(CharReader input) {
                return Parser.this.apply(input).map(f);
            }

            @Override
            public String getName() {
                return Parser.this.getName();
            }
        };
    }

    default <U> Parser<Pair<T, U>> then(Parser<U> p) {
        return flatmap(a -> p.map(b -> Pair.of(a, b))).withName(getName() + " ~ " + p.getName());
    }

    default <U> Parser<U> skipThen(Parser<U> p) {
        return flatmap(__ -> p.map(b -> b)).withName("[" + getName() + "] ~ " + p.getName());
    }

    default <U> Parser<T> skip(Parser<U> p) {
        return flatmap(a -> p.map(__ -> a)).withName(getName() + " ~ [" + p.getName() + "]");
    }

    default Parser<T> or(Parser<T> p) {
        Parser<T> parser = input -> this.apply(input).or(p.apply(input));
        return parser.withName(getName() + " | " + p.getName());
    }

}
