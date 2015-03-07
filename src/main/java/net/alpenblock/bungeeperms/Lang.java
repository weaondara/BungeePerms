package net.alpenblock.bungeeperms;

import java.util.EnumMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Lang
{

    private static final Map<MessageType, String> map = new EnumMap<>(MessageType.class);

    public static void load(String file)
    {
        Config langconf = new Config(file);
        langconf.load();

        map.clear();
        for (MessageType mt : MessageType.values())
        {
            map.put(mt, langconf.getString(mt.getConfigKey(), mt.getDefaultValue()));
        }
    }

    public static String translate(MessageType type, Object... vars)
    {
        String s = map.get(type);
        if (s == null)
        {
            s = type.getDefaultValue();
        }
        s = Statics.format(s, vars);
        return s.replaceAll("&", "§");
    }

    @AllArgsConstructor
    @Getter
    public static enum MessageType
    {

        BUNGEEPERMS("bungeeperms", ChatColor.GOLD.alt() + "Welcome to BungeePerms, a BungeeCord permissions plugin"),
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
        NO_USERS_FOUND("user.nu-users-found", Color.Text.alt() + "No players found!"),
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
        DISPLAY("common.display", Color.Text.alt() + "Dislay: " + ChatColor.RESET.alt() + "{0}"),
        PREFIX("common.prefix", Color.Text.alt() + "Prefix: " + ChatColor.RESET.alt() + "{0}"),
        SUFFIX("common.suffix", Color.Text.alt() + "Suffix: " + ChatColor.RESET.alt() + "{0}"),
        PERMISSIONS_LIST_ITEM("common.permissions-list-item", Color.Text.alt() + "- " + Color.Value.alt() + "{0}" + Color.Text.alt() + " (" + Color.Value.alt() + "{1}" + Color.Text.alt() + "{2}{3})"),
        //util & partss
        OWN("general.own", "own"),
        NONE("general.none", "none"),
        DEFAULT("general.default", "default"),
        NONDEFAULT("general.nondefault", "non-default"),
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
        INTRUSTION_DETECTED("log.intrusion-detected", Color.Error + "Possible intrusion detected. Sender is {0}");

        private final String configKey;
        private final String defaultValue;
    }
}
