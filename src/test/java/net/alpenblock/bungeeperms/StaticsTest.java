/* 
 * Copyright (C) 2020 wea_ondara
 *
 * BungeePerms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeePerms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
