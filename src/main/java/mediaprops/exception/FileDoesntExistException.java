package mediaprops.exception;

public final class FileDoesntExistException extends RuntimeException {
    public FileDoesntExistException(String msg) {
        super(msg);
    }
}