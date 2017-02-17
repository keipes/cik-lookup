package lol.driveways.xbrl.cache;

import lol.driveways.xbrl.exceptions.GramNotFoundException;
import lol.driveways.xbrl.proto.XBRLProto;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LoaderThing {


    private final String cachePath;
    private final Map<String, XBRLProto.NameCache> cachedNameCaches;
    private final Map<String, XBRLProto.GramBucket> cachedGramBuckets;

    public LoaderThing() {
        this.cachePath = "/cache";
        this.cachedNameCaches = new HashMap<>();
        this.cachedGramBuckets = new HashMap<>();
    }

    public XBRLProto.NameCache getNamesCacheMemoized(final Integer cik)  {
        final String bucket = Common.bucketKey(cik);
        return this.cachedNameCaches.computeIfAbsent(bucket, k -> getNamesCache(cik));
    }

    public XBRLProto.NameCache getNamesCache(final Integer cik) {
        final XBRLProto.NameCache.Builder cacheBuilder = XBRLProto.NameCache.newBuilder();
        final String cachePath = this.cachePath + "/" + Common.bucketKey(cik);
        try (final BufferedInputStream in = (BufferedInputStream) LoaderThing.class.getResourceAsStream(cachePath)) {
            if (in.available() == 0) {
                System.out.println(cachePath);
            }
            System.out.println(cachePath);

            cacheBuilder.mergeFrom(in);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return cacheBuilder.build();
    }

    public XBRLProto.GramCache getGramCacheMemoized(final String gram)  {
        final String bucket = this.cachePath + "/" + Common.gramBucketKey(gram);
        this.cachedGramBuckets.computeIfAbsent(bucket, this::loadBucket);
        return this.cachedGramBuckets.get(bucket).getGramCacheOrDefault(gram, XBRLProto.GramCache.getDefaultInstance());
    }

    private XBRLProto.GramBucket loadBucket(final String bucketPath) {
        final XBRLProto.GramBucket.Builder cacheBuilder = XBRLProto.GramBucket.newBuilder();
        try (final BufferedInputStream in = (BufferedInputStream) LoaderThing.class.getResourceAsStream(bucketPath)) {
            cacheBuilder.mergeFrom(in);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return cacheBuilder.build();
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
        final String gramPath = this.cachePath + "/" + Common.gramPath(gram);
        try (final InputStream in = LoaderThing.class.getResourceAsStream(gramPath)) {
            cacheBuilder.mergeFrom(in);
        } catch (final IOException e) {
            throw new GramNotFoundException(gram, gramPath, e);
        }
        return cacheBuilder.build();
    }
}
