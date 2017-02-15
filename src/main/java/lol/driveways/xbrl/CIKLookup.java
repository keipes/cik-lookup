package lol.driveways.xbrl;

import lol.driveways.xbrl.ciklookup.DiskGramSearch;
import lol.driveways.xbrl.ciklookup.Search;

public class CIKLookup {
    public static void main(final String[] args) {
//        ParseColeft p = new ParseColeft();

        Search search = new DiskGramSearch("D:\\Code\\cik-lookup\\build");
        search.search("google inc lcc llc lkj;laKSDJ;oaisud;lkajsdklj aaa llc com com com llc inc inc inc ", 10L).forEach((result) -> {
            System.out.println(result.getScore() + " " + result.getCik()); //master.knownNames(result.getCik()));
        });
    }
}
