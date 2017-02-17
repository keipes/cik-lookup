package lol.driveways.xbrl.ciklookup;

import lol.driveways.xbrl.cache.LoaderThing;
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

public class GramSearch implements Search {

    private final LoaderThing loaderThing;
    private XBRLProto.NameCache nameCache = null;
    private Long prev;

    private Long startTime;

    public GramSearch() {
        startTime = System.currentTimeMillis();
        prev = startTime;
        bench("load search");
        loaderThing = new LoaderThing();
        bench("got gram cache");
    }

    private synchronized void bench(final String msg) {
        final Long now = System.currentTimeMillis();
        out.println(String.format("%d %d %s", now - startTime, now - prev, msg));
        prev = now;
    }


    public XBRLProto.Names knownNames(final Integer cik) {
        return loaderThing.getNamesCacheMemoized(cik).getNameMapOrDefault(cik, XBRLProto.Names.getDefaultInstance());
//        if (this.nameCache == null) {
//            this.nameCache = loaderThing.getNamesCache(cik);
//        }
//        return this.nameCache.getNameMapOrDefault(cik, XBRLProto.Names.getDefaultInstance());
    }

    @Override
    public List<CIKScore> search(final String query, final Long numResults) {
        bench("start");
        final CIKReference searchReference = new CIKReference(query, 0);
        final ConcurrentHashMap<Integer, Integer> scoreMap = new ConcurrentHashMap<>();
        bench("map initialized");
        final Map<String, Integer> gramFrequency = new HashMap<>();
        final Stream<String> grams = searchReference.nGrams();
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
//                    .parallel()
                    .map(loaderThing::getGramCacheMemoized)
//                    .filter(Optional::isPresent)
//                    .map(Optional::get)
                    .forEach((cache) -> processCache(scoreMap, cache, gramFrequency.get(cache.getGram())))).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        pool.shutdown();

        bench("map built");
        final List<CIKScore> scores = scoreMap.entrySet().stream()
                .map((entry) -> {
                    final Integer cik = entry.getKey();
                    final float score = entry.getValue(); //(float) entry.getValue() / this.knownNames(cik).getNumNames();
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

    private class SortableThing implements Comparable<SortableThing> {
        private Integer score;
        private Integer cik;

        SortableThing(Integer score, Integer cik) {
            this.score = score;
            this.cik = cik;
        }

        Integer getScore() {
            return score;
        }

        public Integer getCik() {
            return cik;
        }

        @Override
        public int compareTo(final SortableThing other) {
            return other.score - this.score;
        }
    }

    private PriorityQueue<SortableThing> topScores(final Map<Integer, Integer> scoreMap, final Integer maxScores) {
        final PriorityQueue<SortableThing> scores = new PriorityQueue<>();
        scoreMap.entrySet().forEach((entry) -> {
            final SortableThing score = new SortableThing(entry.getKey(), entry.getValue());
            while (scores.size() >= maxScores) {
                if (scores.peek().getScore()  < score.getScore()) {
                    scores.poll();
                    if (scores.offer(score)) {

                    } else {
                        System.err.println(score.cik);
                    }
                }
            }
        });
        return scores;
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
