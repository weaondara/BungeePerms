package net.alpenblock.bungeeperms;

public class CleanupTask implements Runnable
{
    @Override
    public void run()
    {
        BungeePerms bp = BungeePerms.getInstance();
        PermissionsManager pm = bp.getPermissionsManager();
        
        long threshold = bp.getConfig().getCleanupThreshold() * 1000;
        
        pm.getUserlock().writeLock().lock();
        
        for(User u : pm.getUsers())
        {
            if((bp.getConfig().isUseUUIDs() ? bp.getPlugin().getPlayer(u.getUUID()) : bp.getPlugin().getPlayer(u.getName())) != null)
            {
                continue;
            }
            if(u.getLastAccess() + threshold < System.currentTimeMillis())
            {
                pm.removeUserFromCache(u);
            }
        }
        
        pm.getUserlock().writeLock().unlock();
    }
}
