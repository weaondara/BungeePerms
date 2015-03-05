/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.alpenblock.bungeeperms.platform.bukkit.bridge;

import org.bukkit.event.Listener;

public interface Bridge extends Listener
{

    public void enable();

    public void disable();
}
