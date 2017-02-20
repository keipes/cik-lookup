package lol.driveways.xbrl.cache;

import lol.driveways.xbrl.proto.XBRLProto;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LoaderThing {

    private static final Logger log = Logger.getLogger(LoaderThing.class);

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

    private XBRLProto.NameCache getNamesCache(final Integer cik) {
        final XBRLProto.NameCache.Builder cacheBuilder = XBRLProto.NameCache.newBuilder();
        final String cachePath = this.cachePath + "/" + Common.bucketKey(cik);
        try (final InputStream in = LoaderThing.class.getResourceAsStream(cachePath)) {
            cacheBuilder.mergeFrom(in);
        } catch (final IOException e) {
            log.error(e);
        }
        return cacheBuilder.build();
    }

    public XBRLProto.GramCache getGramCacheMemoized(final String gram)  {
        final String bucket = this.cachePath + "/" + Common.gramBucketKey(gram);
        this.cachedGramBuckets.computeIfAbsent(bucket, this::loadBucket);
        return this.cachedGramBuckets.get(bucket).getGramCacheOrDefault(gram, XBRLProto.GramCache.getDefaultInstance());
    }

    private XBRLProto.GramBucket loadBucket(final String bucketPath) {
        try (final InputStream in = LoaderThing.class.getResourceAsStream(bucketPath)) {
            return XBRLProto.GramBucket.parseFrom(in);
        } catch (final IOException e) {
            log.error(e);
            return XBRLProto.GramBucket.getDefaultInstance();
        }
    }
}
