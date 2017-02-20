package lol.driveways.xbrl.model;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class NGramBuilder {

    private static final Integer maxSize = 3;
    private static final Logger log = Logger.getLogger(NGramBuilder.class);

    public static Stream<String> nGrams(final String name) {
        log.info("ngrams");
        final String[] tokens = name.split(" ");
        final List<String> nGrams = new ArrayList<>();
        for (final String token : tokens) {
            for (int i = 0; i < token.length() - maxSize + 1; i++) {
                nGrams.add(token.substring(i, i + maxSize));
            }
        }
        final Stream<String> gramStream = nGrams.stream();
        log.info("got stream");
        return gramStream;
    }
}
