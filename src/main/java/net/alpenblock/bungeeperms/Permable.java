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
    public List<String> getPerms();
    public void setPerms(List<String> perms);
}
