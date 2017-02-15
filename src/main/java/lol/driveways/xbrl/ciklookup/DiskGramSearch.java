package lol.driveways.xbrl.ciklookup;

import lol.driveways.xbrl.cache.GramCache;
import lol.driveways.xbrl.model.CIKReference;
import lol.driveways.xbrl.model.CIKScore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class DiskGramSearch implements Search {

    final GramCache gramCache;
    private Long prev;
    public DiskGramSearch(final String outputdir) {
        prev = System.currentTimeMillis();
        bench("load search");
        gramCache = new GramCache(outputdir);
        bench("got gram cache");
    }
    private void bench(final String msg) {
        final Long now = System.currentTimeMillis();
        out.println(String.format("%d %s", now - prev, msg));
        prev = now;
    }

    @Override
    public List<CIKScore> search(final String query, final Long numResults) {
        bench("start");
        final CIKReference searchReference = new CIKReference(query, 0);
        ConcurrentHashMap<Integer, Integer> scoreMap = new ConcurrentHashMap<>();
        bench("map initialized");
        searchReference.nGrams()
                .parallel()
                .map(gramCache::getGram)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach((gc) -> {
            bench("start load gram");
                    gc.getGramsMap().entrySet().forEach((entry) -> {
                        final Integer score = entry.getKey();
                        entry.getValue().getCiksList().forEach((cik) -> {
                            final Integer oldScore = scoreMap.get(cik);
                            if (oldScore == null) {
                                scoreMap.put(cik, score);
                            } else {
                                scoreMap.put(cik, score + oldScore);
                            }
                        });
                    });
                    bench("gram loaded");
                });
        bench("map built");
        final List<CIKScore> scores = scoreMap.entrySet().stream()
                .map((entry) -> {
                    final Integer cik = entry.getKey();
                    final float score = (float) entry.getValue();// / this.knownNames(cik).size();
                    return new CIKScore(cik, score);
                })
                .collect(Collectors.toList());
        bench("to CIK score");
//        Collections.sort(scores);
        bench("sorted");
        return scores.stream()
                .limit(numResults)
                .collect(Collectors.toList());
    }
}
