package mediaprops;

public final class MediaProperty<T> {
    static final int STRING_FIELD_TYPE = 0;
    static final int INT_FIELD_TYPE = 1;

    public static final MediaProperty<String> AUTHOR = new MediaProperty<>(0, STRING_FIELD_TYPE);
    public static final MediaProperty<String> COMMENT = new MediaProperty<>(1, STRING_FIELD_TYPE);
    public static final MediaProperty<String> COPYRIGHT = new MediaProperty<>(2, STRING_FIELD_TYPE);
    public static final MediaProperty<String> KEYWORDS = new MediaProperty<>(3, STRING_FIELD_TYPE);
    public static final MediaProperty<String> LANGUAGE = new MediaProperty<>(4, STRING_FIELD_TYPE);
    public static final MediaProperty<String> SUB_TITLE = new MediaProperty<>(5, STRING_FIELD_TYPE);
    public static final MediaProperty<Integer> YEAR = new MediaProperty<>(6, INT_FIELD_TYPE);
    public static final MediaProperty<String> TITLE = new MediaProperty<>(7, STRING_FIELD_TYPE);

    final int propOrdinal;
    final int type;

    private MediaProperty(int propOrdinal, int type) {
        this.propOrdinal = propOrdinal;
        this.type = type;
    }
}