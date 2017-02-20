package lol.driveways.xbrl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lol.driveways.xbrl.ciklookup.GramSearch;
import lol.driveways.xbrl.ciklookup.Search;
import lol.driveways.xbrl.model.LambdaQuery;
import lol.driveways.xbrl.model.LambdaResponse;
import lol.driveways.xbrl.model.LambdaResult;

import java.util.ArrayList;
import java.util.List;

public class CIKLookup implements RequestHandler<LambdaQuery, LambdaResponse> {
    public static void main(final String[] args) {
        CIKLookup lookup = new CIKLookup();
        LambdaQuery query = new LambdaQuery();
        query.setQuery("google inc");
        query.setLimit(10L);
        lookup.handleRequest(query, null);
    }

//    public String lambdaHandler(final String input, final Context context) {
//        Search search = new GramSearch();
//        final List<String> retVal = new ArrayList<>();
//        search.search("google inc", 10L).forEach((result) -> {
//            String name = search.knownNames(result.getCik()).getName();
//            System.out.println(result.getScore() + " " + name);
//            retVal.add(result.getScore() + " " + name);// + '\n';
//        });
//        return retVal.stream().collect(Collectors.joining("\n"));
//    }

    @Override
    public LambdaResponse handleRequest(LambdaQuery input, Context context) {
        final Search search = new GramSearch();
        final List<LambdaResult> results = new ArrayList<>();
        search.search(input.getQuery(), input.getLimit()).forEach((cikScore) -> {
            final LambdaResult result = new LambdaResult();
            result.setCik(cikScore.getCik());
            result.setName(search.knownNames(cikScore.getCik()).getName());
            result.setScore(cikScore.getScore());
            results.add(result);
            String name = search.knownNames(cikScore.getCik()).getName();
            System.out.println(cikScore.getScore() + " " + name);
        });
        final LambdaResponse lambdaResponse = new LambdaResponse();
        lambdaResponse.setResults(results);
        return lambdaResponse;
    }
}
