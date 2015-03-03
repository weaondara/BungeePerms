package net.alpenblock.bungeeperms.platform;

import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;

public interface NetworkNotifier
{

    public void deleteUser(User u);

    public void deleteGroup(Group g);

    public void reloadUser(User u);

    public void reloadGroup(Group g);

    public void reloadAll();
}
