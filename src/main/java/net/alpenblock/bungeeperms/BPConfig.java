package net.alpenblock.bungeeperms;

import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.UUIDPlayerDBType;

import java.util.Locale;


public class BPConfig
{

    private Config config;

    //perms
    private boolean useUUIDs;
    private boolean useRegexPerms;
    private boolean groupPermission;

    //db
    private BackEndType backEndType;
    private UUIDPlayerDBType UUIDPlayerDBType;
    private String tablePrefix;
    private int fetcherCooldown;
    private boolean saveAllUsers;
    private boolean deleteUsersOnCleanup;

    //fancy ingame
    private boolean notifyPromote;
    private boolean notifyDemote;
    private boolean tabComplete;
    private Locale locale;
    private boolean terminatePrefixReset;
    private boolean terminateSuffixReset;
    private boolean terminatePrefixSpace;
    private boolean terminateSuffixSpace;

    //tmp at runtime
    private boolean debug;
    
    //cleanup
    private int cleanupInterval;
    private int cleanupThreshold;
    
    //other
    private boolean asyncCommands;

    public BPConfig(Config config)
    {
        this.config = config;
    }

    public void load()
    {
        config.load();

        //perms
        useUUIDs = config.getBoolean("useUUIDs", false);
        useRegexPerms = config.getBoolean("useregexperms", false);
        groupPermission = config.getBoolean("grouppermission", true);

        //db
        backEndType = config.getEnumValue("backendtype", BackEndType.YAML);
        UUIDPlayerDBType = config.getEnumValue("uuidplayerdb", UUIDPlayerDBType.YAML);
        tablePrefix = config.getString("tablePrefix", "bungeeperms_");
        fetcherCooldown = config.getInt("uuidfetcher.cooldown", 3000);
        saveAllUsers = config.getBoolean("saveAllUsers", true);
        deleteUsersOnCleanup = config.getBoolean("deleteUsersOnCleanup", false);

        //fancy ingame
        notifyPromote = config.getBoolean("notify.promote", false);
        notifyDemote = config.getBoolean("notify.demote", false);
        tabComplete = config.getBoolean("tabcomplete", false);
        locale = Locale.forLanguageTag(config.getString("locale", Statics.localeString(new Locale("en", "GB"))));
        terminatePrefixReset = config.getBoolean("terminate.prefix.reset", true);
        terminateSuffixReset = config.getBoolean("terminate.suffix.reset", true);
        terminatePrefixSpace = config.getBoolean("terminate.prefix.space", true);
        terminateSuffixSpace = config.getBoolean("terminate.suffix.space", true);
        
        //cleanup
        cleanupInterval = config.getInt("cleanup.interval", 30 * 60);
        cleanupThreshold = config.getInt("cleanup.threshold", 10 * 60);
        
        //other
        asyncCommands = config.getBoolean("async-commands", true);
        
        validate();
    }
    
    public void validate()
    {
        if(useUUIDs && UUIDPlayerDBType == UUIDPlayerDBType.None)
        {
            BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_USEUUID_NONE_UUID_DB));
        }
    }

    public void setUseUUIDs(boolean useUUIDs)
    {
        this.useUUIDs = useUUIDs;
        config.setBool("useUUIDs", useUUIDs);
        config.save();
    }

    public void setUUIDPlayerDB(UUIDPlayerDBType type)
    {
        this.UUIDPlayerDBType = type;
        config.setEnumValue("uuidplayerdb", type);
        config.save();
    }

    public void setBackendType(BackEndType type)
    {
        this.backEndType = type;
        config.setEnumValue("backendtype", type);
        config.save();
    }

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    public Config getConfig()
    {
        return config;
    }

    public Locale getLocale()
    {
        return locale;
    }

    public boolean isUseUUIDs()
    {
        return useUUIDs;
    }

    public boolean isUseRegexPerms()
    {
        return useRegexPerms;
    }

    public void setUseRegexPerms(boolean useRegexPerms)
    {
        this.useRegexPerms = useRegexPerms;
    }

    public boolean isGroupPermission()
    {
        return groupPermission;
    }

    public void setGroupPermission(boolean groupPermission)
    {
        this.groupPermission = groupPermission;
    }

    public BackEndType getBackEndType()
    {
        return backEndType;
    }

    public void setBackEndType(BackEndType backEndType)
    {
        this.backEndType = backEndType;
    }

    public net.alpenblock.bungeeperms.io.UUIDPlayerDBType getUUIDPlayerDBType()
    {
        return UUIDPlayerDBType;
    }

    public void setUUIDPlayerDBType(net.alpenblock.bungeeperms.io.UUIDPlayerDBType UUIDPlayerDBType)
    {
        this.UUIDPlayerDBType = UUIDPlayerDBType;
    }

    public String getTablePrefix()
    {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix)
    {
        this.tablePrefix = tablePrefix;
    }

    public int getFetcherCooldown()
    {
        return fetcherCooldown;
    }

    public boolean isSaveAllUsers()
    {
        return saveAllUsers;
    }

    public boolean isDeleteUsersOnCleanup()
    {
        return deleteUsersOnCleanup;
    }

    public boolean isNotifyPromote()
    {
        return notifyPromote;
    }

    public boolean isNotifyDemote()
    {
        return notifyDemote;
    }

    public boolean isTabComplete()
    {
        return tabComplete;
    }

    public boolean isTerminatePrefixReset()
    {
        return terminatePrefixReset;
    }

    public boolean isTerminateSuffixReset()
    {
        return terminateSuffixReset;
    }

    public boolean isTerminatePrefixSpace()
    {
        return terminatePrefixSpace;
    }

    public boolean isTerminateSuffixSpace()
    {
        return terminateSuffixSpace;
    }

    public boolean isDebug()
    {
        return debug;
    }

    public int getCleanupInterval()
    {
        return cleanupInterval;
    }

    public int getCleanupThreshold()
    {
        return cleanupThreshold;
    }

    public boolean isAsyncCommands()
    {
        return asyncCommands;
    }
}
