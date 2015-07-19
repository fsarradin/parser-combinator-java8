package me.parsing;

import java.util.function.Function;

public abstract class ParseResult<T> {

    public enum Type {
        SUCCESS, FAILURE, ERROR
    }

    private ParseResult() {}

    public abstract Type getType();

    public abstract <U> ParseResult<U> map(Function<T, U> f);

    public abstract <U> ParseResult<U> flatmapWithNext(Function<T, ? extends Function<CharReader, ParseResult<U>>> f);

    public abstract T getOrElse(T defaultValue);
    public abstract T getOrThrow();
    public abstract RuntimeException getException();
    public abstract CharReader getNext();

    public abstract ParseResult<T> or(ParseResult<T> p);

    public static <T> ParseResult<T> success(final T result, final CharReader next) {
        return new ParseResult<T>() {
            @Override
            public Type getType() {
                return Type.SUCCESS;
            }

            @Override
            public <U> ParseResult<U> map(Function<T, U> f) {
                return success(f.apply(result), next);
            }

            @Override
            public <U> ParseResult<U> flatmapWithNext(Function<T, ? extends Function<CharReader, ParseResult<U>>> f) {
                return f.apply(result).apply(next);
            }

            @Override
            public T getOrElse(T defaultValue) {
                return result;
            }

            @Override
            public T getOrThrow() {
                return result;
            }

            @Override
            public RuntimeException getException() {
                throw new UnsupportedOperationException();
            }

            @Override
            public CharReader getNext() {
                return next;
            }

            @Override
            public ParseResult<T> or(ParseResult<T> p) {
                return this;
            }
        };
    }

    public static <T> ParseResult<T> failure(RuntimeException exception, CharReader next) {
        return new ParseResult<T>() {
            @Override
            public Type getType() {
                return Type.FAILURE;
            }

            @Override
            public <U> ParseResult<U> map(Function<T, U> f) {
                return (ParseResult<U>) this;
            }

            @Override
            public <U> ParseResult<U> flatmapWithNext(Function<T, ? extends Function<CharReader, ParseResult<U>>> f) {
                return (ParseResult<U>) this;
            }

            @Override
            public T getOrElse(T defaultValue) {
                return defaultValue;
            }

            @Override
            public T getOrThrow() {
                throw exception;
            }

            @Override
            public RuntimeException getException() {
                return exception;
            }

            @Override
            public CharReader getNext() {
                return next;
            }

            @Override
            public ParseResult<T> or(ParseResult<T> p) {
                return p;
            }
        };
    }

    public static <T> ParseResult<T> error(RuntimeException exception, CharReader next) {
        return new ParseResult<T>() {
            @Override
            public Type getType() {
                return Type.ERROR;
            }

            @Override
            public <U> ParseResult<U> map(Function<T, U> f) {
                return (ParseResult<U>) this;
            }

            @Override
            public <U> ParseResult<U> flatmapWithNext(Function<T, ? extends Function<CharReader, ParseResult<U>>> f) {
                return (ParseResult<U>) this;
            }

            @Override
            public T getOrElse(T defaultValue) {
                return defaultValue;
            }

            @Override
            public T getOrThrow() {
                throw exception;
            }

            @Override
            public RuntimeException getException() {
                return exception;
            }

            @Override
            public CharReader getNext() {
                return next;
            }

            @Override
            public ParseResult<T> or(ParseResult<T> p) {
                return this;
            }
        };
    }

}
