package lol.driveways.xbrl.ciklookup;

import org.junit.BeforeClass;
import org.junit.Test;

public class GramSearchTest {

    private static Search subject;

    @BeforeClass
    public static void setUpClass() {
        subject = new GramSearch();
    }

    @Test
    public void search() {
        subject.search("google inc", 10L).forEach((result) -> {
            String name = subject.knownNames(result.getCik()).getName();
            System.out.println(result.getScore() + " " + name + " " + result.getCik());
        });
    }
}
