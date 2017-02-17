package lol.driveways.xbrl.cache;

import com.google.protobuf.Any;
import com.google.protobuf.AnyOrBuilder;
import com.google.protobuf.Message;
import lol.driveways.xbrl.ParseColeft;
import lol.driveways.xbrl.exceptions.GramNotFoundException;
import lol.driveways.xbrl.model.CIKReference;
import lol.driveways.xbrl.proto.XBRLProto;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuilderThing {

    public static void main(final String[] args) {
        System.out.println(args[0]);
        BuilderThing cache = new BuilderThing();
        cache.setPaths(args[0]);
        CIKReference master = ParseColeft.loadMaster(ParseColeft.getLines());
//        cache.gramsToDisk(master.getMap());
//        cache.namesToDisk(master.knownNames());
        cache.dumpNameBuckets(cache.namesToBuckets(master.knownNames()));
        cache.gramBucketsToDisk(cache.gramMapToBuckets(master.getMap()));
    }

    private String cachePath = null;


    public BuilderThing() {

    }

    private void setPaths(final String outputDir) {
        this.cachePath = Paths.get(outputDir, "cache").toString();
    }

    private void gramsToDisk(final Map<String, Map<Integer, List<Integer>>> grams) {
        grams.keySet().forEach((gram) -> toDisk(gram, toProto(gram, grams.get(gram))));
    }

    private Map<String, Map<String, Map<Integer, List<Integer>>>> gramMapToBuckets(final Map<String, Map<Integer, List<Integer>>> grams) {
        final Map<String, Map<String, Map<Integer, List<Integer>>>> buckets = new HashMap<>();
        grams.entrySet().forEach((entry) -> {
            final String gram = entry.getKey();
            final String bucketKey = Common.gramBucketKey(entry.getKey());
            buckets.computeIfAbsent(bucketKey, (k) ->  new HashMap<>());
            buckets.get(bucketKey).put(gram, entry.getValue());
        });
        return buckets;
    }

    private void gramBucketsToDisk(final Map<String, Map<String, Map<Integer, List<Integer>>>> buckets) {
        buckets.forEach((bucket, gramCache) -> {
            final String outputPath = Paths.get(this.cachePath, bucket).toString();
            protoToDisk(outputPath, toGramBucket(gramCache));
        });
    }

    private XBRLProto.GramBucket toGramBucket(final Map<String, Map<Integer, List<Integer>>> grams) {
        final XBRLProto.GramBucket.Builder bucketBuilder = XBRLProto.GramBucket.newBuilder();
        grams.entrySet().forEach((entry) -> bucketBuilder.putGramCache(entry.getKey(), toProto(entry.getKey(), entry.getValue())));
        return bucketBuilder.build();
    }

    private void protoToDisk(final String path, final Message message) {
        try {
            new File(cachePath).mkdirs();
            try (FileOutputStream out = new FileOutputStream(path)) {
                message.writeTo(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void toDisk(final String gram, final XBRLProto.GramCache cache) {
        final String path = Paths.get(this.cachePath, Common.gramPath(gram)).toString();
        protoToDisk(path, cache);
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

    protected Map<String, Map<Integer, List<String>>> namesToBuckets(final Map<Integer, List<String>> knownNames) {
        final Map<String, Map<Integer, List<String>>> buckets = new HashMap<>();
        knownNames.entrySet().forEach((entry) -> {
            final String key = Common.bucketKey(entry.getKey());
            if (!buckets.containsKey(key)) {
                buckets.put(key, new HashMap<>());
            }
            buckets.get(key).put(entry.getKey(), entry.getValue());
        });
        return buckets;
    }

    private void dumpNameBuckets(final Map<String, Map<Integer, List<String>>> buckets) {
        buckets.entrySet().forEach((entry) -> {
            final String bucket = entry.getKey();
            final String outputPath = Paths.get(this.cachePath, bucket).toString();
            final XBRLProto.NameCache nameCache = toNameCache(buckets.get(bucket));
            protoToDisk(outputPath, nameCache);
        });
    }

    protected XBRLProto.NameCache toNameCache(final Map<Integer, List<String>> knownNames) {
        final XBRLProto.NameCache.Builder builder = XBRLProto.NameCache.newBuilder();
        knownNames.entrySet().forEach(entry -> builder.putNameMap(entry.getKey(), toNames(entry.getValue())));
        return builder.build();
    }

    private XBRLProto.Names toNames(final List<String> names) {
        return XBRLProto.Names.newBuilder().setName(names.get(0)).setNumNames(names.size()).build();
    }
}
