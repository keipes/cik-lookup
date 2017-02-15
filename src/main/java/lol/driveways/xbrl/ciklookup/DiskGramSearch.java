package lol.driveways.xbrl.ciklookup;

import lol.driveways.xbrl.cache.GramCacheBuilderThing;
import lol.driveways.xbrl.model.CIKReference;
import lol.driveways.xbrl.model.CIKScore;
import lol.driveways.xbrl.proto.XBRLProto;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;

public class DiskGramSearch implements Search {

    private final GramCacheBuilderThing gramCacheBuilderThing;
    private Long prev;

    private Long startTime;

    public DiskGramSearch(final String outputdir) {
        startTime = System.currentTimeMillis();
        prev = startTime;
        bench("load search");
        gramCacheBuilderThing = new GramCacheBuilderThing(outputdir);
        bench("got gram cache");
    }

    private synchronized void bench(final String msg) {
        final Long now = System.currentTimeMillis();
        out.println(String.format("%d %d %s", now - startTime, now - prev, msg));
        prev = now;
    }

    @Override
    public List<CIKScore> search(final String query, final Long numResults) {
        bench("start");
        final CIKReference searchReference = new CIKReference(query, 0);
        ConcurrentHashMap<Integer, Integer> scoreMap = new ConcurrentHashMap<>();
        bench("map initialized");
        Map<String, Integer> gramFrequency = new HashMap<>();
//        searchReference.nGrams();
        Stream<String> grams = searchReference.nGrams();
        bench("got grams");
        grams.forEach((gram) -> {
            final Integer old = gramFrequency.get(gram);
            if (old == null) {
                gramFrequency.put(gram, 1);
            } else {
                gramFrequency.put(gram, old + 1);
            }
        });
        bench("grams filtered");
        final Set<String> gramSet = gramFrequency.keySet();
        final ForkJoinPool pool = new ForkJoinPool(1);
        try {
            pool.submit(() -> gramSet.stream()
                    .parallel()
                    .map(gramCacheBuilderThing::getGramCache)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach((cache) -> processCache(scoreMap, cache, gramFrequency.get(cache.getGram())))).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        pool.shutdown();

        bench("map built");
        final List<CIKScore> scores = scoreMap.entrySet().stream()
                .map((entry) -> {
                    final Integer cik = entry.getKey();
                    final float score = (float) entry.getValue();// / this.knownNames(cik).size();
                    return new CIKScore(cik, score);
                })
                .collect(Collectors.toList());
        bench("to CIK score");
        Collections.sort(scores);
        bench("sorted");
        return scores.stream()
                .limit(numResults)
                .collect(Collectors.toList());
    }

    private void processCache(final ConcurrentHashMap<Integer, Integer> scoreMap, final XBRLProto.GramCache cache, final Integer gramFrequency) {
        bench(cache.getGram());
        cache.getScoreList().forEach((score) -> score.getCikList().forEach((cik) -> {
            final Integer addScore = score.getScore() * gramFrequency;
            scoreMap.compute(cik, (key, oldScore) -> {
                if (oldScore == null) {
                    return addScore;
                } else {
                    return oldScore + addScore;
                }
            });
        }));
    }
}
