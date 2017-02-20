package lol.driveways.xbrl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lol.driveways.xbrl.ciklookup.GramSearchV2;
import lol.driveways.xbrl.ciklookup.Search;
import lol.driveways.xbrl.model.LambdaQuery;
import lol.driveways.xbrl.model.LambdaResponse;
import lol.driveways.xbrl.model.LambdaResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class CIKLookup implements RequestHandler<LambdaQuery, LambdaResponse> {

    private static final Logger log = Logger.getLogger(CIKLookup.class);

    public static void main(final String[] args) throws IOException {
        char c = 'y';
        while(c != 'x') {
            doIt();
            c = 'x';
//            c = (char) System.in.read();
        }
    }

    private static void doIt() {
        CIKLookup lookup = new CIKLookup();
        LambdaQuery query = new LambdaQuery();
        query.setQuery("google inc");
        query.setLimit(10L);
        lookup.handleRequest(query, null);
    }

    @Override
    public LambdaResponse handleRequest(final LambdaQuery input, final Context context) {
        log.info("request start");
        final Search search = new GramSearchV2();
        final List<LambdaResult> results = new ArrayList<>();
        search.search(input.getQuery(), input.getLimit()).forEach((cikScore) -> {
            final LambdaResult result = new LambdaResult();
            final String name = search.knownNames(cikScore.getCik()).getName();
            result.setCik(cikScore.getCik());
            result.setName(name);
            result.setScore(cikScore.getScore());
            results.add(result);
            log.debug("result: " + cikScore.getScore() + " " + name);
        });
        final LambdaResponse lambdaResponse = new LambdaResponse();
        lambdaResponse.setResults(results);
        log.info("request done");
        return lambdaResponse;
    }
}
