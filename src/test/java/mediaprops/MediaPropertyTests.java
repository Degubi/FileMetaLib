package mediaprops;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;

public class MediaPropertyTests {
    private static final Path mp4Path = Path.of("src/test/resources/test.mp4");
    private static final Random rand = new Random();

    @AfterAll
    public static void postTest() {
        MediaPropertyUtils.clearProperty(mp4Path, MediaProperty.TITLE);
        MediaPropertyUtils.clearProperty(mp4Path, MediaProperty.COMMENT);
        MediaPropertyUtils.clearProperty(mp4Path, MediaProperty.YEAR);
    }

    @Test
    @Order(0)
    public void testStringProperty() {
        var value = generateString();

        MediaPropertyUtils.writeProperty(mp4Path, MediaProperty.TITLE, value);
        assertEquals(value, MediaPropertyUtils.readProperty(mp4Path, MediaProperty.TITLE));
    }

    @Test
    @Order(0)
    public void testIntegerProperty() {
        var value = generateInt();

        MediaPropertyUtils.writeProperty(mp4Path, MediaProperty.YEAR, value);
        assertEquals(value, MediaPropertyUtils.readProperty(mp4Path, MediaProperty.YEAR));
    }

    @Test
    @Order(1)
    public void testHasProperty() {
        assertFalse(MediaPropertyUtils.hasProperty(mp4Path, MediaProperty.LANGUAGE));
        assertTrue(MediaPropertyUtils.hasProperty(mp4Path, MediaProperty.YEAR));
        assertTrue(MediaPropertyUtils.hasProperty(mp4Path, MediaProperty.TITLE));
    }

    @Test
    @Order(1)
    public void testNonExistingProperty() {
        assertThrows(IllegalArgumentException.class, () -> MediaPropertyUtils.readProperty(mp4Path, MediaProperty.COPYRIGHT));
        assertEquals(Optional.empty(), MediaPropertyUtils.readOptionalProperty(mp4Path, MediaProperty.KEYWORDS));
    }

    @Test
    @Order(2)
    public void testClearProperty() {
        assertFalse(MediaPropertyUtils.hasProperty(mp4Path, MediaProperty.COMMENT));

        MediaPropertyUtils.writeProperty(mp4Path, MediaProperty.COMMENT, "yo");

        assertTrue(MediaPropertyUtils.hasProperty(mp4Path, MediaProperty.COMMENT));

        MediaPropertyUtils.clearProperty(mp4Path, MediaProperty.COMMENT);

        assertFalse(MediaPropertyUtils.hasProperty(mp4Path, MediaProperty.COMMENT));
    }


    private static String generateString() {
        var val = rand.nextInt(5);

        return val == 0 ? "asd" :
               val == 1 ? "kek" :
               val == 2 ? "dadada" :
               val == 3 ? "blablabla" :
                          "sadasd";
    }

    private static int generateInt() {
        var val = rand.nextInt(5);

        return val == 0 ? 420 :
               val == 1 ? 1337 :
               val == 2 ? 2021 :
               val == 3 ? 1234 :
                          5678;
    }
}