package mediaprops;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Random;
import mediaprops.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.*;

@TestMethodOrder(OrderAnnotation.class)
public class MediaPropertyTests {
    private static final Path mp4Path = Path.of("src/test/resources/test.mp4");
    private static final Path txtPath = Path.of("src/test/resources/test.txt");
    private static final Random rand = new Random();
    private static final String executionFolder = Path.of("").toAbsolutePath().toString();

    @AfterAll
    public static void postTest() {
        MediaFileUtils.clearProperty(mp4Path, MediaProperty.TITLE);
        MediaFileUtils.clearProperty(mp4Path, MediaProperty.COMMENT);
        MediaFileUtils.clearProperty(mp4Path, MediaProperty.YEAR);
    }


    @Test
    @Order(0)
    public void testWriteFileInUse() {
        useFileAlreadInUsage(() -> {
            var exception = assertThrows(MediaFileIOException.class, () -> MediaFileUtils.writeProperty(mp4Path, MediaProperty.YEAR, 200));

            assertEquals("An IO error happened with the file: '" + executionFolder + "\\src\\test\\resources\\test.mp4', description: The process cannot access the file because it is being used by another process.\r\n", exception.getMessage());
        });
    }

    @Test
    @Order(0)
    public void testReadFileInUse() {
        useFileAlreadInUsage(() -> {
            var exception = assertThrows(MediaFileIOException.class, () -> MediaFileUtils.readProperty(mp4Path, MediaProperty.COMMENT));

            assertEquals("An IO error happened with the file: '" + executionFolder + "\\src\\test\\resources\\test.mp4', description: The process cannot access the file because it is being used by another process.\r\n", exception.getMessage());
        });
    }

    @Test
    @Order(0)
    public void testWriteWithInvalidIntegerValue() {
        var exception = assertThrows(IllegalArgumentException.class, () -> MediaFileUtils.writeProperty(mp4Path, MediaProperty.YEAR, -3));

        assertEquals("Can't set property 'Year' to a negative value! Tried to passed in value: '-3'", exception.getMessage());
    }

    @Test
    @Order(0)
    public void testReadFromNonMediaFile() {
        var exception = assertThrows(NonMediaFileException.class, () -> MediaFileUtils.readProperty(txtPath, MediaProperty.COMMENT));

        assertEquals("The given file is not a media file: '" + executionFolder + "\\src\\test\\resources\\test.txt'\r\n", exception.getMessage());
    }

    @Test
    @Order(0)
    public void testWriteToNonMediaFile() {
        var exception = assertThrows(NonMediaFileException.class, () -> MediaFileUtils.writeProperty(txtPath, MediaProperty.YEAR, 200));

        assertEquals("The given file is not a media file: '" + executionFolder + "\\src\\test\\resources\\test.txt'\r\n", exception.getMessage());
    }

    @Test
    @Order(0)
    public void testValidMediaFile() {
        assertTrue(MediaFileUtils.isMediaFile(mp4Path));
    }

    @Test
    @Order(0)
    public void testInvalidMediaFile() {
        assertFalse(MediaFileUtils.isMediaFile(txtPath));
    }


    @Test
    @Order(1)
    public void testDefaultProperties() {
        var expected = MediaPropertyMap.empty();
        expected.put(MediaProperty.AUDIO_SAMPLE_SIZE, 16);
        expected.put(MediaProperty.AUDIO_CHANNEL_COUNT, 2);
        expected.put(MediaProperty.AUDIO_ENCODING_BITRATE, 8288);
        expected.put(MediaProperty.DURATION, 4004000);
        expected.put(MediaProperty.AUDIO_SAMPLE_RATE, 48000);
        expected.put(MediaProperty.AUDIO_FORMAT, "{00001610-0000-0010-8000-00AA00389B71}");

        assertEquals(expected, MediaFileUtils.readAllProperties(mp4Path));
    }

    @Test
    @Order(2)
    public void testWriteAndReadStringProperty() {
        var value = generateString();

        MediaFileUtils.writeProperty(mp4Path, MediaProperty.TITLE, value);
        assertEquals(value, MediaFileUtils.readProperty(mp4Path, MediaProperty.TITLE));
        assertTrue(MediaFileUtils.hasProperty(mp4Path, MediaProperty.TITLE));
    }

    @Test
    @Order(2)
    public void testWriteAndReadIntegerProperty() {
        var value = generateInt();

        MediaFileUtils.writeProperty(mp4Path, MediaProperty.YEAR, value);
        assertEquals(value, MediaFileUtils.readProperty(mp4Path, MediaProperty.YEAR));
        assertTrue(MediaFileUtils.hasProperty(mp4Path, MediaProperty.YEAR));
    }


    @Test
    @Order(3)
    public void testDoesntHaveProperty() {
        assertFalse(MediaFileUtils.hasProperty(mp4Path, MediaProperty.LANGUAGE));
    }

    @Test
    @Order(3)
    public void testNonExistingPropertyRead() {
        var exception = assertThrows(IllegalArgumentException.class, () -> MediaFileUtils.readProperty(mp4Path, MediaProperty.COPYRIGHT));

        assertEquals("Property 'Copyright' doesn't exist on file: '" + executionFolder + "\\src\\test\\resources\\test.mp4'", exception.getMessage());
        assertEquals(Optional.empty(), MediaFileUtils.readOptionalProperty(mp4Path, MediaProperty.KEYWORDS));
    }


    @Test
    @Order(4)
    public void testClearProperty() {
        assertFalse(MediaFileUtils.hasProperty(mp4Path, MediaProperty.COMMENT));

        MediaFileUtils.writeProperty(mp4Path, MediaProperty.COMMENT, "yo");

        assertTrue(MediaFileUtils.hasProperty(mp4Path, MediaProperty.COMMENT));

        MediaFileUtils.clearProperty(mp4Path, MediaProperty.COMMENT);

        assertFalse(MediaFileUtils.hasProperty(mp4Path, MediaProperty.COMMENT));
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

    private static void useFileAlreadInUsage(Runnable action) {
        try(var hihi = Files.newOutputStream(mp4Path, StandardOpenOption.WRITE)) {
            action.run();
        } catch (IOException e) {}
    }
}