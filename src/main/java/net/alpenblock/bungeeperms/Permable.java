package net.alpenblock.bungeeperms;

import java.util.List;

public interface Permable
{

    public String getPrefix();

    public void setPrefix(String prefix);

    public String getSuffix();

    public void setSuffix(String suffix);

    public String getDisplay();

    public void setDisplay(String display);

    public List<String> getGroupsString();

    public void setGroups(List<String> groups);

    public List<TimedValue<String>> getTimedGroupsString();

    public void setTimedGroups(List<TimedValue<String>> groups);

    public List<String> getPerms();

    public void setPerms(List<String> perms);

    public List<TimedValue<String>> getTimedPerms();

    public void setTimedPerms(List<TimedValue<String>> perms);

    public boolean hasTimedPermSet(String perm);
}
