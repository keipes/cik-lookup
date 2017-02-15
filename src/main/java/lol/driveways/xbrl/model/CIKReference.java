package lol.driveways.xbrl.model;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CIKReference {

    private final static int minSize = 3;
    private final static int maxSize = 3;
    private String name;
    private String nameLower;
    private Integer cik;
    private Map<String, Map<Integer, List<Integer>>> selfMap = null;
    private Map<Integer, List<String>> cikNamesMap;

    public CIKReference(final String name, final Integer cik) {
        this.name = name;
        this.nameLower = name.toLowerCase();
        this.cik = cik;
        this.selfMap = new HashMap<>();
        this.cikNamesMap = new HashMap<>();
        this.cikNamesMap.put(this.cik, Arrays.asList(this.name));
    }

    public Map<String, Map<Integer, List<Integer>>> getMap() {
        return selfMap;
    }

    public Integer getCik() {
        return this.cik;
    }

    public String getName() {
        return this.name;
    }

    public List<String> knownNames(final Integer cik) {
        return this.cikNamesMap.get(cik);
    }

    public void addScore(final String gram, final Integer score, final Integer cik) {
        if (!selfMap.containsKey(gram)) {
            selfMap.put(gram, new HashMap<>());
        }
        if (!selfMap.get(gram).containsKey(score)) {
            final List<Integer> list = new ArrayList<>();
            list.add(cik);
            selfMap.get(gram).put(score, list);
        } else {
            selfMap.get(gram).get(score).add(cik);
        }
    }

    public void merge(final CIKReference next) {
        this.cikNamesMap.computeIfAbsent(next.getCik(), k -> new ArrayList<>());
        this.cikNamesMap.get(next.getCik()).add(next.getName());
        next.nGramScores().entrySet().forEach((entry) -> {
            final String gram = entry.getKey();
            final Integer score = entry.getValue();
            final Integer cik = next.getCik();
            addScore(gram, score, cik);
        });
    }

    public Map<String, Integer> nGramScores() {
        final Map<String, Integer> scores = new HashMap<>();
        this.nGrams().forEach((gram) -> {
            if (scores.containsKey(gram)) {
                scores.put(gram, scores.get(gram) + 1);
            } else {
                scores.put(gram, 1);
            }
        });
        return scores;
    }

    public Stream<String> nGrams() {
        return this.tokens()
                .stream()
                .flatMap((token) -> IntStream.range(0, token.length() - maxSize + 1)
                        .mapToObj((start) -> token.substring(start, start + maxSize)));
    }

    final Pattern pattern = Pattern.compile("\\d+|[a-z]+|[/()#$!&%@]+");

    public List<String> tokens() {
        List<String> l = new ArrayList<>();
        Matcher matcher = pattern.matcher(nameLower);
//        matcher.
        while(matcher.find()) {
            l.add(nameLower.substring(matcher.start(), matcher.end()));
        }
        return l;
    }

    public String toString() {
        Map<String, Map<Integer, List<Integer>>> data = getMap();
        return new StringBuilder()
                .append(this.name).append(" ").append(this.cik).append('\n')
                .append('\t').append(this.tokens().stream().collect(Collectors.joining(","))).append('\n')
                .append('\t').append(this.nGrams().collect(Collectors.joining(","))).append('\n')
                .append(data.keySet().stream().map(
                        (gram) -> gram + "\n" + data.get(gram).entrySet().stream().map(
                                (e) -> "" + e.getKey() + " : " + e.getValue() + '\n'
                        ).collect(Collectors.joining())
                ).collect(Collectors.joining()))
                .toString();
    }

    public List<CIKScore> search(final String term, final Long numResults) {
        final CIKReference searchCIK = new CIKReference(term, 0);
        Map<Integer, Integer> scoreMap = new HashMap<>();
        searchCIK.nGrams().forEach((gram) -> this.getMap().get(gram).entrySet().forEach((entry) -> {
             final Integer score = entry.getKey();
             final List<Integer> ciks = entry.getValue();
             ciks.forEach((cik) -> {
                 final Integer oldScore = scoreMap.get(cik);
                 if (oldScore == null) {
                     scoreMap.put(cik, score);
                 } else {
                     scoreMap.put(cik, score + oldScore);
                 }
             });
        }));
        final List<CIKScore> scores = scoreMap.entrySet().stream()
                .map((entry) -> {
            final Integer cik = entry.getKey();
            final float score = (float) entry.getValue() / this.knownNames(cik).size();
            return new CIKScore(cik, score);
        })
                .collect(Collectors.toList());
        Collections.sort(scores);
        return scores.stream()
                .limit(numResults)
                .collect(Collectors.toList());
    }
}
