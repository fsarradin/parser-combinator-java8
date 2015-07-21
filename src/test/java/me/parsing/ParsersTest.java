package me.parsing;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.function.Function;

import static me.parsing.Parsers.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ParsersTest {

    @Test
    public void should_parse_1_string() throws Exception {
        Parser<String> parser = string("abc");
        ParseResult<String> res = parse("abc", parser);

        assertThat(res.getType()).isEqualTo(ParseResult.Type.SUCCESS);
    }

    @Test
    public void should_parse_2_successive_strings() throws Exception {
        Parser<Pair<String, String>> parser = string("abc").then(string("def"));
        ParseResult<Pair<String, String>> result = parse("abcdef", parser);

        assertThat(result.getType()).isEqualTo(ParseResult.Type.SUCCESS);
    }

    @Test
    public void should_parse_1_of_2_strings() throws Exception {
        Parser<String> parser = string("abc").or(string("def"));
        ParseResult<String> result = parse("def", parser);

        assertThat(result.getType()).isEqualTo(ParseResult.Type.SUCCESS);
    }

    @Test
    public void should_parse_integer_with_limited_size() throws Exception {
        Parser<Integer> parser = integerOfSize(5);
        ParseResult<Integer> result = parse("1    ", parser);

        assertThat(result.getOrThrow()).isEqualTo(1);
    }

    @Test
    public void should_not_parse_when_not_integer() throws Exception {
        Parser<Integer> parser = integerOfSize(5);
        ParseResult<Integer> result = parse("1w   ", parser);

        assertThat(result.getType()).isEqualTo(ParseResult.Type.FAILURE);
    }

    @Test
    public void should_parse_given_integer() throws Exception {
        Parser<Integer> parser = integer(24);
        ParseResult<Integer> result = parse("24", parser);

        assertThat(result.getOrThrow()).isEqualTo(24);
    }

    @Test
    public void should_parse_date_YYYYMMDD() throws Exception {
        Parser<LocalDate> parser = dateYYYYMMDD();
        ParseResult<LocalDate> result = parse("20000101", parser);

        assertThat(result.getOrThrow()).isEqualTo(LocalDate.of(2000, 1, 1));
    }

    @Test
    public void should_parse_successive_integers() throws Exception {
        Parser<Pair<Integer, Integer>> parser = integer(24).then(integer(42));
        ParseResult<Pair<Integer, Integer>> result = parse("2442", parser);

        Pair<Integer, Integer> value = result.getOrThrow();

        assertThat(value.first).isEqualTo(24);
        assertThat(value.second).isEqualTo(42);
    }

    @Test
    public void should_parse_a_string_of_given_size() throws Exception {
        Parser<String> parser = stringOfSize(5);
        ParseResult<String> result = parse("12345", parser);

        assertThat(result.getType()).isEqualTo(ParseResult.Type.SUCCESS);
    }

    @Test
    public void should_parse_to_chain_of_pairs() throws Exception {
        Parser<Pair<String, Pair<String, Pair<Integer, Pair<String, Integer>>>>> parser = string("GL")
                .then(string("EC")
                        .then(integer(24)
                                .then(stringOfSize(5)
                                        .then(integerOfSize(4)))));
        ParseResult<Pair<String, Pair<String, Pair<Integer, Pair<String, Integer>>>>> result = parse("GLEC24UC459  41", parser);
        Pair<String, Pair<String, Pair<Integer, Pair<String, Integer>>>> value = result.getOrThrow();

        assertThat(value.first).isEqualTo("GL");
        assertThat(value.second.first).isEqualTo("EC");
        assertThat(value.second.second.first).isEqualTo(24);
        assertThat(value.second.second.second.first).isEqualTo("UC459");
        assertThat(value.second.second.second.second).isEqualTo(41);
    }

    @Test
    public void should_parse_according_to_regex() throws Exception {
        Parser<BigDecimal> parser =
                regex("[+-]?\\d+(\\.\\d+)?")
                        .map(BigDecimal::new);

        ParseResult<BigDecimal> result = parse("-42.1", parser);

        assertThat(result.getOrThrow()).isEqualTo("-42.1");
    }

    @Test
    public void should_parse_to_bean() throws Exception {
        class Bean {
            public final String id;
            public final int quantity;
            public final BigDecimal price;
            public final LocalDate date;

            public Bean(String id, int quantity, BigDecimal price, LocalDate date) {
                this.id = id;
                this.quantity = quantity;
                this.price = price;
                this.date = date;
            }
        }

        Function<String, BigDecimal> toPrice =
                value -> new BigDecimal(value).divide(new BigDecimal("100"), MathContext.DECIMAL128);
        Function<Pair<String, Pair<Integer, Pair<BigDecimal, LocalDate>>>, Bean> createBean =
                value -> new Bean(value.first, value.second.first, value.second.second.first, value.second.second.second);

        Parser<Bean> parser =
                string("PRD")
                    .skipThen(stringOfSize(5)
                        .then(integerOfSize(4)
                            .then(stringOfSize(8).map(toPrice)
                                .then(dateYYYYMMDD()))))
                .map(createBean);

        ParseResult<Bean> result = parse("PRDUC459  41    599920000101", parser);

        Bean value = result.getOrThrow();
        assertThat(value.id).isEqualTo("UC459");
        assertThat(value.quantity).isEqualTo(41);
        assertThat(value.price).isEqualTo("59.99");
        assertThat(value.date).isEqualTo(LocalDate.of(2000, 1, 1));
    }

    @Test
    public void perf_test() throws Exception {
        final String input = "PRDUC459  4120000101";
        class Bean {
            public final String id;
            public final int quantity;
            public final LocalDate date;

            public Bean(String id, int quantity, LocalDate date) {
                this.id = id;
                this.quantity = quantity;
                this.date = date;
            }
        }

        Parser<Bean> parser =
                string("PRD").skipThen(stringOfSize(5).then(integerOfSize(4).then(dateYYYYMMDD())))
                        .map(value -> new Bean(value.first, value.second.first, value.second.second));

        long start = System.nanoTime();
        for (int i = 0; i < 1_000_000; i++) {
            parse(input, parser);
        }
        long end = System.nanoTime();
        long delay = end - start;
        System.out.println("Delay: " + (delay / 1_000_000_000.0));
    }
}