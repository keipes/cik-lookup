package lol.driveways.xbrl;

import lol.driveways.xbrl.cache.BuilderThing;
import lol.driveways.xbrl.model.CIKReference;
import org.apache.log4j.Logger;

public class CacheGenerator {

    private static final Logger log = Logger.getLogger(CacheGenerator.class);
    private final BuilderThing builder;

    private CacheGenerator(final String outputPath) {
        log.info("generating cache at: " + outputPath);
        builder = new BuilderThing();
        builder.setPaths(outputPath);
    }

    private void generate() {
        CIKReference master = ParseColeft.loadMaster(ParseColeft.getLines());
        log.info("got master");
        builder.dumpNameBuckets(builder.namesToBuckets(master.knownNames()));
        log.info("names dumped");
        builder.gramBucketsToDisk(builder.gramMapToBuckets(master.getMap()));
        log.info("grams dumped");
    }

    private void clearCache() {
        builder.clearCache();
    }
    public static void main(final String[] args) {
        final CacheGenerator generator = new CacheGenerator(args[0]);
        generator.clearCache();
        generator.generate();
    }
}
