package net.alpenblock.bungeeperms;

import java.util.List;
import net.alpenblock.bungeeperms.platform.Sender;

public interface PermissionsPreProcessor
{

    public List<String> process(List<String> perms, Sender s);
}
