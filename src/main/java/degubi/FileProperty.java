package degubi;

public enum FileProperty {
    TITLE(0, FileMeta.STRING_FIELD_TYPE),
    SUB_TITLE(1, FileMeta.STRING_FIELD_TYPE),
    COMMENT(2, FileMeta.STRING_FIELD_TYPE),
    AUTHOR(3, FileMeta.STRING_FIELD_TYPE);

    final int propOrdinal;
    final int type;

    FileProperty(int propOrdinal, int type) {
        this.propOrdinal = propOrdinal;
        this.type = type;
    }
}