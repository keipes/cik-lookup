package lol.driveways.xbrl.ciklookup;

import lol.driveways.xbrl.cache.LoaderThing;
import lol.driveways.xbrl.model.CIKScore;
import lol.driveways.xbrl.model.NGramBuilder;
import lol.driveways.xbrl.proto.XBRLProto;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GramSearch implements Search {

    private static final Logger log = Logger.getLogger(GramSearch.class);

    private final LoaderThing loaderThing;

    public GramSearch() {
        loaderThing = new LoaderThing();
    }

    @Override
    public List<CIKScore> search(final String query, final Long resultLimit) {
        log.info("query: \"" + query + "\", limit: " + resultLimit);
        final Stream<String> grams = NGramBuilder.nGrams(query);
        log.info("got ngrams");
        final Map<String, Integer> gramFrequency = Common.scoreMap(grams);
        log.info("got gram frequency");
        final Set<String> gramSet = gramFrequency.keySet();
        log.info("got gram set");
        final Stream<XBRLProto.GramCache> caches = gramSet.stream()
                .map(loaderThing::getGramCacheMemoized);
        log.info("got caches");
        final Stream<CIKScore> cikScores = processCaches(caches, gramFrequency);
        log.info("caches processed");
        final Stream<CIKScore> mergedScores = mergedScores(cikScores);
        log.info("scores merged");
        final Stream<CIKScore> topScores = mergedScores.sorted().limit(resultLimit);
        log.info("got top scores");

        log.info("search done");
        return topScores.collect(Collectors.toList());
    }

    private Stream<CIKScore> mergedScores(Stream<CIKScore> scores) {
        final Map<Integer, CIKScore> scoreMap = scores.collect(Collectors.toConcurrentMap(
                CIKScore::getCik,
                score -> score,
                (score1, score2) -> new CIKScore(score1.getCik(), score1.getScore() + score2.getScore())));
        return scoreMap.values().stream();
    }

    private Stream<CIKScore> processCaches(final Stream<XBRLProto.GramCache> caches, final Map<String, Integer> gramFrequency) {
        return caches.flatMap((cache) -> processCache(cache, gramFrequency.get(cache.getGram())));
    }

    private Stream<CIKScore> processCache(final XBRLProto.GramCache cache, final Integer multiplier) {
        log.info("cache "+ cache.getGram());
        final Stream<XBRLProto.Score> scores = getSortedScores(cache.getScoreList());
        log.info("scores sorted");
        Stream<CIKScore> cikScores = scores
                .flatMap((score) -> score
                        .getCikList()
                        .stream()
                        .map((Integer cik) -> new CIKScore(cik, (float) score.getScore() * multiplier)));
        log.info("to CIKScore");
        return cikScores;
    }

    private Stream<XBRLProto.Score> getSortedScores(final List<XBRLProto.Score> scores) {
        return scores.stream()
                .sorted(Comparator.comparing(score -> (score.getScore())));
    }
    @Override
    public XBRLProto.Names knownNames(final Integer cik) {
        return loaderThing.getNamesCacheMemoized(cik).getNameMapOrDefault(cik, XBRLProto.Names.getDefaultInstance());
    }
}
