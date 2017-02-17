package lol.driveways.xbrl.cache;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

class Common {
    static String gramPath(final String gram) {
        String name = gram;
        try {
            name = URLEncoder.encode(gram, "utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("_%s.gc2", name);
    }
}
