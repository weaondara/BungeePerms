package net.alpenblock.bungeeperms.platform;

import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;

public interface NetworkNotifier
{

    public void deleteUser(User u, String origin);

    public void deleteGroup(Group g, String origin);

    public void reloadUser(User u, String origin);

    public void reloadGroup(Group g, String origin);

    public void reloadAll(String origin);
}
