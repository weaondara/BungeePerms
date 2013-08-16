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
    public List<Group> getGroups();
    public List<User> getUsers();
    public int getVersion();
    public void setVersion(int version);
    
    public boolean isUserInDatabase(User user);
    
    public Group getGroup(String groupname);
    public User getUser(String username);
    
    public void addUser(User user);
    public void addGroup(Group group);
    public void deleteUser(User user);
    public void deleteGroup(Group group);

    public void addUserGroup(User user, Group group);
    public void removeUserGroup(User u, Group group);

    public void addUserPerm(User user, String perm);
    public void removeUserPerm(User user, String perm);

    public void addUserPerServerPerm(User user, String server, String perm);
    public void removeUserPerServerPerm(User user, String server, String perm);

    public void addGroupPerm(Group group, String perm);
    public void removeGroupPerm(Group group, String perm);
 
    public void addGroupPerServerPerm(Group group, String server, String perm);
    public void removeGroupPerServerPerm(Group group, String server, String perm);

    public void addGroupInheritance(Group group, String toadd);
    public void removeGroupInheritance(Group group, String toremove);

    public void rankGroup(Group group, int rank);
    public void ladderGroup(Group group, String ladder);
    public void setGroupDefault(Group group, boolean adefault);
    public void setGroupDisplay(Group group, String display);
    public void setGroupPrefix(Group group, String prefix);
    public void setGroupSuffix(Group group, String suffix);

    public int cleanup();
    public void format();
    
    public void clearDatabase();
}
