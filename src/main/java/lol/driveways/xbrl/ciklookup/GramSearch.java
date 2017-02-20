package lol.driveways.xbrl.ciklookup;

import lol.driveways.xbrl.cache.LoaderThing;
import lol.driveways.xbrl.model.CIKReference;
import lol.driveways.xbrl.model.CIKScore;
import lol.driveways.xbrl.model.NGramBuilder;
import lol.driveways.xbrl.proto.XBRLProto;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GramSearch implements Search {

    private static final Logger log = Logger.getLogger(GramSearch.class);

    private final LoaderThing loaderThing;
    private Long prev;

    private Long startTime;

    public GramSearch() {
        log.debug("load");
        startTime = System.currentTimeMillis();
        prev = startTime;
        loaderThing = new LoaderThing();
    }

    private synchronized void bench(final String msg) {
        final Long now = System.currentTimeMillis();
        log.info(String.format("%d %d %s", now - startTime, now - prev, msg));
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
        log.info("query: " + query + " limit: " + numResults);
//        final CIKReference searchReference = new CIKReference(query, 0);
        final ConcurrentHashMap<Integer, Integer> scoreMap = new ConcurrentHashMap<>();
        final Stream<String> nGrams = NGramBuilder.nGrams(query);
        final Map<String, Integer> gramFrequency = Common.scoreMap(nGrams);
        bench("got grams");
        bench("grams filtered");
        final Set<String> gramSet = gramFrequency.keySet();
//        final ForkJoinPool pool = new ForkJoinPool(gramSet.size());
        gramSet.stream()
                .map(loaderThing::getGramCacheMemoized)
                .forEach((cache) -> processCache(scoreMap, cache, gramFrequency.get(cache.getGram())));
//        try {
//            pool.submit(() -> gramSet.stream()
////                    .parallel()
//                    .map(loaderThing::getGramCacheMemoized)
////                    .filter(Optional::isPresent)
////                    .map(Optional::get)
//                    .forEach((cache) -> processCache(scoreMap, cache, gramFrequency.get(cache.getGram())))).get();
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
//        pool.shutdown();

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
