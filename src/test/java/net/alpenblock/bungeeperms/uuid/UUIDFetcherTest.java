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
        //these make problems ... blame mojang
//        UUID uuid=UUID.fromString("10976043-a820-4412-bfcd-bc7ec3e59998");
//        
//        UUID fetcheduuid = UUIDFetcher.getUUIDFromMojang("wea_ondara", null);
//        
//        assertTrue(uuid.equals(fetcheduuid));
    }

    @Test
    public void testGetPlayerNames()
    {
        //these make problems ... blame mojang
//        UUID uuid=UUID.fromString("10976043-a820-4412-bfcd-bc7ec3e59998");
//        
//        String name = UUIDFetcher.getPlayerNameFromMojang(uuid);
//        
//        assertTrue(name.equals("wea_ondara"));
    }
    
}
