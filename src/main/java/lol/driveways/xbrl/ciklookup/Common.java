package lol.driveways.xbrl.ciklookup;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Common {

    static Map<String, Integer> scoreMap(final Stream<String> grams) {
        return grams.collect(Collectors.groupingBy(gram -> gram, Collectors.summingInt((s) -> 1)));
    }
}
