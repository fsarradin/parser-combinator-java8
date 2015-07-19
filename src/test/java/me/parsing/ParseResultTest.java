package me.parsing;

import org.junit.Test;

import static me.parsing.ParseResult.success;
import static org.assertj.core.api.Assertions.assertThat;

public class ParseResultTest {

    @Test
    public void should_() throws Exception {
        ParseResult<Integer> result =
                success("24", new CharReader("2442", 2))
                .flatmapWithNext(r -> input -> success(Integer.valueOf(r), input));

        assertThat(result.getOrThrow()).isEqualTo(24);
        assertThat(result.getNext().offset).isEqualTo(2);
    }

}