package lol.driveways.xbrl;

import lol.driveways.xbrl.model.CIKReference;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class ParseColeft {

    private static final String COLEFT = "/cik.coleft.c";

    public static Stream<String> getLines() {
        InputStream coleft = ParseColeft.class.getResourceAsStream(COLEFT);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(coleft));
        return reader.lines();
    }

    public static CIKReference loadMaster(final Stream<String> lines) {
        return lines.map(ParseColeft::lineToCik)
                .reduce(null, (cumulative, next) -> {
                    if (cumulative == null) {
                        cumulative = new CIKReference("", -1);
                    }
                    cumulative.merge(next);
                    return cumulative;
                });
    }

    private static CIKReference lineToCik(final String line) {
        int end = line.lastIndexOf(':');
        int split = line.lastIndexOf(':', end - 1);
        final String name = line.substring(0, split);
        final Integer cik = Integer.parseInt(line.substring(split + 1, end));
        return new CIKReference(name, cik);
    }



}
