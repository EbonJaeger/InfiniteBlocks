package me.gnat008.infiniteblocks.exceptions;

public class RegionDatabaseException extends Exception {

    private static final long serialVersionUID = 17L;

    public RegionDatabaseException() {
        super();
    }

    public RegionDatabaseException(String message) {
        super(message);
    }

    public RegionDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegionDatabaseException(Throwable cause) {
        super(cause);
    }
}
