package net.alpenblock.bungeeperms;

import java.util.Locale;
import lombok.Getter;
import lombok.Setter;
import net.alpenblock.bungeeperms.io.BackEndType;

@Getter
public class BPConfig
{

    protected Config config;

    //backend
    private BackEndType backendType;
    private boolean useUUIDs;
    private boolean saveAllUsers;
    private boolean deleteUsersOnCleanup;
    private int fetcherCooldown;

    //mysql
    private String mysqlURL;
    private String mysqlUser;
    private String mysqlPassword;
    private String mysqlTablePrefix;

    //debug
    private String debugPath;
    private boolean debugShowExceptions;
    private boolean debugShowLogs;

    //perms
    private boolean useRegexPerms;
    private boolean groupPermission;

    //ingame
    private boolean notifyPromote;
    private boolean notifyDemote;
    private boolean tabComplete;
    private boolean terminatePrefixReset;
    private boolean terminateSuffixReset;
    private boolean terminatePrefixSpace;
    private boolean terminateSuffixSpace;

    //cleanup
    private int cleanupInterval;
    private int cleanupThreshold;

    //misc
    private boolean asyncCommands;
    private Locale locale;

    //tmp at runtime
    @Setter
    private boolean debug;

    public BPConfig(Config config)
    {
        this.config = config;
    }

    public void load()
    {
        config.load();

        //backend
        backendType = config.getEnumValue("backend.type", BackEndType.YAML);
        useUUIDs = config.getBoolean("backend.useuuids", true);
        saveAllUsers = config.getBoolean("backend.saveAllUsers", false);
        deleteUsersOnCleanup = config.getBoolean("backend.deleteUsersOnCleanup", true);
        fetcherCooldown = config.getInt("backend.uuidfetchercooldown", 3000);

        //mysql
        mysqlUser = config.getString("mysql.user", "bungeeperms");
        mysqlPassword = config.getString("mysql.password", "password");
        mysqlTablePrefix = config.getString("mysql.tableprefix", "bungeeperms_");
        mysqlURL = config.getString("mysql.url", "jdbc:mysql://localhost:3306/bungeperms?autoReconnect=true&dontTrackOpenResources=true");

        //debug
        debugPath = config.getString("debug.path", "plugins/BungeePerms/debug.log");
        debugShowExceptions = config.getBoolean("debug.showexceptions", true);
        debugShowLogs = config.getBoolean("debug.showlogs", false);

        //perms
        useRegexPerms = config.getBoolean("permissions.useregexperms", false);
        groupPermission = config.getBoolean("permissions.grouppermission", true);

        //fancy ingame
        notifyPromote = config.getBoolean("ingame.notify.promote", false);
        notifyDemote = config.getBoolean("ingame.notify.demote", false);
        tabComplete = config.getBoolean("ingame.tabcomplete", false);
        terminatePrefixReset = config.getBoolean("ingame.terminate.prefix.reset", true);
        terminateSuffixReset = config.getBoolean("ingame.terminate.suffix.reset", true);
        terminatePrefixSpace = config.getBoolean("ingame.terminate.prefix.space", true);
        terminateSuffixSpace = config.getBoolean("ingame.terminate.suffix.space", true);

        //cleanup
        cleanupInterval = config.getInt("offlinecleanup.interval", 30 * 60);
        cleanupThreshold = config.getInt("offlinecleanup.threshold", 10 * 60);

        //misc
        asyncCommands = config.getBoolean("misc.async-commands", true);
        locale = Locale.forLanguageTag(config.getString("misc.locale", Statics.localeString(new Locale("en", "GB"))));

        validate();
    }

    public void validate()
    {
    }

    public void setUseUUIDs(boolean useUUIDs)
    {
        this.useUUIDs = useUUIDs;
        config.setBool("backend.useuuids", useUUIDs);
        config.save();
    }

    public void setBackendType(BackEndType type)
    {
        this.backendType = type;
        config.setEnumValue("backend.type", type);
        config.save();
    }
}
