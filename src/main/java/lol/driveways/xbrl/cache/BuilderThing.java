package lol.driveways.xbrl.cache;

import lol.driveways.xbrl.ParseColeft;
import lol.driveways.xbrl.exceptions.GramNotFoundException;
import lol.driveways.xbrl.model.CIKReference;
import lol.driveways.xbrl.proto.XBRLProto;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BuilderThing {

    public static void main(final String[] args) {
        System.out.println(args[0]);
        BuilderThing cache = new BuilderThing(args[0]);
        CIKReference master = ParseColeft.loadMaster(ParseColeft.getLines());
        Map<String, Map<Integer, List<Integer>>> map = master.getMap();
        cache.gramsToDisk(master.getMap());
        cache.namesToDisk(master.knownNames());
    }

    private final String cachePath;
    private final String knownNames;

    public BuilderThing(final String outputDir) {
        this.cachePath = Paths.get(outputDir, "cache").toString();
        this.knownNames = Paths.get(this.cachePath, "knownNames.nc").toString();
    }

    private void gramsToDisk(final Map<String, Map<Integer, List<Integer>>> grams) {
        grams.keySet().forEach((gram) -> toDisk(gram, toProto(gram, grams.get(gram))));
    }

    private void toDisk(final String gram, final XBRLProto.GramCache cache) {
        try {
            new File(cachePath).mkdirs();
            final String path = Paths.get(this.cachePath, Common.gramPath(gram)).toString();
            try (FileOutputStream out = new FileOutputStream(path)) {
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

    private void namesToDisk(final Map<Integer, List<String>> knownNames) {
        XBRLProto.NameCache cache = toNameCache(knownNames);
        try {
            new File(cachePath).mkdirs();
            try (FileOutputStream out = new FileOutputStream(this.knownNames)) {
                cache.writeTo(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private XBRLProto.NameCache toNameCache(final Map<Integer, List<String>> knownNames) {
        final XBRLProto.NameCache.Builder builder = XBRLProto.NameCache.newBuilder();
        knownNames.entrySet().forEach(entry -> builder.putNameMap(entry.getKey(), toNames(entry.getValue())));
        return builder.build();
    }

    private XBRLProto.Names toNames(final List<String> names) {
        return XBRLProto.Names.newBuilder().setName(names.get(0)).setNumNames(names.size()).build();
    }
}
