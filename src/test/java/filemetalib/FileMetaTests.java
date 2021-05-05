package filemetalib;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;

public class FileMetaTests {
    private static final Path mp4Path = Path.of("src/test/resources/test.mp4");
    private static final Random rand = new Random();

    @AfterAll
    public static void postTest() {
        FileMeta.writeProperty(mp4Path, FileProperty.TITLE, "");
    }

    @Test
    public void testTitle() {
        var value = generateString();

        FileMeta.writeProperty(mp4Path, FileProperty.TITLE, value);
        assertEquals(value, FileMeta.readProperty(mp4Path, FileProperty.TITLE));
    }


    private static String generateString() {
        var val = rand.nextInt(5);

        return val == 0 ? "asd" :
               val == 1 ? "kek" :
               val == 2 ? "dadada" :
               val == 3 ? "blablabla" :
                          "sadasd";
    }
}