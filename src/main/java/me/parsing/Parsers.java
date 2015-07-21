package me.parsing;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parsers {

    private Parsers() { throw new UnsupportedOperationException(); }

    public static Parser<String> string(String s) {
        Parser<String> parser = input -> {
            int size = s.length();
            if (!input.sizeIsGreaterOrEqualTo(size))
                return ParseResult.failure(new NoSuchElementException("\"" + s + "\""), input);
            if (input.peek(size).toString().equals(s))
                return ParseResult.success(s, input.drop(size));
            return ParseResult.failure(new NoSuchElementException(s), input);
        };
        return parser.withName("\"" + s + "\"");
    }

    public static Parser<String> stringOfSize(int size) {
        Parser<String> parser = input -> {
            if (!input.sizeIsGreaterOrEqualTo(size))
                return ParseResult.failure(new NoSuchElementException("token of size " + size), input);
            String s = input.peek(size).toString();
            return ParseResult.success(s.trim(), input.drop(size));
        };
        return parser.withName("string(" + size + ")");
    }

    public static Parser<Integer> toInteger(Parser<String> parser) {
        return parser.flatmap(s -> input -> {
            try {
                return ParseResult.success(Integer.valueOf(s), input);
            } catch (NumberFormatException e) {
                return ParseResult.failure(e, input);
            }
        });
    }

    public static Parser<Integer> integer(int i) {
        String s = String.valueOf(i);
        return toInteger(string(s)).withName(s);
    }

    public static Parser<Integer> integerOfSize(int size) {
        return toInteger(stringOfSize(size)).withName("integer(" + size + ")");
    }

    public static Parser<LocalDate> dateYYYYMMDD() {
        return integerOfSize(4).then(integerOfSize(2).then(integerOfSize(2)))
                .withName("date(YYYYMMDD)")
                .map(p -> LocalDate.of(p.first, p.second.first, p.second.second));
    }

    public static Parser<String> regex(String pattern) {
        return regex(Pattern.compile(pattern));
    }

    public static Parser<String> regex(Pattern pattern) {
        return new Parser<String>() {
            @Override
            public ParseResult<String> apply(CharReader input) {
                Matcher matcher = pattern.matcher(input.source);
                if (!matcher.find(input.offset) || matcher.start() != input.offset) {
                    return ParseResult.failure(new NoSuchElementException(pattern.pattern()), input);
                }
                int size = matcher.end() - matcher.start();
                return ParseResult.success(matcher.group(), input.drop(size));
            }

            @Override
            public String getName() {
                return "regex(" + pattern.pattern() + ")";
            }
        };
    }

    public static <T> ParseResult<T> parse(String s, Parser<T> parser) {
        return parser.apply(new CharReader(s));
    }

}
