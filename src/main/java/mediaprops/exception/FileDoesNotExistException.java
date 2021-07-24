package mediaprops.exception;

public final class FileDoesNotExistException extends RuntimeException {
    public FileDoesNotExistException(String msg) {
        super(msg);
    }
}