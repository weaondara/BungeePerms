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
public class Server implements Permable
{

    private String server;
    private List<String> perms;
    private List<TimedValue<String>> timedPerms;
    private Map<String, World> worlds;
    private String display;
    private String prefix;
    private String suffix;

    @Override
    public boolean hasTimedPermSet(String perm)
    {
        perm = Statics.toLower(perm);

        for (TimedValue<String> t : timedPerms)
            if (t.getValue().equalsIgnoreCase(perm))
                return true;
        return false;
    }

    public World getWorld(String name)
    {
        if (name == null)
            return null;
        name = Statics.toLower(name);

        World w = worlds.get(name);
        if (w == null)
        {
            w = new World(name, new ArrayList(), new ArrayList(), null, null, null);
            worlds.put(name, w);
        }

        return w;
    }
}
