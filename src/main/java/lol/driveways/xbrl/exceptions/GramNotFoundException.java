package lol.driveways.xbrl.exceptions;

public class GramNotFoundException extends Exception {
    public GramNotFoundException(final String gram, final String gramPath, final Exception e) {
        super(String.format("%s not found at expected location: %s", gram, gramPath), e);
    }
}
