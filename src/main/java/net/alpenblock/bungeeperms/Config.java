package net.alpenblock.bungeeperms;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.alpenblock.bungeeperms.config.FileConfiguration;
import net.alpenblock.bungeeperms.config.YamlConfiguration;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;

public class Config
{

    private boolean allowsave;
    private FileConfiguration fconfig;
    private String path;

    public Config(PlatformPlugin p, String path)
    {
        this.path = p.getPluginFolder() + path;
        createFile();
        fconfig = YamlConfiguration.loadConfiguration(new File(this.path));
        allowsave = false;
    }

    public Config(String path)
    {
        this.path = path;
        createFile();
        fconfig = YamlConfiguration.loadConfiguration(new File(this.path));
        allowsave = false;
    }

    public void load()
    {
        createFile();
        try
        {
            fconfig.load(path);
            allowsave = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            allowsave = false;
        }
    }

    public void save()
    {
        createFile();
        try
        {
            if (allowsave)
            {
                fconfig.save(path);
            }
            else
            {
                throw new IllegalStateException("config failed to load; save not allowed");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void createFile()
    {
        File file = new File(path);
        if (!file.exists())
        {
            file.getParentFile().mkdirs();
            try
            {
                file.createNewFile();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public String getString(String key, String def)
    {

        if (fconfig.contains(key))
        {
            return fconfig.getString(key);
        }
        else
        {
            fconfig.set(key, def);
            save();
            return def;
        }
    }

    public int getInt(String key, int def)
    {

        if (fconfig.contains(key))
        {
            return fconfig.getInt(key);
        }
        else
        {
            fconfig.set(key, def);
            save();
            return def;
        }

    }

    public boolean getBoolean(String key, boolean def)
    {

        if (fconfig.contains(key))
        {
            return fconfig.getBoolean(key);
        }
        else
        {
            fconfig.set(key, def);
            save();
            return def;
        }

    }

    public <T extends Enum> T getEnumValue(String key, T def)
    {
        if (fconfig.contains(key))
        {
            String s = getString(key, def.name());
            T[] constants = (T[]) def.getDeclaringClass().getEnumConstants();
            for (T constant : constants)
            {
                if (constant.name().equals(s))
                {
                    return constant;
                }
            }
            return def;
        }
        else
        {
            load();
            fconfig.set(key, def.name());
            save();
            return def;
        }
    }

    public List<String> getListString(String key, List<String> def)
    {

        if (fconfig.contains(key))
        {
            return fconfig.getStringList(key);
        }
        else
        {
            fconfig.set(key, def);
            save();
            return def;
        }

    }

    public double getDouble(String key, double def)
    {
        if (fconfig.contains(key))
        {
            return fconfig.getDouble(key);
        }
        else
        {
            fconfig.set(key, def);
            save();
            return def;
        }
    }

    public void setString(String key, String val)
    {
        fconfig.set(key, val);
    }

    public void setStringAndSave(String key, String val)
    {
        fconfig.set(key, val);
        save();
    }

    public void setInt(String key, int val)
    {
        fconfig.set(key, val);
    }

    public void setIntAndSave(String key, int val)
    {
        fconfig.set(key, val);
        save();
    }

    public void setBool(String key, boolean val)
    {
        fconfig.set(key, val);
    }

    public void setBoolAndSave(String key, boolean val)
    {
        fconfig.set(key, val);
        save();
    }

    public <T extends Enum> void setEnumValue(String key, T val)
    {
        fconfig.set(key, val.name());
    }

    public <T extends Enum> void setEnumAndSave(String key, T val)
    {
        fconfig.set(key, val.name());
        save();
    }

    public void setListString(String key, List<String> val)
    {
        fconfig.set(key, val);
    }

    public void setListStringAndSave(String key, List<String> val)
    {
        fconfig.set(key, val);
        save();
    }

    public List<String> getSubNodes(String node)
    {
        List<String> ret = new ArrayList<String>();
        try
        {
            for (Object o : fconfig.getConfigurationSection(node).getKeys(false).toArray())
            {
                ret.add((String) o);
            }
        }
        catch (Exception e)
        {
        }
        return ret;
    }

    public void deleteNode(String node)
    {
        fconfig.set(node, null);
        save();
    }

    public boolean keyExists(String node)
    {
        return fconfig.contains(node);
    }
}
