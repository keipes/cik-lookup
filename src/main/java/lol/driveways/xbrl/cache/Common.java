package lol.driveways.xbrl.cache;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

class Common {
    private static final Integer NUM_CHUNKS = 100;
    private static final Integer NUM_GRAM_CHUNKS = 100;

    static String bucketKey(final Integer cik) {
        return cik % NUM_CHUNKS + ".nc";
    }
    static String gramPath(final String gram) {
        String name = gram;
        try {
            name = URLEncoder.encode(gram, "utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("_%s.gc2", name);
    }

    static String gramBucketKey(final String gram) {
        return String.format("_%d.gc", gram.hashCode() % NUM_GRAM_CHUNKS);
    }
}
