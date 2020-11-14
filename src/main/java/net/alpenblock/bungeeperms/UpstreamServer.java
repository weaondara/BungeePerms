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
package net.alpenblock.bungeeperms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import net.alpenblock.bungeeperms.io.upstream.UpstreamIO;
import net.alpenblock.bungeeperms.platform.bungee.BungeePlugin;

public class UpstreamServer
{

    private ServerSocket socket;
    private boolean running;

    public void start()
    {
        String host = BungeePerms.getInstance().getConfig().getUpstreamhost();
        int port = BungeePerms.getInstance().getConfig().getUpstreamport();
        if (port <= 0)
        {
            BungeePlugin.getInstance().getLogger().info("[UpstreamServer] Upstream server disabled. Not starting.");
            return;
        }
        try
        {
            BungeePlugin.getInstance().getLogger().info("[UpstreamServer] Starting ...");
            socket = new ServerSocket(port, 50, InetAddress.getByName(host));
            running = true;
        }
        catch (Exception e)
        {
            BungeePlugin.getInstance().getLogger().severe("[UpstreamServer] Failed to start upstream server ...");
            BungeePerms.getInstance().getDebug().log(e);
            return;
        }

        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                while (running)
                {
                    try
                    {
                        final Socket s = socket.accept();
                        BungeePlugin.getInstance().runTaskLaterAsync(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                new UpstreamHandler(s).socketloop();
                            }
                        }, 0);
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        };
        BungeePlugin.getInstance().runTaskLaterAsync(r, 0);
    }

    public void stop()
    {
        running = false;
        try
        {
            socket.close();
        }
        catch (Exception e)
        {
        }
    }

    private class UpstreamHandler
    {

        private final Socket socket;
        private String servername;

        public UpstreamHandler(Socket s)
        {
            socket = s;
        }

        public void socketloop()
        {
            try
            {
                while (true)
                {
//                    System.out.println("waiting for packet");
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    int len = dis.readInt();
//                    System.out.println("len: " + len);
                    byte[] b = new byte[len];
                    int pos = 0;
                    do
                    {
                        int read = dis.read(b, pos, len - pos);
                        pos += read;
                    }
                    while (pos < len);
//                    System.out.println("read packet");

                    DataInputStream is = new DataInputStream(new ByteArrayInputStream(b));
                    String cmd = is.readUTF();
                    String server = UpstreamIO.readStringNull(is);
                    String world = UpstreamIO.readStringNull(is);
                    int addinfolen = is.readInt();
                    byte[] baddinfo = new byte[addinfolen];
                    is.read(baddinfo);
                    DataInputStream isaddinfo = new DataInputStream(new ByteArrayInputStream(baddinfo));

//                    System.out.println("handling packet");
                    ByteArrayOutputStream res = handle(cmd, server, world, isaddinfo);
//                    System.out.println("handling packet done");
                    byte[] osdata = res.toByteArray();
                    DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                    os.writeInt(osdata.length);
                    os.write(osdata);
                }
            }
            catch (Exception e)
            {
                try
                {
                    socket.close();
                }
                catch (Exception i)
                {
                }
            }
        }

        @SneakyThrows
        private ByteArrayOutputStream handle(String cmdargs, String server, String world, DataInputStream addinfo)
        {
            String[] args = cmdargs.split(":");
            String cmd = args[0];

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream os = new DataOutputStream(baos);
            if ("ping".equals(cmd))
            {
                os.writeUTF("pong");
            }
            else if ("servername".equals(cmd))
            {
                servername = args[1];
                BungeePlugin.getInstance().getLogger().info("Upstream connected from " + servername);
            }
            else if ("loadgroups".equals(cmd))
            {
                List<Group> groups = BungeePerms.getInstance().getPermissionsManager().getGroups();
                os.writeInt(groups.size());
                for (Group g : groups)
                    UpstreamIO.writeGroup(os, g);
            }
            else if ("loadusers".equals(cmd))
            {
                List<User> users = BungeePerms.getInstance().getPermissionsManager().getBackEnd().loadUsers();
                os.writeInt(users.size());
                for (User u : users)
                    UpstreamIO.writeUser(os, u);
            }
            else if ("loadgroup".equals(cmd))
            {
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                if (group != null)
                {
                    os.writeInt(1);
                    UpstreamIO.writeGroup(os, group);
                }
                else
                    os.writeInt(0);
            }
            else if ("loadusername".equals(cmd))
            {
                User user = BungeePerms.getInstance().getPermissionsManager().getUser(args[1]);
                if (user != null)
                {
                    os.writeInt(1);
                    UpstreamIO.writeUser(os, user);
                }
                else
                    os.writeInt(0);
            }
            else if ("loaduseruuid".equals(cmd))
            {
                User user = BungeePerms.getInstance().getPermissionsManager().getUser(UUID.fromString(args[1]));
                if (user != null)
                {
                    os.writeInt(1);
                    UpstreamIO.writeUser(os, user);
                }
                else
                    os.writeInt(0);
            }
            else if ("loadversion".equals(cmd))
            {
                os.writeInt(BungeePerms.getInstance().getPermissionsManager().getBackEnd().loadVersion());
            }
            else if ("userindb".equals(cmd))
            {
                User user = BungeePerms.getInstance().getPermissionsManager().getUser(args[1]);
                os.writeBoolean(BungeePerms.getInstance().getPermissionsManager().getBackEnd().isUserInDatabase(user));
            }
            else if ("registeredusers".equals(cmd))
            {
                UpstreamIO.writeStringList(os, BungeePerms.getInstance().getPermissionsManager().getBackEnd().getRegisteredUsers());
            }
            else if ("groupusers".equals(cmd))
            {
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                UpstreamIO.writeStringList(os, BungeePerms.getInstance().getPermissionsManager().getBackEnd().getGroupUsers(group));
            }
            else if ("saveuser".equals(cmd))
            {
                User user = UpstreamIO.readUser(addinfo);
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveUser(user, true);
            }
            else if ("savegroup".equals(cmd))
            {
                Group group = UpstreamIO.readGroup(addinfo);
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveGroup(group, true);
            }
            else if ("deleteuser".equals(cmd))
            {
                User user = BungeePerms.getInstance().getPermissionsManager().getUser(args[1]);
                BungeePerms.getInstance().getPermissionsManager().deleteUser(user);
            }
            else if ("deletegroup".equals(cmd))
            {
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                BungeePerms.getInstance().getPermissionsManager().deleteGroup(group);
            }
            else if ("saveusergroups".equals(cmd))
            {
                User user = BungeePerms.getInstance().getPermissionsManager().getUser(args[1]);
                List<String> groups = UpstreamIO.readStringList(addinfo);
                Permable perm;
                if (server == null)
                    perm = user;
                else if (world == null)
                    perm = user.getServer(server);
                else
                    perm = user.getServer(server).getWorld(world);
                perm.setGroups(groups);
                user.invalidateCache();
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveUserGroups(user, server, world);
            }
            else if ("saveusertimedgroups".equals(cmd))
            {
                User user = BungeePerms.getInstance().getPermissionsManager().getUser(args[1]);
                List<TimedValue<String>> groups = UpstreamIO.readStringListTimed(addinfo);
                Permable perm;
                if (server == null)
                    perm = user;
                else if (world == null)
                    perm = user.getServer(server);
                else
                    perm = user.getServer(server).getWorld(world);
                perm.setTimedGroups(groups);
                user.invalidateCache();
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveUserTimedGroups(user, server, world);
            }
            else if ("saveuserperms".equals(cmd))
            {
                User user = BungeePerms.getInstance().getPermissionsManager().getUser(args[1]);
                List<String> perms = UpstreamIO.readStringList(addinfo);
                Permable perm;
                if (server == null)
                    perm = user;
                else if (world == null)
                    perm = user.getServer(server);
                else
                    perm = user.getServer(server).getWorld(world);
                perm.setPerms(perms);
                user.invalidateCache();
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveUserPerms(user, server, world);
            }
            else if ("saveusertimedperms".equals(cmd))
            {
                User user = BungeePerms.getInstance().getPermissionsManager().getUser(args[1]);
                List<TimedValue<String>> timedperms = UpstreamIO.readStringListTimed(addinfo);
                Permable perm;
                if (server == null)
                    perm = user;
                else if (world == null)
                    perm = user.getServer(server);
                else
                    perm = user.getServer(server).getWorld(world);
                perm.setTimedPerms(timedperms);
                user.invalidateCache();
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveUserTimedPerms(user, server, world);
            }
            else if ("saveuserdisplay".equals(cmd))
            {
                args = cmdargs.split(":", 3);
                User user = BungeePerms.getInstance().getPermissionsManager().getUser(args[1]);
                BungeePerms.getInstance().getPermissionsManager().setUserDisplay(user, args[2], server, world);
            }
            else if ("saveuserprefix".equals(cmd))
            {
                args = cmdargs.split(":", 3);
                User user = BungeePerms.getInstance().getPermissionsManager().getUser(args[1]);
                BungeePerms.getInstance().getPermissionsManager().setUserPrefix(user, args[2], server, world);
            }
            else if ("saveusersuffix".equals(cmd))
            {
                args = cmdargs.split(":", 3);
                User user = BungeePerms.getInstance().getPermissionsManager().getUser(args[1]);
                BungeePerms.getInstance().getPermissionsManager().setUserSuffix(user, args[2], server, world);
            }
            else if ("savegroupperms".equals(cmd))
            {
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                List<String> perms = UpstreamIO.readStringList(addinfo);
                Permable perm;
                if (server == null)
                    perm = group;
                else if (world == null)
                    perm = group.getServer(server);
                else
                    perm = group.getServer(server).getWorld(world);
                perm.setPerms(perms);
                group.invalidateCache(); //todo invalidate all
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveGroupPerms(group, server, world);
            }
            else if ("savegrouptimedperms".equals(cmd))
            {
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                List<TimedValue<String>> timedperms = UpstreamIO.readStringListTimed(addinfo);
                Permable perm;
                if (server == null)
                    perm = group;
                else if (world == null)
                    perm = group.getServer(server);
                else
                    perm = group.getServer(server).getWorld(world);
                perm.setTimedPerms(timedperms);
                group.invalidateCache(); //todo invalidate all
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveGroupTimedPerms(group, server, world);
            }
            else if ("savegroupinherits".equals(cmd))
            {
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                List<String> groups = UpstreamIO.readStringList(addinfo);
                Permable perm;
                if (server == null)
                    perm = group;
                else if (world == null)
                    perm = group.getServer(server);
                else
                    perm = group.getServer(server).getWorld(world);
                perm.setGroups(groups);
                group.invalidateCache(); //todo invalidate all
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveGroupInheritances(group, server, world);
            }
            else if ("savegrouptimedinherits".equals(cmd))
            {
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                List<TimedValue<String>> groups = UpstreamIO.readStringListTimed(addinfo);
                Permable perm;
                if (server == null)
                    perm = group;
                else if (world == null)
                    perm = group.getServer(server);
                else
                    perm = group.getServer(server).getWorld(world);
                perm.setTimedGroups(groups);
                group.invalidateCache(); //todo invalidate all
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveGroupTimedInheritances(group, server, world);
            }
            else if ("savegroupladder".equals(cmd))
            {
                args = cmdargs.split(":", 3);
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                BungeePerms.getInstance().getPermissionsManager().ladderGroup(group, args[2]);
            }
            else if ("savegrouprank".equals(cmd))
            {
                //todo invalidate all
                args = cmdargs.split(":", 3);
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                BungeePerms.getInstance().getPermissionsManager().rankGroup(group, Integer.parseInt(args[2]));
            }
            else if ("savegroupweight".equals(cmd))
            {
                //todo invalidate all
                args = cmdargs.split(":", 3);
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                BungeePerms.getInstance().getPermissionsManager().weightGroup(group, Integer.parseInt(args[2]));
            }
            else if ("savegroupdefault".equals(cmd))
            {
                args = cmdargs.split(":", 3);
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                BungeePerms.getInstance().getPermissionsManager().setGroupDefault(group, Boolean.parseBoolean(args[2]));
            }
            else if ("savegroupdisplay".equals(cmd))
            {
                args = cmdargs.split(":", 3);
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                BungeePerms.getInstance().getPermissionsManager().setGroupDisplay(group, args[2], server, world);
            }
            else if ("savegroupprefix".equals(cmd))
            {
                args = cmdargs.split(":", 3);
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                BungeePerms.getInstance().getPermissionsManager().setGroupPrefix(group, args[2], server, world);
            }
            else if ("savegroupsuffix".equals(cmd))
            {
                args = cmdargs.split(":", 3);
                Group group = BungeePerms.getInstance().getPermissionsManager().getGroup(args[1]);
                BungeePerms.getInstance().getPermissionsManager().setGroupSuffix(group, args[2], server, world);
            }
            else if ("format".equals(cmd))
            {
                BungeePerms.getInstance().getPermissionsManager().format();
            }
            else if ("cleanup".equals(cmd))
            {
                int cleanup = BungeePerms.getInstance().getPermissionsManager().cleanup();
                os.write(cleanup);
            }
            else if ("uuid".equals(cmd))
            {
                UUID uuid = BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getUUID(args[1]);
                UpstreamIO.writeStringNull(os, uuid == null ? null : uuid.toString());
            }
            else if ("name".equals(cmd))
            {
                UpstreamIO.writeStringNull(os, BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getPlayerName(UUID.fromString(args[1])));
            }
            else if ("uuidplayerall".equals(cmd))
            {
                Map<UUID, String> all = BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getAll();
                for (Map.Entry<UUID, String> e : all.entrySet())
                {
                    os.writeUTF(e.getKey().toString());
                    os.writeUTF(e.getValue());
                }
            }
            else
            {
                System.err.println("[UpstreamServer] Unknown command: " + cmd);
            }
            os.flush();

            return baos;
        }
    }
}
