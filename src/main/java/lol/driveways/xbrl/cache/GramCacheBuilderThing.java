package lol.driveways.xbrl.cache;

import lol.driveways.xbrl.ParseColeft;
import lol.driveways.xbrl.exceptions.GramNotFoundException;
import lol.driveways.xbrl.model.CIKReference;
import lol.driveways.xbrl.proto.XBRLProto;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GramCacheBuilderThing {

    public static void main(final String[] args) {
        System.out.println(args[0]);
        GramCacheBuilderThing cache = new GramCacheBuilderThing(args[0]);
        CIKReference master = ParseColeft.loadMaster(ParseColeft.getLines(null));
        cache.gramsToDisk(master.getMap());
    }

    private final Path cachePath;
    private final String cache;

    public GramCacheBuilderThing(final String outputDir) {
        this.cachePath = Paths.get(outputDir, "cache");
        this.cache = Paths.get(outputDir, "cache").toString();
    }

    public void gramsToDisk(final Map<String, Map<Integer, List<Integer>>> grams) {
        grams.keySet().forEach((gram) -> {
            toDisk(gram, toProto(gram, grams.get(gram)));
        });
    }

    private void toDisk(final String gram, final XBRLProto.GramCache cache) {
        try {
            try (FileOutputStream out = new FileOutputStream(String.valueOf(gramPath(gram)))) {
                cache.writeTo(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private XBRLProto.GramCache toProto(final String gram, final Map<Integer, List<Integer>> scores) {
        final XBRLProto.GramCache.Builder cache = XBRLProto.GramCache.newBuilder();
        cache.setGram(gram);
        scores.entrySet().forEach(
                (entry) -> cache.addScore(
                        XBRLProto.Score.newBuilder()
                                .setScore(entry.getKey())
                                .addAllCik(entry.getValue()).build()
                )
        );
        return cache.build();
    }

    private Path gramPath(final String gram) {
        String name = gram;
        try {
            name = URLEncoder.encode(gram, "utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Paths.get(cache, String.format("_%s.gc2", name));
    }

    public Optional<XBRLProto.GramCache> getGramCache(final String gram) {
        try {
            return Optional.of(loadGram(gram));
        } catch (final GramNotFoundException e) {
            System.err.println(e.getMessage());
            return Optional.empty();
        }
    }

    private XBRLProto.GramCache loadGram(final String gram) throws GramNotFoundException {
        final XBRLProto.GramCache.Builder cacheBuilder = XBRLProto.GramCache.newBuilder();
        final String gramPath = String.valueOf(gramPath(gram));
        try (FileInputStream fIn = new FileInputStream(gramPath)) {
            cacheBuilder.mergeFrom(fIn);
        } catch (final IOException e) {
            throw new GramNotFoundException(gram, gramPath, e);
        }
        return cacheBuilder.build();
    }
}
