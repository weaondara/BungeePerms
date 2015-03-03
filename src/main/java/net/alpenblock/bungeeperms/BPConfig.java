package net.alpenblock.bungeeperms;

import lombok.Getter;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.UUIDPlayerDBType;

@Getter
public class BPConfig
{

    protected Config config;

    //perms
    private boolean useUUIDs;
    private boolean useRegexPerms;

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

        //db
        backEndType = config.getEnumValue("backendtype", BackEndType.YAML);
        UUIDPlayerDBType = config.getEnumValue("uuidplayerdb", UUIDPlayerDBType.None);
        tablePrefix = config.getString("tablePrefix", "bungeeperms_");
        fetcherCooldown = config.getInt("uuidfetcher.cooldown", 3000);
        saveAllUsers = config.getBoolean("saveAllUsers", true);
        deleteUsersOnCleanup = config.getBoolean("deleteUsersOnCleanup", false);

        //fancy ingame
        notifyPromote = config.getBoolean("notify.promote", false);
        notifyDemote = config.getBoolean("notify.demote", false);
        tabComplete = config.getBoolean("tabcomplete", false);
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
}
