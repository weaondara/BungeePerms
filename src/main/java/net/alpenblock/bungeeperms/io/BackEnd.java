/* 
 * Copyright (C) 2020 wea_ondara
 *
 * BungeePerms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeePerms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.alpenblock.bungeeperms.io;

import java.util.List;
import java.util.UUID;

import net.alpenblock.bungeeperms.BPPermission;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;

public interface BackEnd
{

    public BackEndType getType();

    public void load();

    public List<Group> loadGroups();

    public List<User> loadUsers();

    public Group loadGroup(String group);

    public User loadUser(String user);

    public User loadUser(UUID user);

    public int loadVersion();

    public void saveVersion(int version, boolean savetodisk);

    public boolean isUserInDatabase(User user);

    public List<String> getRegisteredUsers();

    public List<String> getGroupUsers(Group group);

    public void reloadGroup(Group group);

    public void reloadUser(User user);

    public void saveUser(User user, boolean savetodisk);

    public void saveGroup(Group group, boolean savetodisk);

    public void deleteUser(User user);

    public void deleteGroup(Group group);

    public void saveUserGroups(User user, String server, String world);

    public void saveUserTimedGroups(User user, String server, String world);

    public void saveUserPerms(User user, String server, String world);

    public void saveUserTimedPerms(User user, String server, String world);

    public void saveUserDisplay(User user, String server, String world);

    public void saveUserPrefix(User user, String server, String world);

    public void saveUserSuffix(User user, String server, String world);

    public void saveGroupPerms(Group group, String server, String world);

    public void saveGroupTimedPerms(Group group, String server, String world);

    public void saveGroupInheritances(Group group, String server, String world);

    public void saveGroupTimedInheritances(Group group, String server, String world);

    public void saveGroupRank(Group group);

    public void saveGroupWeight(Group group);

    public void saveGroupLadder(Group group);

    public void saveGroupDefault(Group group);

    public void saveGroupDisplay(Group group, String server, String world);

    public void saveGroupPrefix(Group group, String server, String world);

    public void saveGroupSuffix(Group group, String server, String world);

    public int cleanup(List<Group> groups, List<User> users, int version);

    public void format(List<Group> groups, List<User> users, int version);

    public void clearDatabase();
    
    public void removeGroupReferences(Group g);

    public List<BPPermission> getUsersWithPerm(String perm);

    public List<BPPermission> getGroupsWithPerm(String perm);

}
