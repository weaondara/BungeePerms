package net.alpenblock.bungeeperms;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class FileExtractor
{

    public static final Map<String, String> allFiles = new HashMap<>();

    static
    {
        allFiles.put("permissions.yml", BungeePerms.getInstance().getPlugin().getPluginFolderPath() + "/permissions.yml");
        allFiles.put("lang/EN-gb.yml", BungeePerms.getInstance().getPlugin().getPluginFolderPath() + "/lang/EN-gb.yml");
    }

    public static void extractAll()
    {
        for (Map.Entry<String, String> e : allFiles.entrySet())
        {
            extract(e.getKey(), e.getValue());
        }
    }

    public static void extract(String file, String dest)
    {
        File f = new File(dest);
        if (f.isFile())
        {
            return;
        }

        BungeePerms.getLogger().info("extracting " + file);
        f.getParentFile().mkdirs();
        try
        {
            //file öffnen
            ClassLoader cl = FileExtractor.class.getClassLoader();
            URL url = cl.getResource(file);
            if (url != null)
            {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                Files.copy(connection.getInputStream(), f.toPath());
            }
        }
        catch (Exception e)
        {
            BungeePerms.getLogger().info("could not extract file " + file + ": " + e.getMessage());
            e.printStackTrace();
            return;
        }
        BungeePerms.getLogger().info("extracted " + file);
    }
}
