package filemetalib;

public final class FileProperty<T> {
    static final int STRING_FIELD_TYPE = 0;

    public static final FileProperty<String> TITLE = new FileProperty<>(0, STRING_FIELD_TYPE);
    public static final FileProperty<String> SUB_TITLE = new FileProperty<>(1, STRING_FIELD_TYPE);
    public static final FileProperty<String> COMMENT = new FileProperty<>(2, STRING_FIELD_TYPE);
    public static final FileProperty<String> AUTHOR = new FileProperty<>(3, STRING_FIELD_TYPE);

    final int propOrdinal;
    final int type;

    private FileProperty(int propOrdinal, int type) {
        this.propOrdinal = propOrdinal;
        this.type = type;
    }
}