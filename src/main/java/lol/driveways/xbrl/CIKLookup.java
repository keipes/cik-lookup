package lol.driveways.xbrl;

import lol.driveways.xbrl.ciklookup.DiskGramSearch;
import lol.driveways.xbrl.ciklookup.Search;

public class CIKLookup {
    public static void main(final String[] args) {
        final String dir = "D:\\Code\\cik-lookup\\build";
        Search search = new DiskGramSearch(dir);
        search.search("google inc", 10L).forEach((result) -> {
            String name = search.knownNames(result.getCik()).getName();
            System.out.println(result.getScore() + " " + name);
        });
    }
}
