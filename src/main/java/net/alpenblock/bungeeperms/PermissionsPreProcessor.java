package net.alpenblock.bungeeperms;

import java.util.List;
import net.alpenblock.bungeeperms.platform.Sender;

public interface PermissionsPreProcessor
{

    public List<BPPermission> process(List<BPPermission> perms, Sender s);
}
