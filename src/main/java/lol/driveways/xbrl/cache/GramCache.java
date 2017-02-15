package lol.driveways.xbrl.cache;

import lol.driveways.xbrl.ParseColeft;
import lol.driveways.xbrl.exceptions.GramNotFoundException;
import lol.driveways.xbrl.model.CIKReference;
import lol.driveways.xbrl.proto.XBRLProto;
import lol.driveways.xbrl.proto.XBRLProto.CikList;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GramCache {

    public static void main(final String[] args) {
        System.out.println(args[0]);
        GramCache cache = new GramCache(args[0]);
        CIKReference master = ParseColeft.loadMaster(ParseColeft.getLines(null));
        cache.gramsToDisk(master.getMap());
    }

    private final Path cachePath;
    private final String cache;

    public GramCache(final String outputDir) {
        this.cachePath = Paths.get(outputDir, "cache");
        this.cache = Paths.get(outputDir, "cache").toString();
    }

    public void gramsToDisk(final Map<String, Map<Integer, List<Integer>>> grams) {
        grams.keySet().forEach((gram) -> {
            toDisk(gram, toProto(grams.get(gram)));
        });
    }

    private void toDisk(final String gram, final XBRLProto.GramCacheV1 cache) {
        try {
            try (FileOutputStream out = new FileOutputStream(String.valueOf(gramPath(gram)))) {
                cache.writeTo(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private XBRLProto.GramCacheV1 toProto(Map<Integer, List<Integer>> scores) {
        final XBRLProto.GramCacheV1.Builder cache = XBRLProto.GramCacheV1.newBuilder();
        scores.entrySet().forEach(
                (entry) -> cache.putGrams(
                        entry.getKey(),
                        CikList.newBuilder().addAllCiks(entry.getValue()).build()
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
        return Paths.get(cache, String.format("_%s.gc1", name));
    }

    public Optional<XBRLProto.GramCacheV1> getGram(final String gram) {
        try {
            return Optional.of(loadGram(gram));
        } catch (final GramNotFoundException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private XBRLProto.GramCacheV1 loadGram(final String gram) throws GramNotFoundException {
        final XBRLProto.GramCacheV1.Builder cacheBuilder = XBRLProto.GramCacheV1.newBuilder();
        final String gramPath = String.valueOf(gramPath(gram));
        try (FileInputStream fIn = new FileInputStream(gramPath)) {
            cacheBuilder.mergeFrom(fIn);
        } catch (final IOException e) {
            throw new GramNotFoundException(gram, gramPath, e);
        }
        System.out.println("gram");

        return cacheBuilder.build();
    }
}
