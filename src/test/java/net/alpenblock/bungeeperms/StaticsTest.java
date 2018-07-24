package net.alpenblock.bungeeperms;

import org.junit.Test;
import static org.junit.Assert.*;

public class StaticsTest
{

    public StaticsTest()
    {
    }

    @Test
    public void testParseCommand()
    {
        assertArrayEquals(Statics.array("a", "b", "c", "d", "e"), Statics.parseCommand("a b c d e"));
        assertArrayEquals(Statics.array("a", "b", "c", "d", "e"), Statics.parseCommand("a b c d e "));
        assertArrayEquals(Statics.array("a", "bcd", "e"), Statics.parseCommand("a bcd e "));
        assertArrayEquals(Statics.array("a", "b cd", "e"), Statics.parseCommand("a \"b cd\" e "));
    }
}
