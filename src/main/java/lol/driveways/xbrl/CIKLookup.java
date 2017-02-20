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
//import java.util.logging.Logger;

//import org.apache.logging.log4j.Logger;
import org.apache.log4j.Logger;

public class CIKLookup implements RequestHandler<LambdaQuery, LambdaResponse> {

    private static final Logger log = Logger.getLogger(CIKLookup.class);

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
    public LambdaResponse handleRequest(final LambdaQuery input, final Context context) {
        log.info("request start");
//        log.info("query: " + input.getQuery() + ", limit: " + input.getLimit());
        final Search search = new GramSearch();
        final List<LambdaResult> results = new ArrayList<>();
        search.search(input.getQuery(), input.getLimit()).forEach((cikScore) -> {
            final LambdaResult result = new LambdaResult();
            result.setCik(cikScore.getCik());
            result.setName(search.knownNames(cikScore.getCik()).getName());
            result.setScore(cikScore.getScore());
            results.add(result);
            String name = search.knownNames(cikScore.getCik()).getName();
            log.debug("result: " + cikScore.getScore() + " " + name);
        });
        final LambdaResponse lambdaResponse = new LambdaResponse();
        lambdaResponse.setResults(results);
        log.info("request done");
        return lambdaResponse;
    }
}
