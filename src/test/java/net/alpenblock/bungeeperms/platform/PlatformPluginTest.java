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
