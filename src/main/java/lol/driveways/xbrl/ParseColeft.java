package lol.driveways.xbrl;

import lol.driveways.xbrl.model.CIKReference;
import lol.driveways.xbrl.model.CIKScore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.System.out;

public class ParseColeft {

    private static final String COLEFT = "/cik.coleft.c";
    private Long prev = System.currentTimeMillis();

    public ParseColeft() {
        bench("start");
        InputStream coleft = ParseColeft.class.getResourceAsStream(COLEFT);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(coleft))) {
            bench("reading lines");
            final CIKReference master = br.lines()
//                    .limit(10000L)
                    .map(ParseColeft::lineToCik)
                    .reduce(null, (cumulative, next) -> {
                        if (cumulative == null) {
                            cumulative = new CIKReference("", 0);
                        }
                        cumulative.merge(next);
                        return cumulative;
                    });
            bench("searching");
            final List<CIKScore> results = master.search("amazon web", 100L);
            results.forEach((result) -> {
                System.out.println(result.getScore() + " " + master.knownNames(result.getCik()));
            });
//            results.forEach(out::println);
//            System.out.println(master.toString());
            bench("done");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            coleft.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void bench(final String msg) {
        final Long now = System.currentTimeMillis();
        out.println(String.format("%d %s", now - prev, msg));
        prev = now;
    }

    public static Stream<String> getLines(final Long lineLimit) {
        InputStream coleft = ParseColeft.class.getResourceAsStream(COLEFT);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(coleft));
        final Stream<String> lines = reader.lines();
        if (lineLimit != null) {
            return lines.limit(lineLimit);
        } else {
            return lines;
        }
    }

    public static CIKReference loadMaster(final Stream<String> lines) {
        return lines.map(ParseColeft::lineToCik)
                .reduce(null, (cumulative, next) -> {
                    if (cumulative == null) {
                        cumulative = new CIKReference("", 0);
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
