package lol.driveways.xbrl.ciklookup;

import lol.driveways.xbrl.model.CIKScore;

import java.util.List;

public interface Search {

    List<CIKScore> search(final String query, final Long resultLimit);
}
