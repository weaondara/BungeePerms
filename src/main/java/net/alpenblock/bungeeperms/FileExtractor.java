package net.alpenblock.bungeeperms;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import net.alpenblock.bungeeperms.Lang.MessageType;

public class FileExtractor
{

    public static final Map<String, String> ALL_FILES = new HashMap<>();

    static
    {
        if (!new File(BungeePerms.getInstance().getPlugin().getPluginFolderPath() + "/permissions.yml").isFile())
        {
            ALL_FILES.put("permissions.groups.yml", BungeePerms.getInstance().getPlugin().getPluginFolderPath() + "/permissions.groups.yml");
            ALL_FILES.put("permissions.users.yml", BungeePerms.getInstance().getPlugin().getPluginFolderPath() + "/permissions.users.yml");
        }
        ALL_FILES.put("lang/en-GB.yml", BungeePerms.getInstance().getPlugin().getPluginFolderPath() + "/lang/en-GB.yml");
        ALL_FILES.put("lang/de-DE.yml", BungeePerms.getInstance().getPlugin().getPluginFolderPath() + "/lang/de-DE.yml");
        ALL_FILES.put("lang/bg-BG.yml", BungeePerms.getInstance().getPlugin().getPluginFolderPath() + "/lang/bg-BG.yml");
    }

    public static void extractAll()
    {
        for (Map.Entry<String, String> e : ALL_FILES.entrySet())
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

        BungeePerms.getLogger().info(Lang.translate(MessageType.EXTRACTING, file));
        f.getParentFile().mkdirs();
        try
        {
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
            BungeePerms.getLogger().info(Lang.translate(MessageType.EXTRACTION_FAILED, file, e.getMessage()));
            BungeePerms.getInstance().getDebug().log(e);
            return;
        }
        BungeePerms.getLogger().info(Lang.translate(MessageType.EXTRACTION_DONE, file));
    }
}
