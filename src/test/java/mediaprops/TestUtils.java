package mediaprops;

import java.nio.file.*;
import java.util.*;

public final class TestUtils {
    public static final Path mp4Path = Path.of("src/test/resources/test.mp4");
    public static final Path txtPath = Path.of("src/test/resources/test.txt");
    public static final Path nonExistingPath = Path.of("src/test/resources/test.lol");
    public static final Random rand = new Random();
    public static final String executionFolder = Path.of("").toAbsolutePath().toString();

    private TestUtils() {}
}