package net.alpenblock.bungeeperms.uuid;

import com.google.gson.Gson;
import com.mojang.api.profiles.Profile;
import com.mojang.api.profiles.ProfileCriteria;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.alpenblock.bungeeperms.Statics;

public class UUIDFetcher
{
    private HttpProfileRepository repo;
    
    @Getter
    private final List<String> tofetch;
    @Getter
    private final Map<String,UUID> UUIDs;
    @Getter
    private final Map<UUID,String> playerNames;
    
    private int cooldown;

    public UUIDFetcher(List<String> tofetch, int cooldown)
    {
        repo=new HttpProfileRepository();
        
        this.tofetch = tofetch;
        UUIDs=new HashMap<>();
        playerNames=new HashMap<>();
        this.cooldown=cooldown;
    }

    @SneakyThrows
    public void fetchUUIDs()
    {
        for(String player:tofetch)
        {
            UUID uuid = getUUIDFromMojang(player, repo);
            if(uuid!=null)
            {
                UUIDs.put(player, uuid);
            }
            Thread.sleep(cooldown);
        }
    }
    @SneakyThrows
    public void fetchPlayerNames()
    {
        for(String suuid:tofetch)
        {
            UUID uuid=UUID.fromString(suuid);
            String playername = getPlayerNameFromMojang(uuid);
            if(playername!=null)
            {
                playerNames.put(uuid, playername);
            }
            Thread.sleep(cooldown);
        }
    }
    
    public static UUID getUUIDFromMojang(String player, HttpProfileRepository repo)
    {
        if(repo==null)
        {
            repo=new HttpProfileRepository();
        }
        
        Profile[] profiles = repo.findProfilesByCriteria(new ProfileCriteria(player, "minecraft"));
        for(Profile p:profiles)
        {
            UUID uuid=Statics.parseUUID(p.getId());
            if(uuid!=null)
            {
                return uuid;
            }
        }
        
        return null;
    }
    public static String getPlayerNameFromMojang(UUID uuid)
    {
        String suuid=uuid.toString().toLowerCase().replaceAll("-", "");
        
        try
        {
            URL url=new URL("https://sessionserver.mojang.com/session/minecraft/profile/"+suuid);
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            byte[] buffer=new byte[con.getContentLength()];
            in.read(buffer);
            String res=new String(buffer);
            Gson gson=new Gson();
            PlayerNameFetchResult result = gson.fromJson(res, PlayerNameFetchResult.class);
            return result.getName();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    @Getter @Setter
    private class PlayerNameFetchResult
    {
        private String id;
        private String name;
    }
}
