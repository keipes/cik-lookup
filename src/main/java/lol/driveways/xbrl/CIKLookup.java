package lol.driveways.xbrl;

import com.amazonaws.services.lambda.runtime.Context;
import lol.driveways.xbrl.ciklookup.GramSearch;
import lol.driveways.xbrl.ciklookup.Search;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CIKLookup {
    public static void main(final String[] args) {
        CIKLookup lookup = new CIKLookup();
        lookup.lambdaHandler("google inc", null);
//        Search search = new GramSearch();
//        search.search("google inc", 10L).forEach((result) -> {
//            String name = search.knownNames(result.getCik()).getName();
//            System.out.println(result.getScore() + " " + name);
//        });
    }

    public String lambdaHandler(final String input, final Context context) {
        Search search = new GramSearch();
        final List<String> retVal = new ArrayList<>();
        search.search("google inc", 10L).forEach((result) -> {
            String name = search.knownNames(result.getCik()).getName();
            System.out.println(result.getScore() + " " + name);
            retVal.add(result.getScore() + " " + name);// + '\n';
        });
        return retVal.stream().collect(Collectors.joining("\n"));
    }
}
