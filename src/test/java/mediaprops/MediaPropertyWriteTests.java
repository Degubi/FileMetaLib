package mediaprops;

import static mediaprops.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import mediaprops.exception.*;
import org.junit.jupiter.api.*;

public class MediaPropertyWriteTests {

    @AfterEach
    public void postTest() {
        MediaFileUtils.clearProperty(mp4Path, MediaProperty.TITLE);
        MediaFileUtils.clearProperty(mp4Path, MediaProperty.COMMENT);
        MediaFileUtils.clearProperty(mp4Path, MediaProperty.YEAR);
    }


    @Test
    public void testWriteToFileInUse() throws IOException {
        try(var hihi = Files.newOutputStream(mp4Path, StandardOpenOption.WRITE)) {
            var exception = assertThrows(MediaFileIOException.class, () -> MediaFileUtils.writeProperty(mp4Path, MediaProperty.YEAR, 200));

            assertEquals("An IO error happened with the file: '" + executionFolder + "\\src\\test\\resources\\test.mp4', description: The process cannot access the file because it is being used by another process.\r\n", exception.getMessage());
        }
    }

    @Test
    public void testWriteToNonExistentFile() {
        var exception = assertThrows(FileDoesNotExistException.class, () -> MediaFileUtils.writeProperty(nonExistingPath, MediaProperty.YEAR, 200));

        assertEquals("File doesn't exist: '" + executionFolder + "\\src\\test\\resources\\test.lol'", exception.getMessage());
    }

    @Test
    public void testWriteToNonMediaFile() {
        var exception = assertThrows(NonMediaFileException.class, () -> MediaFileUtils.writeProperty(txtPath, MediaProperty.YEAR, 200));

        assertEquals("The given file is not a media file: '" + executionFolder + "\\src\\test\\resources\\test.txt'\r\n", exception.getMessage());
    }

    @Test
    public void testWriteWithInvalidIntegerValue() {
        var exception = assertThrows(IllegalArgumentException.class, () -> MediaFileUtils.writeProperty(mp4Path, MediaProperty.YEAR, -3));

        assertEquals("Can't set property 'Year' to a negative value! Tried to passed in value: '-3'", exception.getMessage());
    }

    @Test
    public void testWriteAndReadStringProperty() {
        var value = "mamaaa";

        MediaFileUtils.writeProperty(mp4Path, MediaProperty.TITLE, value);
        assertTrue(MediaFileUtils.hasProperty(mp4Path, MediaProperty.TITLE));
        assertEquals(Optional.of(value), MediaFileUtils.readProperty(mp4Path, MediaProperty.TITLE));
    }

    @Test
    public void testWriteAndReadIntegerProperty() {
        var value = 5000;

        MediaFileUtils.writeProperty(mp4Path, MediaProperty.YEAR, value);
        assertTrue(MediaFileUtils.hasProperty(mp4Path, MediaProperty.YEAR));
        assertEquals(Optional.of(value), MediaFileUtils.readProperty(mp4Path, MediaProperty.YEAR));
    }

    @Test
    public void testClearProperty() {
        assertFalse(MediaFileUtils.hasProperty(mp4Path, MediaProperty.COMMENT));

        MediaFileUtils.writeProperty(mp4Path, MediaProperty.COMMENT, "yo");
        assertTrue(MediaFileUtils.hasProperty(mp4Path, MediaProperty.COMMENT));

        MediaFileUtils.clearProperty(mp4Path, MediaProperty.COMMENT);
        assertFalse(MediaFileUtils.hasProperty(mp4Path, MediaProperty.COMMENT));

        MediaFileUtils.writeProperty(mp4Path, MediaProperty.COMMENT, "yo");
        MediaFileUtils.writeProperty(mp4Path, MediaProperty.YEAR, 5000);
        assertTrue(MediaFileUtils.hasProperty(mp4Path, MediaProperty.COMMENT));
        assertTrue(MediaFileUtils.hasProperty(mp4Path, MediaProperty.YEAR));
    }

    @Test
    public void testCopyProperty() throws IOException {
        var testFileCopyPath = Path.of(mp4Path.toString().replace("test.mp4", "testCopy.mp4"));
        Files.copy(mp4Path, testFileCopyPath);

        MediaFileUtils.writeProperty(mp4Path, MediaProperty.COMMENT, "yo");
        assertTrue(MediaFileUtils.hasProperty(mp4Path, MediaProperty.COMMENT));
        assertFalse(MediaFileUtils.hasProperty(testFileCopyPath, MediaProperty.COMMENT));

        MediaFileUtils.copyProperty(mp4Path, testFileCopyPath, MediaProperty.COMMENT);

        Files.delete(testFileCopyPath);
    }
}