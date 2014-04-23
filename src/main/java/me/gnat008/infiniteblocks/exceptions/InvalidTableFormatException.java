package me.gnat008.infiniteblocks.exceptions;

/**
 * Created by Gnat008 on 4/23/2014.
 */
public class InvalidTableFormatException extends FatalConfigurationLoadingException {

    private static final long serialVersionUid = 4L;

    protected String updateFile;

    public InvalidTableFormatException(String updateFile) {
        super();

        this.updateFile = updateFile;
    }

    public String toString() {
        return "You need to update your database to the latest version.\n" +
                "\t\tPlease see " + this.updateFile;
    }
}
