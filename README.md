# BungeePerms
A permissions plugin for BungeeCord.

BungeePerms is a permissions plugin for BungeeCord and Spigot. It overrides the built-in permissions systems
so you don't need BungeeCord/Spigot permissions (anymore). BungeePerms can form a network so that it's a single
system managing all permissions in your network.

**Binaries**

Dev builds of this project are available at http://ci.wea-ondara.net/job/BungeePerms/  

**Maven Repo**

```xml
<repositories>
    <repository>
        <id>bungeeperms-repo</id>
        <url>http://repo.wea-ondara.net/repository/public/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>net.alpenblock</groupId>
        <artifactId>BungeePerms</artifactId>
        <version>3.0-dev-47</version>
    </dependency>
</dependencies>
```


**Functionality:**

- permission groups  
- extra permissions despite the groups permissions for different users  
- promote/demote functionality  
- in-game permission check (check a user's/group's permission)  
- in-game list for defined users/groups  
- complete in-game user and group management - NO MORE FILE EDIT REQUIRED  
- reload function to reload permissions from the permissions file/table  
- group inheritances  
- group ladders  
- prefixes, suffixes and group display names for each group  
- MySQL support  
- per-server-and-world permissions (world perms only work with BungeePermsBukkit)  
- permissions reload on-the-fly  
- uuid support  
- regex permissions  


**Permissions:**

`bungeeperms.help` - Help command  
`bungeeperms.reload` - Reload command  
`bungeeperms.users` - For listing defined users  
`bungeeperms.user.info` - For showing info of a user  
`bungeeperms.user.delete` - For deleting a user  
`bungeeperms.user.perms.add` - For adding permissions to a user  
`bungeeperms.user.perms.remove` - For removing permissions from a user  
`bungeeperms.user.perms.has` - For checking permissions of a user  
`bungeeperms.user.perms.list` - For listing the permissions of a user  
`bungeeperms.user.group.add` - For adding a group to a user  
`bungeeperms.user.group.remove` - For removing a group from a user  
`bungeeperms.user.group.set` - For setting a group as the main group of a user  
`bungeeperms.user.groups` - For listing a user's groups  
`bungeeperms.groups` - For listing defined groups  
`bungeeperms.group.info` - For showing info of a group  
`bungeeperms.group.create` - For creating a group  
`bungeeperms.group.delete` - For deleting a group  
`bungeeperms.group.inheritances.add` - For adding an inheritance to a group  
`bungeeperms.group.inheritances.remove` - For removing an inheritance of a group  
`bungeeperms.group.rank` - For setting the rank to a group  
`bungeeperms.group.default` - For determining whether the group is a default group  
`bungeeperms.group.display` - For setting a display name to a group  
`bungeeperms.group.prefix` - For setting a prefix to a group  
`bungeeperms.group.suffix` - For setting a suffix to a group  
`bungeeperms.group.perms.add` - For adding permissions to a group  
`bungeeperms.group.perms.remove` - For removeing permissions from a user  
`bungeeperms.group.perms.has` - For checking permissions of a user  
`bungeeperms.group.perms.list` - For listing permissions of a user  
`bungeeperms.promote` - For promoting a user  
`bungeeperms.demote` - For demoting a user  
`bungeeperms.cleanup` - For cleanup  
`bungeeperms.format` - For formatting  
`bungeeperms.backend` - For showing the currently used backend or changing it  
`bungeeperms.migrate` - For migrating backend, uuid use and uuid-player database  
`bungeeperms.uuid` - For uuid command  




**Commands:**

`/bungeeperms` - Welcomes you to BungeePerms  
`/bungeeperms help` - Shows the help  
`/bungeeperms reload` - Reloads the permissions  
`/bungeeperms users` - Lists the users (add '-c' for counting)  
`/bungeeperms user <username/uuid> add <permission> [server] [world]` - Adds a permission to the given user  
`/bungeeperms user <username/uuid> remove <permission> [server] [world]` - Remove a permission from the given user  
`/bungeeperms user <username/uuid> has <permission> [server] [world]` - Checks if the given user has the given permission  
`/bungeeperms user <username/uuid> list` - Lists the permissions of the given user  
`/bungeeperms user <username/uuid> groups` - Lists the groups the given user is in  
`/bungeeperms user <username/uuid> info` - Shows the group and other relevant user information  
`/bungeeperms user <username/uuid> delete` - Deletes a user  
`/bungeeperms user <username/uuid> addgroup <groupname>` - Adds a group to a user  
`/bungeeperms user <username/uuid> removegroup <groupname>` - Removes a group from a user  
`/bungeeperms user <username/uuid> setgroup <groupname>` - Sets the main group for a user  

`/bungeeperms groups` - Lists the groups  
`/bungeeperms group <groupname> add <permission> [server] [world]` - Adds a permission to the given group  
`/bungeeperms group <groupname> remove <permission> [server] [world]` - Removes a permission from the given group  
`/bungeeperms group <groupname> has <permission> [server] [world]` - Checks if the given group name has the given permission  
`/bungeeperms group <groupname> list` - Lists the permissions of the given group  
`/bungeeperms group <groupname> info` - Shows info to a group  
`/bungeeperms group <groupname> create` - Creates a new group  
`/bungeeperms group <groupname> delete` - Deletes a group  
`/bungeeperms group <groupname> addinherit <group>` - Adds an inheritance to a group  
`/bungeeperms group <groupname> removeinherit <group>` - Removes an inheritance from a group  
`/bungeeperms group <groupname> rank <new rank>` - Sets the rank for a group  
`/bungeeperms group <groupname> ladder <new ladder>` - Sets the ladderfor a group  
`/bungeeperms group <groupname> default <true|false>` - Determines whether the group is default  
`/bungeeperms group <groupname> display <displayname>` - Set the displayname for the group  
`/bungeeperms group <groupname> prefix <prefix>` - Sets the prefix for the group  
`/bungeeperms group <groupname> suffix <suffix>` - Sets the suffix for the group  

`/bungeeperms promote <username/uuid> [ladder]` - Promotes the given user to the next rank [in the give ladder]  
`/bungeeperms demote <username/uuid> [ladder]` - Demotes the given user to the previous rank [in the give ladder]  
`/bungeeperms format` - Reformates the permission.yml or mysql table - BE CAREFUL  
`/bungeeperms cleanup` - Cleans up the permission.yml or mysql table - !BE VERY CAREFUL! - removes a lot of players from the permissions.yml if configured  
`/bungeeperms migrate backend [yaml|mysql|mysql2]` - Shows the used permissions database (file or mysql table) [or migrates to the given database] - BungeePerms needs a mysql account on your server and general table permissions  
`/bungeeperms migrate useuuid [true|false]` - Shows whether uuids are used for player identification [or migrates the database]  
`/bungeeperms migrate uuidplayerdb [none|yaml|mysql]` - Shows the used uuid-player database (none, file or mysql table) [or migrates to the given database]  



**The permissions.yml**

The structure of the permissions file is not very complicated. This should be familiar for PEX (or other similar permissions plugins for bukkit/spigot) users. The basic structure should look like this:
```
groups:
  default:
    default: true
    inheritances: []
    permissions:
    - a.permission.granted.to.default
    - a. second.permission
    rank: 1000
  mod:
    default: false
    inheritances:
    - Default
    permissions:
    - bungeeperms.promote
    - bungeeperms.demote
    - bungeeperms.user.groups
    rank: 500
  admin:
    default: false
    inheritances:
    - Default
    - mod
    permissions:
    - bungeecord.command.*
    - bungeeperms.*
    rank: 1
users:
  wea_ondara:
    groups:
    - admin
    permissions: []
    met_me:
    groups:
    - Default
    permissions:
    - an.extra.permission.here
    - -a.permission.which.this.user.should.not.have
version: 1
```


**Group nodes:**

- default: if this rank is a default rank (automatically added to user on first join)  
- inheritances: list of inherited groups (also recursive)  
- permissions: the permissions of this group  
- rank: the lower the number the higher the rank  
- weight: the weight of a group (used for main group detection); order same as rank property  
- display: the display text of a group  
- prefix: the prefix of a group  
- suffix: the suffix of a group  

User nodes:

- groups: the groups the player is in.
- permissions: the extra permissions of the user
