package net.alpenblock.bungeeperms;

import java.util.EnumMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Lang
{

    private static final Map<MessageType, String> MAP = new EnumMap<>(MessageType.class);

    public static void load(String file)
    {
        Config langconf = new Config(file);
        langconf.load();

        MAP.clear();
        for (MessageType mt : MessageType.values())
        {
            MAP.put(mt, langconf.getString(mt.getConfigKey(), mt.getDefaultValue()));
        }
    }

    public static String translate(MessageType type, Object... vars)
    {
        String s = MAP.get(type);
        if (s == null)
        {
            s = type.getDefaultValue();
        }
        s = Statics.format(s, vars);
        return s.replaceAll("&", ChatColor.COLOR_CHAR + "");
    }

    @AllArgsConstructor
    @Getter
    public static enum MessageType
    {

        BUNGEEPERMS("bungeeperms", ChatColor.GOLD.alt() + "Welcome to BungeePerms, a BungeeCord/Spigot permissions plugin"),
        VERSION("version", Color.Text.alt() + "Version " + ChatColor.GOLD.alt() + "{0}"),
        AUTHOR("author", Color.Text.alt() + "Author " + ChatColor.GOLD.alt() + "{0}"),
        PERMISSIONS_RELOADED("permissions-reloaded", Color.Text.alt() + "Permissions reloaded"),
        DEBUG_ENABLED("debug.enabled", Color.Text.alt() + "Debug mode enabled."),
        DEBUG_DISABLED("debug.disabled", Color.Text.alt() + "Debug mode disabled."),
        PROMOTE_MESSAGE("promote-message", Color.User.alt() + "{0}" + Color.Text.alt() + " is now " + Color.Value.alt() + "{1}" + Color.Text.alt() + "!"),
        DEMOTE_MESSAGE("demote-message", Color.User.alt() + "{0}" + Color.Text.alt() + " is now " + Color.Value.alt() + "{1}" + Color.Text.alt() + "!"),
        PROMOTE_MESSAGE_TO_USER("promote-message-to-user", Color.Text.alt() + "You were promoted to " + Color.Value.alt() + "{0}" + Color.Text.alt() + "!"),
        DEMOTE_MESSAGE_TO_USER("demote-message-to-user", Color.Text.alt() + "You were demoted to " + Color.Value.alt() + "{0}" + Color.Text.alt() + "!"),
        FORMATTING("formatting", Color.Text.alt() + "Formatting permissions file/table ..."),
        FORMATTING_DONE("formatting-done", Color.Message.alt() + "Finished formatting."),
        CLEANING("cleaning", Color.Text.alt() + "Cleaning up permissions file/table ..."),
        CLEANING_DONE("cleaning-done", Color.Message.alt() + "Finished cleaning. Deleted " + Color.Value.alt() + "{0} users" + Color.Message.alt() + "."),
        NO_PERM("no-permission", ChatColor.RED.alt() + "You don't have permission to do that!"),
        //error msgs
        ERR_INVALID_BOOL_VALUE("error.invalid-bool-value", Color.Error.alt() + "A boolean value is required!"),
        ERR_INVALID_INT_VALUE("error.invalid-int-value", Color.Error.alt() + "An integer value greater than 0 is required!"),
        ERR_USER_NOT_EXISTING("error.user.not-existing", Color.Error.alt() + "The player " + Color.User.alt() + "{0}" + Color.Error.alt() + " does not exist!"),
        ERR_USER_ALREADY_IN_GROUP("error.user.already-in-group", Color.Error.alt() + "Player is already in group " + Color.Value.alt() + "{0}" + Color.Error.alt() + "!"),
        ERR_USER_NOT_IN_GROUP("error.user.not-in-group", Color.Error.alt() + "Player is not in group " + Color.Value.alt() + "{0}" + Color.Error.alt() + "!"),
        ERR_USER_NO_GROUPS("error.user.no-groups", Color.Error.alt() + "The player " + Color.User.alt() + "{0}" + Color.Error.alt() + " doesn't have a group!"),
        ERR_USER_CANNOT_BE_PROMOTED("error.user.cannot-be-promoted", Color.Error.alt() + "The player " + Color.User.alt() + "{0}" + Color.Error.alt() + " can't be promoted!"),
        ERR_USER_CANNOT_BE_DEMOTED("error.user.cannot-be-demoted", Color.Error.alt() + "The player " + Color.User.alt() + "{0}" + Color.Error.alt() + " can't be demoted!"),
        ERR_USER_YOU_NOT_EXISTING("error.user.you-not-existing", Color.Error.alt() + "You do not exist!"),
        ERR_USER_YOU_NO_GROUPS("error.user.you-no-groups", Color.Error.alt() + "You don't have a group!"),
        ERR_USER_YOU_CANNOT_PROMOTE("error.user.you-cannot-promote", Color.Error.alt() + "You can't promote the player " + Color.User.alt() + "{0}" + Color.Error.alt() + "!"),
        ERR_USER_YOU_CANNOT_DEMOTE("error.user.you-cannot-demote", Color.Error.alt() + "You can't demote the player " + Color.User.alt() + "{0}" + Color.Error.alt() + "!"),
        ERR_GROUP_NOT_EXISTING("error.group.not-existing", Color.Error.alt() + "The group " + Color.User.alt() + "{0}" + Color.Error.alt() + " does not exist!"),
        ERR_GROUP_ALREADY_INHERITS("error.group.already-inherits", Color.Error.alt() + "The group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " already inherits from " + Color.Value.alt() + "{1}" + Color.Error.alt() + "!"),
        ERR_GROUP_DOES_NOT_INHERITS("error.group.does-not-inherit", Color.Error.alt() + "The group " + Color.Value.alt() + "{0}" + Color.Error.alt() + " does not inherit from group " + Color.Value.alt() + "{1}" + Color.Error.alt() + "!"),
        //user msgs
        NO_USERS_FOUND("user.no-users-found", Color.Text.alt() + "No players found!"),
        REGISTERED_USERS("user.registered-users", Color.Text.alt() + "Following players are registered: "),
        REGISTERED_USERS_COUNT("user.registered-users-count", Color.Text.alt() + "There are " + Color.Value.alt() + "{0}" + Color.Text.alt() + " players registered."),
        USER_PERMISSIONS_LIST_HEADER("user.permissions-list-header", Color.Text.alt() + "Permissions of the player " + Color.User.alt() + "{0}" + Color.Text.alt() + ":"),
        USER_PERMISSIONS_LIST_HEADER_UUID("user.permissions-list-header-uuid", Color.Text.alt() + "Permissions of the player " + Color.User.alt() + "{0}" + Color.Text.alt() + " (" + Color.User.alt() + "{1}" + Color.Text.alt() + "):"),
        USER_GROUPS_HEADER("user.group-header", Color.Text.alt() + "Groups of the player " + Color.User.alt() + "{0}" + Color.Text.alt() + ":"),
        USER_ABOUT("user.about", Color.Text.alt() + "About " + Color.User.alt() + "{0}"),
        USER_UUID("user.uuid", Color.Text.alt() + "UUID: " + Color.Value.alt() + "{0}"),
        USER_GROUPS("user.groups", Color.Text.alt() + "Groups: {0}"),
        USER_PERMISSIONS("user.permissions", Color.Text.alt() + "User permissions: " + Color.Value.alt() + "{0}"),
        USER_ALL_PERMISSIONS_COUNT("user.all-permissions", Color.Text.alt() + "All permissions: " + Color.Value.alt() + "{0}"),
        USER_DELETED("user.deleted", Color.Text.alt() + "User deleted"),
        USER_ADDED_PERM("user.added-perm", Color.Text.alt() + "Added permission " + Color.Value.alt() + "{0}" + Color.Text.alt() + " to player " + Color.User.alt() + "{1}" + Color.Text.alt() + "."),
        USER_ADDED_PERM_SERVER("user.added-perm-server", Color.Text.alt() + "Added permission " + Color.Value.alt() + "{0}" + Color.Text.alt() + " to player " + Color.User.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + "."),
        USER_ADDED_PERM_SERVER_WORLD("user.added-perm-server-world", Color.Text.alt() + "Added permission " + Color.Value.alt() + "{0}" + Color.Text.alt() + " to player " + Color.User.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + " in world " + Color.Value.alt() + "{3}" + Color.Text.alt() + "."),
        USER_REMOVED_PERM("user.removed-perm", Color.Text.alt() + "Removed permission " + Color.Value.alt() + "{0}" + Color.Text.alt() + " from player " + Color.User.alt() + "{1}" + Color.Text.alt() + "."),
        USER_REMOVED_PERM_SERVER("user.removed-perm-server", Color.Text.alt() + "Removed permission " + Color.Value.alt() + "{0}" + Color.Text.alt() + " from player " + Color.User.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + "."),
        USER_REMOVED_PERM_SERVER_WORLD("user.removed-perm-server-world", Color.Text.alt() + "Removed permission " + Color.Value.alt() + "{0}" + Color.Text.alt() + " from player " + Color.User.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + " in world " + Color.Value.alt() + "{3}" + Color.Text.alt() + "."),
        USER_ALREADY_HAS_PERM("user.alreday-has-perm", Color.Text.alt() + "The player " + Color.Value.alt() + "{0}" + Color.Text.alt() + " already has the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + "."),
        USER_ALREADY_HAS_PERM_SERVER("user.alreday-has-perm-server", Color.Text.alt() + "The player " + Color.Value.alt() + "{0}" + Color.Text.alt() + " already has the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + "."),
        USER_ALREADY_HAS_PERM_SERVER_WORLD("user.alreday-has-perm-server-world", Color.Text.alt() + "The player " + Color.Value.alt() + "{0}" + Color.Text.alt() + " already has the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + " in world " + Color.Value.alt() + "{3}" + Color.Text.alt() + "."),
        USER_NEVER_HAD_PERM("user.never-had-perm", Color.Text.alt() + "The player " + Color.Value.alt() + "{0}" + Color.Text.alt() + " never had the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + "."),
        USER_NEVER_HAD_PERM_SERVER("user.never-had-perm-server", Color.Text.alt() + "The player " + Color.Value.alt() + "{0}" + Color.Text.alt() + " never had the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + "."),
        USER_NEVER_HAD_PERM_SERVER_WORLD("user.never-had-perm-server-world", Color.Text.alt() + "The player " + Color.Value.alt() + "{0}" + Color.Text.alt() + " never had the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + " in world " + Color.Value.alt() + "{3}" + Color.Text.alt() + "."),
        USER_HAS_PERM("user.has-perm", Color.Text.alt() + "Player " + Color.User.alt() + "{0}" + Color.Text.alt() + " has the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + ": {2}"),
        USER_HAS_PERM_SERVER("user.has-perm-server", Color.Text.alt() + "Player " + Color.User.alt() + "{0}" + Color.Text.alt() + " has the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + ": {3}"),
        USER_HAS_PERM_SERVER_WORLD("user.has-perm-server-world", Color.Text.alt() + "Player " + Color.User.alt() + "{0}" + Color.Text.alt() + " has the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + " in world " + Color.Value.alt() + "{3}" + Color.Text.alt() + ": {4}"),
        USER_ADDED_GROUP("user.added-group", Color.Text.alt() + "Added group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " to player " + Color.User.alt() + "{1}" + Color.Text.alt() + "."),
        USER_REMOVED_GROUP("user.removed-group", Color.Text.alt() + "Removed group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " from player " + Color.User.alt() + "{1}" + Color.Text.alt() + "."),
        USER_SET_GROUP("user.set-group", Color.Text.alt() + "Set group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " for player " + Color.User.alt() + "{1}" + Color.Text.alt() + "."),
        USER_SET_DISPLAY("user.set-display", Color.Text.alt() + "Set display name for user " + Color.Value.alt() + "{0}" + Color.Text.alt() + "."),
        USER_SET_PREFIX("user.set-prefix", Color.Text.alt() + "Set prefix for user " + Color.Value.alt() + "{0}" + Color.Text.alt() + "."),
        USER_SET_SUFFIX("user.set-suffix", Color.Text.alt() + "Set suffix for user " + Color.Value.alt() + "{0}" + Color.Text.alt() + "."),
        //group msgs
        NO_GROUPS_FOUND("group.no-groups-found", Color.Text.alt() + "No groups found!"),
        GROUPS_LIST_HEADER("group.groups-list-header", Color.Text.alt() + "There are following groups: "),
        GROUP_PERMISSIONS_LIST_HEADER("group.permissions-list-header", Color.Text.alt() + "Permissions of the group " + Color.Value.alt() + "{0}" + Color.Text.alt() + ":"),
        GROUP_ABOUT("group.about", Color.Text.alt() + "About group " + Color.Value.alt() + "{0}"),
        GROUP_INHERITANCES("group.inheritances", Color.Text.alt() + "Inheritances: {0}"),
        GROUP_PERMISSONS("group.permissions", Color.Text.alt() + "Group permissions: " + Color.Value.alt() + "{0}"),
        GROUP_ALL_PERMISSIONS("group.all-permissions", Color.Text.alt() + "All permissions: " + Color.Value.alt() + "{0}"),
        GROUP_RANK("group.rank", Color.Text.alt() + "Rank: " + Color.Value.alt() + "{0}"),
        GROUP_WEIGHT("group.weight", Color.Text.alt() + "Weight: " + Color.Value.alt() + "{0}"),
        GROUP_LADDER("group.ladder", Color.Text.alt() + "Ladder: " + Color.Value.alt() + "{0}"),
        GROUP_DEFAULT("group.default", Color.Text.alt() + "Default: {0}"),
        GROUP_USERS_HEADER("group.users-header", Color.Text.alt() + "Following players are in group " + Color.Value.alt() + "{0}" + Color.Text.alt() + ": "),
        GROUP_USERS_HEADER_COUNT("group.users-header-count", Color.Text.alt() + "There are " + Color.Value.alt() + "{0}" + Color.Text.alt() + " players in group " + Color.Value.alt() + "{1}" + Color.Text.alt() + "."),
        GROUP_CREATED("group.created", Color.Text.alt() + "Group {0} created"),
        GROUP_DELETED("group.deleted", Color.Text.alt() + "Group {0} deleted"),
        GROUP_DELETION_IN_PROGRESS("group.deletion-in-progress", Color.Text.alt() + "Group deletion in progress ... this may take a while (backend integrity check)."),
        GROUP_ADDED_PERM("group.added-perm", Color.Text.alt() + "Added permission " + Color.Value.alt() + "{0}" + Color.Text.alt() + " to group " + Color.Value.alt() + "{1}" + Color.Text.alt() + "."),
        GROUP_ADDED_PERM_SERVER("group.added-perm-server", Color.Text.alt() + "Added permission " + Color.Value.alt() + "{0}" + Color.Text.alt() + " to group " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + "."),
        GROUP_ADDED_PERM_SERVER_WORLD("group.added-perm-server-world", Color.Text.alt() + "Added permission " + Color.Value.alt() + "{0}" + Color.Text.alt() + " to group " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + " in world " + Color.Value.alt() + "{3}" + Color.Text.alt() + "."),
        GROUP_REMOVED_PERM("group.removed-perm", Color.Text.alt() + "Removed permission " + Color.Value.alt() + "{0}" + Color.Text.alt() + " from group " + Color.Value.alt() + "{1}" + Color.Text.alt() + "."),
        GROUP_REMOVED_PERM_SERVER("group.removed-perm-server", Color.Text.alt() + "Removed permission " + Color.Value.alt() + "{0}" + Color.Text.alt() + " from group " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + "."),
        GROUP_REMOVED_PERM_SERVER_WORLD("group.removed-perm-server-world", Color.Text.alt() + "Removed permission " + Color.Value.alt() + "{0}" + Color.Text.alt() + " from group " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + " in world " + Color.Value.alt() + "{3}" + Color.Text.alt() + "."),
        GROUP_ALREADY_HAS_PERM("group.already-had-perm", Color.Text.alt() + "The group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " already has the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + "."),
        GROUP_ALREADY_HAS_PERM_SERVER("group.already-had-perm-server", Color.Text.alt() + "The group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " already has the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + "."),
        GROUP_ALREADY_HAS_PERM_SERVER_WORLD("group.already-had-perm-server-world", Color.Text.alt() + "The group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " already has the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + " in world " + Color.Value.alt() + "{3}" + Color.Text.alt() + "."),
        GROUP_NEVER_HAD_PERM("group.never-had-perm", Color.Text.alt() + "The group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " never had the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + "."),
        GROUP_NEVER_HAD_PERM_SERVER("group.never-had-perm-server", Color.Text.alt() + "The group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " never had the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + "."),
        GROUP_NEVER_HAD_PERM_SERVER_WORLD("group.never-had-perm-server-world", Color.Text.alt() + "The group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " never had the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + " in world " + Color.Value.alt() + "{3}" + Color.Text.alt() + "."),
        GROUP_HAS_PERM("group.has-perm", Color.Text.alt() + "Group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " has the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + ": {2}"),
        GROUP_HAS_PERM_SERVER("group.has-perm-server", Color.Text.alt() + "Group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " has the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + ": {3}"),
        GROUP_HAS_PERM_SERVER_WORLD("group.has-perm-server-world", Color.Text.alt() + "Group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " has the permission " + Color.Value.alt() + "{1}" + Color.Text.alt() + " on server " + Color.Value.alt() + "{2}" + Color.Text.alt() + " in world " + Color.Value.alt() + "{3}" + Color.Text.alt() + ": {4}"),
        GROUP_ADDED_INHERITANCE("group.added-inheritance", Color.Text.alt() + "Added inheritance " + Color.Value.alt() + "{0}" + Color.Text.alt() + " to group " + Color.Value.alt() + "{1}" + Color.Text.alt() + "."),
        GROUP_REMOVED_INHERITANCE("group.removed-inheritance", Color.Text.alt() + "Removed inheritance " + Color.Value.alt() + "{0}" + Color.Text.alt() + " from group " + Color.Value.alt() + "{1}" + Color.Text.alt() + "."),
        GROUP_SET_RANK("group.set-rank", Color.Text.alt() + "Group rank set for group " + Color.Value.alt() + "{0}" + Color.Text.alt() + "."),
        GROUP_SET_WEIGHT("group.set-weight", Color.Text.alt() + "Group weight set for group " + Color.Value.alt() + "{0}" + Color.Text.alt() + "."),
        GROUP_SET_LADDER("group.set-ladder", Color.Text.alt() + "Group ladder set for group " + Color.Value.alt() + "{0}" + Color.Text.alt() + "."),
        GROUP_SET_DEFAULT("group.set-default", Color.Text.alt() + "Marked group " + Color.Value.alt() + "{0}" + Color.Text.alt() + " as " + Color.Value.alt() + "{1}"),
        GROUP_SET_DISPLAY("group.set-display", Color.Text.alt() + "Set display name for group " + Color.Value.alt() + "{0}" + Color.Text.alt() + "."),
        GROUP_SET_PREFIX("group.set-prefix", Color.Text.alt() + "Set prefix for group " + Color.Value.alt() + "{0}" + Color.Text.alt() + "."),
        GROUP_SET_SUFFIX("group.set-suffix", Color.Text.alt() + "Set suffix for group " + Color.Value.alt() + "{0}" + Color.Text.alt() + "."),
        //common
        DISPLAY("common.display", Color.Text.alt() + "Display: " + ChatColor.RESET.alt() + "{0}"),
        PREFIX("common.prefix", Color.Text.alt() + "Prefix: " + ChatColor.RESET.alt() + "{0}"),
        SUFFIX("common.suffix", Color.Text.alt() + "Suffix: " + ChatColor.RESET.alt() + "{0}"),
        PREFIX_FULL("common.prefix-full", Color.Text.alt() + "Full prefix: " + ChatColor.RESET.alt() + "{0}"),
        SUFFIX_FULL("common.suffix-full", Color.Text.alt() + "Full suffix: " + ChatColor.RESET.alt() + "{0}"),
        PERMISSIONS_LIST_ITEM("common.permissions-list-item", Color.Text.alt() + "- " + Color.Value.alt() + "{0}" + Color.Text.alt() + " (" + Color.Value.alt() + "{1}" + Color.Text.alt() + "{2}{3})"),
        PERMISSIONS_LIST_HEADER_PAGE("common.permissions-list-header-page", Color.Text.alt() + "Page " + Color.Value.alt() + "{0}" + Color.Text.alt() + "/" + Color.Value.alt() + "{1}" + Color.Text.alt() + ""),
        //util & parts
        OWN("general.own", "own"),
        NONE("general.none", "none"),
        DEFAULT("general.default", "default"),
        NONDEFAULT("general.nondefault", "non-default"),
        MISCONFIGURATION("general.misconfiguration", Color.Error.alt() + "Misconfiguration"),
        //extraction
        EXTRACTING("log.extraction.extracting", "extracting {0}"),
        EXTRACTION_FAILED("log.extraction.failed", "could not extract file {0}: {1}"),
        EXTRACTION_DONE("log.extraction.done", "extracted {0}"),
        //log
        PERMISSIONS_LOADING("log.permissions.loading", "loading permissions ..."),
        PERMISSIONS_LOADED("log.permissions.loaded", "permissions loaded"),
        NO_PERM_FILE("log.permissions.no-perm-file", "no permissions file found!!!"),
        LOGIN("log.login", "Login by {0}"),
        LOGIN_UUID("log.login-uuid", "Login by {0} ({1})"),
        ADDING_DEFAULT_GROUPS("log.permissions.adding-default-groups", "Adding default groups to {0}"),
        ADDING_DEFAULT_GROUPS_UUID("log.permissions.adding-default-groups-uuid", "Adding default groups to {0} ({1})"),
        //warnings
        INTRUSION_DETECTED("warning.intrusion-detected", Color.Error.alt() + "Possible intrusion detected. Sender is {0}"),
        MISCONFIG_BUNGEE_STANDALONE("warning.misconfig.bungee.standalone", "Server {0}: Received a plugin message from Bukkit/Spigot but BungeePerms is in standalone mode. Ignoring it ..."),
        MISCONFIG_BUNGEE_SERVERDEPENDEND("warning.misconfig.bungee.serverdependend", "Server {0}: Received a plugin message from Bukkit/Spigot but BungeePerms is in serverdependend mode. Ignoring it ..."),
        MISCONFIG_BUNGEE_SERVERDEPENDENDBLACKLIST("warning.misconfig.bungee.serverdependend-blacklist", "Server {0}: Received a plugin message from Bukkit/Spigot but BungeePerms is in serverdependend-blacklist mode. Ignoring it ..."),
        MISCONFIG_BUNGEE_SERVERNAME("warning.misconfig.bungee.servername", "Server {0}: The server names of the Bungeecord config and BungeePerms config do not match."),
        MISCONFIG_BUNGEE_BACKEND("warning.misconfig.bungee.backend", "Server {0}: The backend types of the BungeePerms configs do not match."),
        MISCONFIG_BUNGEE_UUIDPLAYERDB("warning.misconfig.bungee.uuidplayerdb", "Server {0}: The uuidplayerdb types of the BungeePerms configs do not match."),
        MISCONFIG_BUNGEE_USEUUID("warning.misconfig.bungee.useuuid", "Server {0}: The useuuids options of the BungeePerms configs do not match."),
        MISCONFIG_BUKKIT_STANDALONE("warning.misconfig.bukkit.standalone", "Received a plugin message from Bungeecord but BungeePerms is in standalone mode. Ignoring it ..."),
        MISCONFIG_BUKKIT_SERVERNAME("warning.misconfig.bukkit.servername", "The server names of the Bungeecord config and BungeePerms config do not match."),
        MISCONFIG_BUKKIT_BACKEND("warning.misconfig.bukkit.backend", "The backend types of the BungeePerms configs do not match."),
        MISCONFIG_BUKKIT_UUIDPLAYERDB("warning.misconfig.bukkit.uuidplayerdb", "The uuidplayerdb types of the BungeePerms configs do not match."),
        MISCONFIG_BUKKIT_USEUUID("warning.misconfig.bukkit.useuuid", "The useuuids options of the BungeePerms configs do not match."),
        MISCONFIG_BUNGEECORD_BUKKIT_CONFIG("warning.misconfig.bungee-bukkit-config", "UUIDs on Bungeecord and Bukkit/Spigot differ. Check your Spigot/Bukkit and Bungeecord config!"),
        MISCONFIG_USEUUID_NONE_UUID_DB("warning.misconfig.useuuid-none-uuiddb", "The useUUIDs option is enabled but the uuidplayerdb is set to none!"),
        //help
        HELP_WELCOME("help.welcome", "Welcomes you to BungeePerms"),
        HELP_HELP("help.help", "Shows this help"),
        HELP_RELOAD("help.reload", "Reloads the plugin"),
        HELP_DEBUG("help.debug", "En-/Disables the debug mode"),
        HELP_USERS("help.users", "Lists the users [or shows the amount]"),
        HELP_USER_INFO("help.user.info", "Shows information about the user"),
        HELP_USER_DELETE("help.user.delete", "Deletes the user"),
        HELP_USER_DISPLAY("help.user.display", "Sets the display name for the user"),
        HELP_USER_PREFIX("help.user.prefiy", "Sets the prefix name for the user"),
        HELP_USER_SUFFIX("help.user.suffix", "Sets the suffix for the user"),
        HELP_USER_ADDPERM("help.user.add-perm", "Adds a permission to the user"),
        HELP_USER_REMOVEPERM("help.user.remove-perm", "Removes a permission from a the user"),
        HELP_USER_HAS("help.user.has", "Checks if the user has the permission"),
        HELP_USER_LIST("help.user.list", "Lists the permissions of the user"),
        HELP_USER_ADDGROUP("help.user.", "Add the group to the user"),
        HELP_USER_REMOVEGROUP("help.user.remove-perm", "Removes the group from the user"),
        HELP_USER_SETGROUP("help.user.set-group", "Removes the old group in the group's ladder and adds the group to the user"),
        HELP_USER_GROUPS("help.user.groups", "Lists the groups the user is in"),
        HELP_GROUPS("help.groups", "Lists the groups"),
        HELP_GROUP_INFO("help.group.info", "Shows information about the group"),
        HELP_GROUP_USERS("help.group.users", "Lists the users of the group [or shows the amount]"),
        HELP_GROUP_CREATE("help.group.create", "Create a new group"),
        HELP_GROUP_DELETE("help.group.delete", "Deletes the group"),
        HELP_GROUP_ADDINHERIT("help.group.add-inherit", "Adds the addgroup to the group as inheritance"),
        HELP_GROUP_REMOVEINHERIT("help.group.remive-inherit", "Removes the removegroup from the group as inheritance"),
        HELP_GROUP_RANK("help.group.rank", "Sets the rank for the group"),
        HELP_GROUP_WEIGHT("help.group.weight", "Sets the weight for the group"),
        HELP_GROUP_LADDER("help.group.ladder", "Sets the ladder for the group"),
        HELP_GROUP_DEFAULT("help.group.default", "Determines whether the group is a default group or not"),
        HELP_GROUP_DISPLAY("help.group.display", "Sets the display name for the group"),
        HELP_GROUP_PREFIX("help.group.prefix", "Sets the prefix for the group"),
        HELP_GROUP_SUFFIX("help.group.suffix", "Sets the suffix for the group"),
        HELP_GROUP_ADDPERM("help.group.add-perm", "Adds a permission to the group"),
        HELP_GROUP_REMOVEPERM("help.group.remove-perm", "Removes a permission from the group"),
        HELP_GROUP_HAS("help.group.has", "Checks if the group has the permission"),
        HELP_GROUP_LIST("help.group.list", "Lists the permissions of the group"),
        HELP_PROMOTE("help.promote", "Promotes the user to the next rank"),
        HELP_DEMOTE("help.demote", "Demotes the user to the previous rank"),
        HELP_FORMAT("help.format", "Reformates the permission.yml or mysql table - " + ChatColor.RED.alt() + "BE CAREFUL"),
        HELP_CLEANUP("help.cleanup", "Cleans up the permission.yml or mysql table - " + ChatColor.RED.alt() + "!BE VERY CAREFUL! - removes a lot of players from the permissions db if configured"),
        HELP_MIGRATE_BACKEND("help.migrate.backend", "Migrates the backend or shows status - " + ChatColor.RED.alt() + "!BE CAREFUL! (MAKE A BACKUP BEFORE EXECUTING)"),
        HELP_MIGRATE_USEUUID("help.migrate.use-uuid", "Migrates backends to (not) use UUIDs or shows status - " + ChatColor.RED.alt() + "!BE CAREFUL! (MAKE A BACKUP BEFORE EXECUTING)"),
        HELP_MIGRATE_UUIDPLAYERDB("help.migrate.uuid-player-db", "Migrates UUID-player-databases or shows status - " + ChatColor.RED.alt() + "!BE CAREFUL! (MAKE A BACKUP BEFORE EXECUTING)"),
        HELP_UUID("help.uuid", "Gets the UUID of a player from database (-r: reverse; -m: ask mojang)");

        private final String configKey;
        private final String defaultValue;
    }
}
