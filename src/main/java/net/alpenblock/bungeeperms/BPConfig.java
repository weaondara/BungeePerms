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
    private PermissionsResolver.ResolvingMode resolvingMode;

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
        migrate();
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
        resolvingMode = config.getEnumValue("permissions.resolvingmode", PermissionsResolver.ResolvingMode.SEQUENTIAL);

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

    private void migrate()
    {
        config.load();

        //test current version
        int version = 0;
        if (config.keyExists("version"))
            version = config.getInt("version", 0);

        //migrate if needed
        Config newconf;
        switch (version)
        {
            case 0:
                newconf = new Config(config.getPath());
                newconf.init();
                migrate0to1(config, newconf);
                newconf.setInt("version", 1);
                newconf.save();
                config = newconf;
            case 1:
            //do nothing
        }
    }

    protected void migrate0to1(Config oldconf, Config newconf)
    {
        //perms
        newconf.setEnumValue("backend.type", oldconf.getString("backendtype", "YAML").equalsIgnoreCase("MYSQL2") ? BackEndType.MySQL : BackEndType.YAML);
        newconf.setBool("backend.useuuids", oldconf.getBoolean("useUUIDs", false));
        newconf.setBool("backend.saveAllUsers", oldconf.getBoolean("saveAllUsers", true));
        newconf.setBool("backend.deleteUsersOnCleanup", oldconf.getBoolean("deleteUsersOnCleanup", false));
        newconf.setInt("backend.uuidfetchercooldown", oldconf.getInt("uuidfetcher.cooldown", 3000));

        //mysql
        newconf.setString("mysql.user", oldconf.getString("bungeeperms.general.mysqluser", "bungeeperms"));
        newconf.setString("mysql.password", oldconf.getString("bungeeperms.general.mysqlpw", "password"));
        newconf.setString("mysql.tableprefix", oldconf.getString("tablePrefix", "bungeeperms_"));
        newconf.setString("mysql.url", "jdbc:mysql://"
                                       + oldconf.getString("bungeeperms.general.mysqlhost", "localhost") + ":"
                                       + oldconf.getString("bungeeperms.general.mysqlport", "3306") + "/"
                                       + oldconf.getString("bungeeperms.general.mysqldb", "bungeeperms") + "?autoReconnect=true&dontTrackOpenResources=true");

        //debug
        newconf.setString("debug.path", config.getString("debug.path", "plugins/BungeePerms/debug.log"));
        newconf.setBool("debug.showexceptions", config.getBoolean("debug.showexceptions", true));
        newconf.setBool("debug.showlogs", config.getBoolean("debug.showlogs", false));

        //perms
        newconf.setBool("permissions.useregexperms", oldconf.getBoolean("useregexperms", false));
        newconf.setBool("permissions.grouppermission", oldconf.getBoolean("grouppermission", true));
        newconf.setEnumValue("permissions.resolvingmode", PermissionsResolver.ResolvingMode.SEQUENTIAL);

        //fancy ingame
        newconf.setBool("ingame.notify.promote", oldconf.getBoolean("notify.promote", false));
        newconf.setBool("ingame.notify.demote", oldconf.getBoolean("notify.demote", false));
        newconf.setBool("ingame.tabcomplete", oldconf.getBoolean("tabcomplete", false));
        newconf.setBool("ingame.terminate.prefix.reset", oldconf.getBoolean("terminate.prefix.reset", true));
        newconf.setBool("ingame.terminate.suffix.reset", oldconf.getBoolean("terminate.suffix.reset", true));
        newconf.setBool("ingame.terminate.prefix.space", oldconf.getBoolean("terminate.prefix.space", true));
        newconf.setBool("ingame.terminate.suffix.space", oldconf.getBoolean("terminate.suffix.space", true));

        //cleanup
        newconf.setInt("offlinecleanup.interval", oldconf.getInt("cleanup.interval", 30 * 60));
        newconf.setInt("offlinecleanup.threshold", oldconf.getInt("cleanup.threshold", 10 * 60));

        //other
        newconf.setBool("misc.async-commands", oldconf.getBoolean("async-commands", true));
        newconf.setString("misc.locale", oldconf.getString("locale", Statics.localeString(new Locale("en", "GB"))));
    }
}
