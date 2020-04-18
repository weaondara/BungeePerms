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
package net.alpenblock.bungeeperms.platform;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import net.alpenblock.bungeeperms.Statics;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlatformPluginTest
{

    public PlatformPluginTest()
    {
    }

    @BeforeClass
    public static void setUpClass()
    {
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of getBuild method, of class PlatformPlugin.
     */
    @Test
    public void testGetBuild()
    {
        System.out.println("getBuild");
        PlatformPlugin instance = new PlatformPluginImpl();
        Integer expResult = 70;
        Integer result = instance.getBuild();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    public class PlatformPluginImpl implements PlatformPlugin
    {

        public String getPluginName()
        {
            return "";
        }

        public String getVersion()
        {
            return "3.0 dev #70";
        }

        public String getAuthor()
        {
            return "";
        }

        public String getPluginFolderPath()
        {
            return "";
        }

        public File getPluginFolder()
        {
            return null;
        }

        public Sender getPlayer(String name)
        {
            return null;
        }

        public Sender getPlayer(UUID uuid)
        {
            return null;
        }

        public Sender getConsole()
        {
            return null;
        }

        public List<Sender> getPlayers()
        {
            return null;
        }

        public Logger getLogger()
        {
            return null;
        }

        public PlatformType getPlatformType()
        {
            return null;
        }

        public boolean isChatApiPresent()
        {
            return false;
        }

        public MessageEncoder newMessageEncoder()
        {
            return null;
        }

        public int registerRepeatingTask(Runnable r, long delay, long interval)
        {
            return 0;
        }

        @Override
        public int runTaskLater(Runnable r, long delay)
        {
            return 0;
        }

        @Override
        public int runTaskLaterAsync(Runnable r, long delay)
        {
            return 0;
        }

        public void cancelTask(int id)
        {
        }

        public Integer getBuild()
        {
            return Statics.getBuild(this);
        }
    }
}
