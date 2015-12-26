/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.alpenblock.bungeeperms.uuid;

import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author alex
 */
public class UUIDFetcherTest
{
    
    public UUIDFetcherTest()
    {
    }

    @Test
    public void testGetUUIDs()
    {
        UUID uuid=UUID.fromString("10976043-a820-4412-bfcd-bc7ec3e59998");
        
        UUID fetcheduuid = UUIDFetcher.getUUIDFromMojang("wea_ondara", null);
        
        assertTrue(uuid.equals(fetcheduuid));
    }

    @Test
    public void testGetPlayerNames()
    {
        UUID uuid=UUID.fromString("10976043-a820-4412-bfcd-bc7ec3e59998");
        
        String name = UUIDFetcher.getPlayerNameFromMojang(uuid);
        
//        assertTrue(name.equals("wea_ondara"));
    }
    
}
