package net.alpenblock.bungeeperms;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TabListener implements Listener
{

    @EventHandler
    public void onTabcomplete(TabCompleteEvent e)
    {
        if (e.getSuggestions().isEmpty())
        {
            for (ProxiedPlayer pp : BungeeCord.getInstance().getPlayers())
            {
                if(pp.getName().toLowerCase().startsWith(e.getCursor().toLowerCase()))
                {
                    e.getSuggestions().add(pp.getName());
                }
            }
        }
    }
}
