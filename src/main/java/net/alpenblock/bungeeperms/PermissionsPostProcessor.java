package net.alpenblock.bungeeperms;

import net.alpenblock.bungeeperms.platform.Sender;

public interface PermissionsPostProcessor
{

    public Boolean process(String perm, Boolean result, Sender s);
}
