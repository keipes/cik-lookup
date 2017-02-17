package lol.driveways.xbrl.cache;

import lol.driveways.xbrl.ParseColeft;
import lol.driveways.xbrl.model.CIKReference;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class BuilderThingTest {
    @Test
    public void testThing() {
        CIKReference master = ParseColeft.loadMaster(ParseColeft.getLines());
        BuilderThing b = new BuilderThing();
        Map<String, Map<Integer, List<String>>> buckets = b.namesToBuckets(master.knownNames());
        buckets.entrySet().forEach((entry) -> {
            System.out.println(entry.getKey() + " -> [" + entry.getValue().keySet().size() + "]");
        });
//        b.toNameCache(master.knownNames());
    }
}
