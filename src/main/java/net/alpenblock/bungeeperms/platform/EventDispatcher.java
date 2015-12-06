package net.alpenblock.bungeeperms.platform;

import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;

public interface EventDispatcher 
{
    public void dispatchReloadedEvent();
    public void dispatchGroupChangeEvent(Group g);
    public void dispatchUserChangeEvent(User u);
}
