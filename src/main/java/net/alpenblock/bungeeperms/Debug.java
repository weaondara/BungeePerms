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

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;

public class Debug
{

    private Logger logger;
    private String path;
    private BPConfig config;
    private PlatformPlugin plugin;
    private boolean showexceptions;
    private boolean showlogs;

    public Debug(PlatformPlugin p, BPConfig conf, String loggername)
    {
        plugin = p;
        config = conf;
        loadconfig();
        File file = new File(path);
        try
        {
            if (!file.isFile() || !file.exists())
            {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            logger = Logger.getLogger(loggername + "Debug");
            logger.setUseParentHandlers(false);
            FileHandler fh = new FileHandler(path, true);
            fh.setFormatter(new DebugFormatter());
            logger.addHandler(fh);
        }
        catch (IOException e)
        {
            System.err.println("Failed to create debug log file " + file + "! Cause: " + e.getMessage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void loadconfig()
    {
        path = config.getDebugPath();
        showexceptions = config.isDebugShowExceptions();
        showlogs = config.isDebugShowLogs();
    }

    public void log(String str)
    {
        File file = new File(path);
        if (!file.isFile())
        {
            try
            {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        if (str == null)
        {
            str = "null";
        }
        logger.info(str);
        if (showlogs || config.isDebug())
        {
            BungeePerms.getLogger().info("[Debug] " + str);
        }
    }

    public void log(Object o)
    {
        if (o == null)
        {
            log("null");
        }
        else
        {
            log(o.toString());
        }
    }

    public void log(Exception e)
    {
        File file = new File(path);
        if (!file.isFile())
        {
            try
            {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        logger.log(Level.SEVERE, e.getMessage(), e);
        if (showexceptions || config.isDebug())
        {
            e.printStackTrace();
        }
    }
}
