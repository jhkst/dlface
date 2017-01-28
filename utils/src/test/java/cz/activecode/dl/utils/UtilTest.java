package cz.activecode.dl.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilTest {

    @Test
    public void sizeProperty() {
        assertEquals(10, Util.parseSize("10", 0, 0));
        assertEquals(10, Util.parseSize("10B", 0, 0));
        assertEquals(1000, Util.parseSize("1000", 0, 0));
        assertEquals(1000, Util.parseSize("1K", 0, 0));
        assertEquals(1000, Util.parseSize("1KB", 0, 0));
        assertEquals(1024, Util.parseSize("1Ki", 0, 0));
        assertEquals(1024, Util.parseSize("1KiB", 0, 0));

        assertEquals(1000000, Util.parseSize("1000000", 0, 0));
        assertEquals(1000000, Util.parseSize("1M", 0, 0));
        assertEquals(1000000, Util.parseSize("1MB", 0, 0));
        assertEquals(1024*1024, Util.parseSize("1Mi", 0, 0));
        assertEquals(1024*1024, Util.parseSize("1MiB", 0, 0));
    }
}
