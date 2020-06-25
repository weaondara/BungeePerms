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
package net.alpenblock.bungeeperms.io.upstream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import lombok.SneakyThrows;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Server;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.TimedValue;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.World;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin;

public class UpstreamIO
{

    @SneakyThrows
    public static List<String> readStringList(DataInputStream is)
    {
        int len = is.readInt();
        List<String> ret = new ArrayList(len);
        for (int i = 0; i < len; i++)
            ret.add(is.readUTF());
        return ret;
    }

    @SneakyThrows
    public static List<TimedValue<String>> readStringListTimed(DataInputStream is)
    {
        int len = is.readInt();
        List<TimedValue<String>> ret = new ArrayList(len);
        for (int i = 0; i < len; i++)
        {
            String val = is.readUTF();
            long start = is.readLong();
            int dur = is.readInt();
            ret.add(new TimedValue<>(val, new Date(start), dur));
        }
        return ret;
    }

    @SneakyThrows
    public static String readStringNull(DataInputStream is)
    {
        boolean set = is.readBoolean();
        if (set)
            return is.readUTF();
        else
            return null;
    }

    @SneakyThrows
    public static void writeStringList(DataOutputStream os, List<String> l)
    {
        os.writeInt(l.size());
        for (String s : l)
            os.writeUTF(s);
    }

    @SneakyThrows
    public static void writeStringListTimed(DataOutputStream os, List<TimedValue<String>> l)
    {
        os.writeInt(l.size());
        for (TimedValue<String> s : l)
        {
            os.writeUTF(s.getValue());
            os.writeLong(s.getStart().getTime());
            os.writeInt(s.getDuration());
        }
    }

    @SneakyThrows
    public static void writeStringNull(DataOutputStream os, String s)
    {
        os.writeBoolean(s != null);
        if (s != null)
            os.writeUTF(s);
    }

    @SneakyThrows
    public static Group readGroup(DataInputStream is)
    {
        String name = is.readUTF();
        List<String> inheritances = readStringList(is);
        List<TimedValue<String>> timedinheritances = readStringListTimed(is);
        List<String> permissions = readStringList(is);
        List<TimedValue<String>> timedperms = readStringListTimed(is);
        boolean isdefault = is.readBoolean();
        int rank = is.readInt();
        int weight = is.readInt();
        String ladder = readStringNull(is);
        String display = readStringNull(is);
        String prefix = readStringNull(is);
        String suffix = readStringNull(is);

        //per server perms
        int serverlen = is.readInt();
        Map<String, Server> servers = new HashMap<>();
        for (int si = 0; si < serverlen; si++)
        {
            String server = is.readUTF();
            List<String> serverinheritances = readStringList(is);
            List<TimedValue<String>> stimedinheritances = readStringListTimed(is);
            List<String> serverperms = readStringList(is);
            List<TimedValue<String>> stimedperms = readStringListTimed(is);
            String sdisplay = readStringNull(is);
            String sprefix = readStringNull(is);
            String ssuffix = readStringNull(is);

            //per server world perms
            int worldlen = is.readInt();
            Map<String, World> worlds = new HashMap<>();
            for (int wi = 0; wi < worldlen; wi++)
            {
                String world = is.readUTF();
                List<String> worldinheritances = readStringList(is);
                List<TimedValue<String>> wtimedinheritances = readStringListTimed(is);
                List<String> worldperms = readStringList(is);
                List<TimedValue<String>> wtimedperms = readStringListTimed(is);
                String wdisplay = readStringNull(is);
                String wprefix = readStringNull(is);
                String wsuffix = readStringNull(is);

                worlds.put(Statics.toLower(world), new World(Statics.toLower(world), worldinheritances, wtimedinheritances, worldperms, wtimedperms, wdisplay, wprefix, wsuffix));
            }

            servers.put(Statics.toLower(server), new Server(Statics.toLower(server), serverinheritances, stimedinheritances, serverperms, stimedperms, worlds, sdisplay, sprefix, ssuffix));
        }

        Group g = new Group(name, inheritances, timedinheritances, permissions, timedperms, servers, rank, weight, ladder, isdefault, display, prefix, suffix);
        g.invalidateCache();
        return g;
    }

    @SneakyThrows
    public static User readUser(DataInputStream is)
    {
        //load user from database
        String name = is.readUTF();
        String suuid = readStringNull(is);
        UUID uuid = suuid == null ? null : UUID.fromString(suuid);
        List<String> groups = readStringList(is);
        List<TimedValue<String>> timedgroups = readStringListTimed(is);
        List<String> perms = readStringList(is);
        List<TimedValue<String>> timedperms = readStringListTimed(is);
        String display = readStringNull(is);
        String prefix = readStringNull(is);
        String suffix = readStringNull(is);

        //per server perms
        int serverlen = is.readInt();
        Map<String, Server> servers = new HashMap<>();
        for (int si = 0; si < serverlen; si++)
        {
            String server = is.readUTF();
            List<String> servergroups = readStringList(is);
            List<TimedValue<String>> stimedgroups = readStringListTimed(is);
            List<String> serverperms = readStringList(is);
            List<TimedValue<String>> stimedperms = readStringListTimed(is);
            String sdisplay = readStringNull(is);
            String sprefix = readStringNull(is);
            String ssuffix = readStringNull(is);

            //per server world perms
            int worldlen = is.readInt();
            Map<String, World> worlds = new HashMap<>();
            for (int wi = 0; wi < worldlen; wi++)
            {
                String world = is.readUTF();
                List<String> worldgroups = readStringList(is);
                List<TimedValue<String>> wtimedgroups = readStringListTimed(is);
                List<String> worldperms = readStringList(is);
                List<TimedValue<String>> wtimedperms = readStringListTimed(is);
                String wdisplay = readStringNull(is);
                String wprefix = readStringNull(is);
                String wsuffix = readStringNull(is);

                worlds.put(Statics.toLower(world), new World(Statics.toLower(world), worldgroups, wtimedgroups, worldperms, wtimedperms, wdisplay, wprefix, wsuffix));
            }

            servers.put(Statics.toLower(server), new Server(Statics.toLower(server), servergroups, stimedgroups, serverperms, stimedperms, worlds, sdisplay, sprefix, ssuffix));
        }

        User u = new User(name, uuid, groups, timedgroups, perms, timedperms, servers, display, prefix, suffix);
        u.invalidateCache();
        return u;
    }

    @SneakyThrows
    public static void writeGroup(DataOutputStream os, Group group)
    {
        os.writeUTF(group.getName());
        writeStringList(os, group.getInheritancesString());
        writeStringListTimed(os, group.getTimedInheritancesString());
        writeStringList(os, group.getPerms());
        writeStringListTimed(os, group.getTimedPerms());
        os.writeBoolean(group.isDefault());
        os.writeInt(group.getRank());
        os.writeInt(group.getWeight());
        writeStringNull(os, group.getLadder());
        writeStringNull(os, group.getDisplay());
        writeStringNull(os, group.getPrefix());
        writeStringNull(os, group.getSuffix());

        os.writeInt(group.getServers().size());
        for (Map.Entry<String, Server> se : group.getServers().entrySet())
        {
            os.writeUTF(se.getKey());
            writeStringList(os, se.getValue().getGroupsString());
            writeStringListTimed(os, se.getValue().getTimedGroupsString());
            writeStringList(os, se.getValue().getPerms());
            writeStringListTimed(os, se.getValue().getTimedPerms());
            writeStringNull(os, se.getValue().getDisplay());
            writeStringNull(os, se.getValue().getPrefix());
            writeStringNull(os, se.getValue().getSuffix());

            os.writeInt(se.getValue().getWorlds().size());
            for (Map.Entry<String, World> we : se.getValue().getWorlds().entrySet())
            {
                os.writeUTF(we.getKey());
                writeStringList(os, we.getValue().getGroupsString());
                writeStringListTimed(os, we.getValue().getTimedGroupsString());
                writeStringList(os, we.getValue().getPerms());
                writeStringListTimed(os, we.getValue().getTimedPerms());
                writeStringNull(os, we.getValue().getDisplay());
                writeStringNull(os, we.getValue().getPrefix());
                writeStringNull(os, we.getValue().getSuffix());
            }
        }
    }

    @SneakyThrows
    public static void writeUser(DataOutputStream os, User user)
    {
        os.writeUTF(user.getName());
        UpstreamIO.writeStringNull(os, user.getUUID() == null ? null : user.getUUID().toString());
        UpstreamIO.writeStringList(os, user.getGroupsString());
        UpstreamIO.writeStringListTimed(os, user.getTimedGroupsString());
        UpstreamIO.writeStringList(os, user.getPerms());
        UpstreamIO.writeStringListTimed(os, user.getTimedPerms());
        UpstreamIO.writeStringNull(os, user.getDisplay());
        UpstreamIO.writeStringNull(os, user.getPrefix());
        UpstreamIO.writeStringNull(os, user.getSuffix());

        os.writeInt(user.getServers().size());
        for (Map.Entry<String, Server> se : user.getServers().entrySet())
        {
            os.writeUTF(se.getKey());
            UpstreamIO.writeStringList(os, se.getValue().getGroupsString());
            UpstreamIO.writeStringListTimed(os, se.getValue().getTimedGroupsString());
            UpstreamIO.writeStringList(os, se.getValue().getPerms());
            UpstreamIO.writeStringListTimed(os, se.getValue().getTimedPerms());
            UpstreamIO.writeStringNull(os, se.getValue().getDisplay());
            UpstreamIO.writeStringNull(os, se.getValue().getPrefix());
            UpstreamIO.writeStringNull(os, se.getValue().getSuffix());

            os.writeInt(se.getValue().getWorlds().size());
            for (Map.Entry<String, World> we : se.getValue().getWorlds().entrySet())
            {
                os.writeUTF(we.getKey());
                UpstreamIO.writeStringList(os, we.getValue().getGroupsString());
                UpstreamIO.writeStringListTimed(os, we.getValue().getTimedGroupsString());
                UpstreamIO.writeStringList(os, we.getValue().getPerms());
                UpstreamIO.writeStringListTimed(os, we.getValue().getTimedPerms());
                UpstreamIO.writeStringNull(os, we.getValue().getDisplay());
                UpstreamIO.writeStringNull(os, we.getValue().getPrefix());
                UpstreamIO.writeStringNull(os, we.getValue().getSuffix());
            }
        }
    }

    private Socket socket;
    private boolean running;
    private boolean connected;
    private long lastreconnectmsg = 0;

    public void start()
    {
        running = true;
        for (int i = 0; i < 3; i++)
        {
            if (connect())
                break; //success
            try
            {
                Thread.sleep(2000);
            }
            catch (InterruptedException ex)
            {
            }
        }
        if (!connected)// && BukkitPlugin.getInstance().getConf().getBackendType() == BackEndType.UPSTREAM)
        {
            BukkitPlugin.getInstance().getLogger().severe("Could not connect to UPSTREAM backend! Giving up after 3 retries");
            return;
        }

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                keepalive();
            }
        }, "BungeePerms UpstreamIO KeepAlive").start();
    }

    public void stop()
    {
        running = false;
        disconnect();
    }

    public void ping()
    {
        request("ping");
    }

    private boolean connect()
    {
        try
        {
            String host = BukkitPlugin.getInstance().getConf().getUpstreamhost();
            int port = BukkitPlugin.getInstance().getConf().getUpstreamport();
            socket = new Socket(host, port);
            connected = true;
            BukkitPlugin.getInstance().getLogger().info("Connection to upstream server established.");
            request("servername:" + BukkitPlugin.getInstance().getConf().getServername());
            return true;
        }
        catch (Exception ex)
        {
//            ex.printStackTrace();
            connected = false;
            if (running)
            {
                if (System.currentTimeMillis() - lastreconnectmsg > 30000)
                {
                    lastreconnectmsg = System.currentTimeMillis();
                    BukkitPlugin.getInstance().getLogger().warning("Connection to Upstream backend lost. Reconnecting ...");
                }
            }
            return false;
        }
    }

    private void disconnect()
    {
        try
        {
            socket.close();
        }
        catch (Exception e)
        {
        }
        socket = null;
    }

    private void reconnect()
    {
        disconnect();
        connect();
    }

    private void keepalive()
    {
        while (running)
        {
            try
            {
                ping();
                Thread.sleep(5000);
            }
            catch (Exception ex)
            {
                reconnect();
            }
        }
    }

    private final ReentrantLock sync = new ReentrantLock(true);

    @SneakyThrows
    public DataInputStream request(String what)
    {
        sync.lock();
        try
        {
            return request(what, null, null, new ByteArrayOutputStream());
        }
        finally
        {
            sync.unlock();
        }
    }

    @SneakyThrows
    public DataInputStream request(String what, String server, String world, ByteArrayOutputStream addinfo)
    {
        //compile msg
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        DataOutputStream tmpos = new DataOutputStream(tmp);
        tmpos.writeUTF(what);
        writeStringNull(tmpos, server);
        writeStringNull(tmpos, world);
        byte[] adddata = addinfo.toByteArray();
        tmpos.writeInt(adddata.length);
        tmpos.write(adddata);
        tmpos.flush();

        //send
        int retries = 3;
        do
        {
            sync.lock();
            try
            {
                //send with prepended length of packet
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                byte[] tmpdata = tmp.toByteArray();
                dout.writeInt(tmpdata.length);
                dout.write(tmpdata);

                //read packet
                DataInputStream is = new DataInputStream(socket.getInputStream());
                int len = is.readInt();
                byte[] buf = new byte[len];
                int pos = 0;
                do
                {
                    int read = is.read(buf, pos, len - pos);
                    pos += read;
                }
                while (pos < len);
                return new DataInputStream(new ByteArrayInputStream(buf));
            }
            catch (Exception e)
            {
                reconnect();
                if (!connected)
                    Thread.sleep(100);
            }
            finally
            {
                sync.unlock();
            }
        }
        while (retries-- > 0);
        return null;
    }
}
