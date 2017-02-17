package lol.driveways.xbrl;

import lol.driveways.xbrl.ciklookup.DiskGramSearch;
import lol.driveways.xbrl.ciklookup.Search;

import java.util.List;
import java.util.stream.Collectors;

public class CIKLookup {
    public static void main(final String[] args) {
        final String dir = "D:\\Code\\cik-lookup\\build";
        Search search = new DiskGramSearch(dir);
        search.search("google inc", 10L).forEach((result) -> {
            List<String> names = search.knownNames(result.getCik()).getNameList();
            names.stream().collect(Collectors.joining(" "));

            System.out.println(result.getScore() + " " + names.stream().collect(Collectors.joining(" "))); //master.knownNames(result.getCik()));
        });
    }
}
