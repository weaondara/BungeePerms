/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.alpenblock.bungeeperms.io.migrate;

import java.util.List;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;

/**
 *
 * @author Alex
 */
public interface Migrator
{
    public void migrate(final List<Group> groups, final List<User> users, final int permsversion);
}
