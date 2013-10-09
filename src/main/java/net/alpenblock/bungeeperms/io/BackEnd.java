/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.alpenblock.bungeeperms.io;

import java.util.List;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;

/**
 *
 * @author Alex
 */
public interface BackEnd
{
    public BackEndType getType();
    
    public void load();
    public List<Group> loadGroups();
    public List<User> loadUsers();
    public User loadUser(String user);
    public int loadVersion();
    public void saveVersion(int version, boolean savetodisk);
    
    public boolean isUserInDatabase(User user);
    public List<String> getRegisteredUsers();
    
    public void saveUser(User user, boolean savetodisk);
    public void saveGroup(Group group, boolean savetodisk);
    public void deleteUser(User user);
    public void deleteGroup(Group group);

    public void saveUserGroups(User user);
    public void saveUserPerms(User user);
    public void saveUserPerServerPerms(User user, String server);
    public void saveUserPerServerWorldPerms(User user, String server, String world);

    public void saveGroupPerms(Group group);
    public void saveGroupPerServerPerms(Group group, String server);
    public void saveGroupPerServerWorldPerms(Group group, String server, String world);
    public void saveGroupInheritances(Group group);
    public void saveGroupRank(Group group);
    public void saveGroupLadder(Group group);
    public void saveGroupDefault(Group group);
    public void saveGroupDisplay(Group group);
    public void saveGroupPrefix(Group group);
    public void saveGroupSuffix(Group group);

    public int cleanup(List<Group> groups, List<User> users,int version);
    public void format(List<Group> groups, List<User> users,int version);
    
    public void clearDatabase();
}
