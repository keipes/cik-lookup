package lol.driveways.xbrl.model;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CIKReference {

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
        this.cikNamesMap.put(this.cik, Collections.singletonList(this.name));
    }

    public Map<String, Map<Integer, List<Integer>>> getMap() {
        return selfMap;
    }

    private Integer getCik() {
        return this.cik;
    }

    private String getName() {
        return this.name;
    }

//    public List<String> knownNames(final Integer cik) {
//        return this.cikNamesMap.get(cik);
//    }

    private void addScore(final String gram, final Integer score, final Integer cik) {
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

    private Map<String, Integer> nGramScores() {
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

    private final Pattern pattern = Pattern.compile("\\d+|[a-z]+|[/()#$!&%@]+");

    private List<String> tokens() {
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
        return this.name + " " + this.cik + '\n' +
                '\t' + this.tokens().stream().collect(Collectors.joining(",")) + '\n' +
                '\t' + this.nGrams().collect(Collectors.joining(",")) + '\n' +
                data.keySet().stream().map(
                        (gram) -> gram + "\n" + data.get(gram).entrySet().stream().map(
                                (e) -> "" + e.getKey() + " : " + e.getValue() + '\n'
                        ).collect(Collectors.joining())
                ).collect(Collectors.joining());
    }
}
