package lol.driveways.xbrl.cache;

import lol.driveways.xbrl.exceptions.GramNotFoundException;
import lol.driveways.xbrl.proto.XBRLProto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Optional;

public class LoaderThing {


    private final String cachePath;
    private final String knownNames;

    public LoaderThing() {
        this.cachePath = "/cache";
        this.knownNames = this.cachePath + "/knownNames.nc";
    }

    public XBRLProto.NameCache getNamesCache() {
        final XBRLProto.NameCache.Builder cacheBuilder = XBRLProto.NameCache.newBuilder();
        try (final InputStream in = LoaderThing.class.getResourceAsStream(this.knownNames)) {
            cacheBuilder.mergeFrom(in);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return cacheBuilder.build();
    }

    public Optional<XBRLProto.GramCache> getGramCache(final String gram) {
        try {
            return Optional.of(loadGram(gram));
        } catch (final GramNotFoundException e) {
            System.err.println(e.getMessage());
            return Optional.empty();
        }
    }

    private XBRLProto.GramCache loadGram(final String gram) throws GramNotFoundException {
        final XBRLProto.GramCache.Builder cacheBuilder = XBRLProto.GramCache.newBuilder();
        final String gramPath = this.cachePath + "/" + Common.gramPath(gram);
        try (final InputStream in = LoaderThing.class.getResourceAsStream(gramPath)) {
            cacheBuilder.mergeFrom(in);
        } catch (final IOException e) {
            throw new GramNotFoundException(gram, gramPath, e);
        }
        return cacheBuilder.build();
    }
}
