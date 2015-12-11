package net.alpenblock.bungeeperms.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Server;
import net.alpenblock.bungeeperms.io.mysql2.EntityType;
import net.alpenblock.bungeeperms.io.mysql2.MysqlPermEntity;
import net.alpenblock.bungeeperms.io.mysql2.ValueEntry;
import static org.junit.Assert.*;
import org.junit.Test;

public class MySQL2BackEndTest
{

    public MySQL2BackEndTest()
    {
    }

    @Test
    public void testMapServerWorlds()
    {
        Group g = new Group("", new ArrayList<String>(), new ArrayList<String>(), new HashMap<String, Server>(), 0, 0, null, false, null, null, null);
        Map<String, List<ValueEntry>> mpedata = new HashMap<>();
        List<ValueEntry> mpeperms = new ArrayList();
        List<ValueEntry> mpesuffix = new ArrayList();
        List<ValueEntry> mpeprefix = new ArrayList();
        List<ValueEntry> mpedisplay = new ArrayList();
        mpedata.put("permissions", mpeperms);
        mpedata.put("suffix", mpesuffix);
        mpedata.put("prefix", mpeprefix);
        mpedata.put("display", mpedisplay);
        
        //mpe data
        mpeperms.add(new ValueEntry("perm1",null,null));
        mpeperms.add(new ValueEntry("perm2",null,null));
        mpeperms.add(new ValueEntry("perm3","server1",null));
        mpeperms.add(new ValueEntry("perm4","server1","world1"));
        mpeperms.add(new ValueEntry("perm5",null,"world1"));
        mpeperms.add(new ValueEntry("perm6","server2","world1"));
        mpeperms.add(new ValueEntry("perm7","server2",null));
        mpesuffix.add(new ValueEntry("suffix1",null,null));
        mpesuffix.add(new ValueEntry("suffix2","server2",null));
        mpesuffix.add(new ValueEntry("suffix3","server2","world1"));
        mpesuffix.add(new ValueEntry("suffix4","server2","world1"));
        mpesuffix.add(new ValueEntry("suffix5","server3",null));
        mpeprefix.add(new ValueEntry("prefix1",null,null));
        mpeprefix.add(new ValueEntry("prefix2","server3",null));
        mpeprefix.add(new ValueEntry("prefix3","server3","world1"));
        mpeprefix.add(new ValueEntry("prefix5","server4",null));
        mpedisplay.add(new ValueEntry("display1",null,null));
        mpedisplay.add(new ValueEntry("display2","server5",null));
        mpedisplay.add(new ValueEntry("display3","server5","world1"));
        mpedisplay.add(new ValueEntry("display5","server6",null));
        
        MysqlPermEntity mpe = new MysqlPermEntity("", EntityType.Group, mpedata);
        MySQL2BackEnd.loadServerWorlds(mpe, g);
        
        assertNotNull(g.getServers());
        
        //servers
        assertNotNull(g.getServers().get("server1"));
        assertNotNull(g.getServers().get("server2"));
        assertNotNull(g.getServers().get("server3"));
        assertNotNull(g.getServers().get("server4"));
        assertNotNull(g.getServers().get("server5"));
        assertNotNull(g.getServers().get("server6"));
        
        //world
        assertNotNull(g.getServers().get("server1").getWorld("world1"));
        assertNotNull(g.getServers().get("server2").getWorld("world1"));
        assertNotNull(g.getServers().get("server2").getWorld("world2"));
        assertNotNull(g.getServers().get("server3").getWorld("world1"));
        assertNotNull(g.getServers().get("server5").getWorld("world1"));
        
        //world count on servers
        assertEquals(1, g.getServers().get("server1").getWorlds().size());
        assertEquals(2, g.getServers().get("server2").getWorlds().size());
        assertEquals(1, g.getServers().get("server3").getWorlds().size());
        assertEquals(0, g.getServers().get("server4").getWorlds().size());
        assertEquals(1, g.getServers().get("server5").getWorlds().size());
        assertEquals(0, g.getServers().get("server6").getWorlds().size());
        
        //perms
        assertEquals(new ArrayList(Arrays.asList("perm1", "perm2", "perm5")), g.getPerms());
        assertEquals(new ArrayList(Arrays.asList("perm3")), g.getServer("server1").getPerms());
        assertEquals(new ArrayList(Arrays.asList("perm7")), g.getServer("server2").getPerms());
        assertEquals(new ArrayList(Arrays.asList("perm4")), g.getServer("server1").getWorld("world1").getPerms());
        assertEquals(new ArrayList(Arrays.asList("perm6")), g.getServer("server2").getWorld("world1").getPerms());
        
        //suffix
        assertEquals("suffix1", g.getSuffix());
        assertEquals("suffix2", g.getServer("server2").getSuffix());
        assertEquals("suffix5", g.getServer("server3").getSuffix());
        assertEquals("suffix3", g.getServer("server2").getWorld("world1").getSuffix());
        
        //prefix
        assertEquals("prefix1", g.getPrefix());
        assertEquals("prefix2", g.getServer("server3").getPrefix());
        assertEquals("prefix5", g.getServer("server4").getPrefix());
        assertEquals("prefix3", g.getServer("server3").getWorld("world1").getPrefix());
        
        //display
        assertEquals("display1", g.getDisplay());
        assertEquals("display2", g.getServer("server5").getDisplay());
        assertEquals("display5", g.getServer("server6").getDisplay());
        assertEquals("display3", g.getServer("server5").getWorld("world1").getDisplay());
    }
}
