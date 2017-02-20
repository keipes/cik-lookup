package lol.driveways.xbrl.model;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NGramBuilderTest {

//    @Test
//    public void testTokens() {
//        final Stream<String> tokens = NGramBuilder.tokens("google inc9");
//        final Set<String> receivedTokens = tokens.collect(Collectors.toSet());
////        assertArrayEquals(receivedTokens.toArray(), new String[] {
////            "google",
////                "inc",
////                "9"
////        });
//        assertTrue(receivedTokens.contains("google"));
//        assertTrue(receivedTokens.contains("inc9"));
////        assertTrue(receivedTokens.contains("nc9"));
//        assertTrue(receivedTokens.size() == 2);
//    }

//    @Test
//    public void testNGrams() {
//        final String[] tokens = NGramBuilder.tokenArray("google inc9");
//        final Stream<String> nGrams = NGramBuilder.nGrams(tokens);
//
//        final Set<String> nGramSet = nGrams.collect(Collectors.toSet());
//        assertTrue(nGramSet.contains("goo"));
//        assertTrue(nGramSet.contains("oog"));
//        assertTrue(nGramSet.contains("ogl"));
//        assertTrue(nGramSet.contains("gle"));
//        assertTrue(nGramSet.contains("inc"));
//        assertTrue(nGramSet.contains("nc9"));
////        assertTrue(nGramSet.contains("9"));
//        assertTrue(nGramSet.size() == 6);
//    }

    @Test
    public void testNGramsByName() {
        final Stream<String> nGrams = NGramBuilder.nGrams("google inc9");
        final Set<String> nGramSet = nGrams.collect(Collectors.toSet());
        assertTrue(nGramSet.contains("goo"));
        assertTrue(nGramSet.contains("oog"));
        assertTrue(nGramSet.contains("ogl"));
        assertTrue(nGramSet.contains("gle"));
        assertTrue(nGramSet.contains("inc"));
        assertTrue(nGramSet.contains("nc9"));
//        assertTrue(nGramSet.contains("9"));
        assertTrue(nGramSet.size() == 6);
    }

//
//    @Test
//    public void testNGramsV1() {
//        final Stream<String> tokens = NGramBuilder.tokens("google inc9");
//        final Stream<String> nGrams = NGramBuilder.nGrams(tokens);
//
////        final String[] tokens = NGramBuilder.tokenArray("google inc9");
////        final Stream<String> nGrams = NGramBuilder.nGrams(tokens);
//
//        final Set<String> nGramSet = nGrams.collect(Collectors.toSet());
//        assertTrue(nGramSet.contains("goo"));
//        assertTrue(nGramSet.contains("oog"));
//        assertTrue(nGramSet.contains("ogl"));
//        assertTrue(nGramSet.contains("gle"));
//        assertTrue(nGramSet.contains("inc"));
//        assertTrue(nGramSet.contains("nc9"));
////        assertTrue(nGramSet.contains("9"));
//        assertTrue(nGramSet.size() == 6);
//    }
}
