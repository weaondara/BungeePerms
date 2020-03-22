package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.List;
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
public class World implements Permable
{

    private String world;
    private List<String> groups;
    private List<TimedValue<String>> timedGroups;
    private List<String> perms;
    private List<TimedValue<String>> timedPerms;
    private String display;
    private String prefix;
    private String suffix;

    @Override
    public List<String> getGroupsString()
    {
        return groups;
    }

    @Override
    public List<TimedValue<String>> getTimedGroupsString()
    {
        return timedGroups;
    }

    @Override
    public boolean hasTimedPermSet(String perm)
    {
        perm = Statics.toLower(perm);

        for (TimedValue<String> t : timedPerms)
            if (t.getValue().equalsIgnoreCase(perm))
                return true;
        return false;
    }

    public List<Group> getGroups()
    {
        List<Group> ret = new ArrayList<>();
        for (String name : groups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(name);
            if (g != null)
                ret.add(g);
        }

        return ret;
    }

    public List<TimedValue<Group>> getTimedGroups()
    {
        List<TimedValue<Group>> ret = new ArrayList<>();
        for (TimedValue<String> name : timedGroups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(name.getValue());
            if (g != null)
                ret.add(new TimedValue(g, name.getStart(), name.getDuration()));
        }

        return ret;
    }
}
