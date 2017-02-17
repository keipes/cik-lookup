package lol.driveways.xbrl.ciklookup;

import lol.driveways.xbrl.model.CIKScore;
import lol.driveways.xbrl.proto.XBRLProto;

import java.util.List;

public interface Search {

    List<CIKScore> search(String query, Long resultLimit);

    XBRLProto.Names knownNames(Integer cik);
}
