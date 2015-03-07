package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class Server
{

    private String server;
    private List<String> perms;
    private Map<String, World> worlds;
    private String display;
    private String prefix;
    private String suffix;

    public World getWorld(String name)
    {
        if(name == null)
        {
            return null;
        }
        World w = worlds.get(name.toLowerCase());
        if (w == null)
        {
            w = new World(name.toLowerCase(), new ArrayList<String>(), "", "", "");
            worlds.put(name.toLowerCase(), w);
        }

        return w;
    }
}
