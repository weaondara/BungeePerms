# BungeePerms
a permissions plugin for BungeeCord

BungeePerms is a permissions plugin for BungeeCord and Spigot. It overrides the built-in permissions systems
so you don't need BungeeCord/Spigot permissions (anymore). BungeePerms can form a network so that it's a single
system managing all permissions in your network.

**Binaries**

Dev builds of this project are available at http://ci.wea-ondara.net/job/BungeePerms/  

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
- mysql support  
- per-server-and-world permissions (world perms only work with BungeePermsBukkit)  
- permissions reload on-the-fly  
- uuid support  
- regex permissions  


**Permissions:**

`bungeeperms.help` - help command  
`bungeeperms.reload` - reload command  
`bungeeperms.users` - for listing defined users  
`bungeeperms.user.info` - for showing info of a user  
`bungeeperms.user.delete` - for deleting a user  
`bungeeperms.user.perms.add` - for adding permissions to a user  
`bungeeperms.user.perms.remove` - for removing permissions from a user  
`bungeeperms.user.perms.has` - for checking permissions of a user  
`bungeeperms.user.perms.list` - for listing the permissions of a user  
`bungeeperms.user.group.add` - for adding a group to a user  
`bungeeperms.user.group.remove` - for removing a group from a user  
`bungeeperms.user.group.set` - for setting a group as the main group of a user  
`bungeeperms.user.groups` - for listing a user's groups  
`bungeeperms.groups` - for listing defined groups  
`bungeeperms.group.info` - for showing info of a group  
`bungeeperms.group.create` - for creating a group  
`bungeeperms.group.delete` - for deleting a group  
`bungeeperms.group.inheritances.add` - for adding an inheritance to a group  
`bungeeperms.group.inheritances.remove` - for removing an inheritance of a group  
`bungeeperms.group.rank` - for setting the rank to a group  
`bungeeperms.group.default` - for determining whether the group is a default group  
`bungeeperms.group.display` - for setting a display name to a group  
`bungeeperms.group.prefix` - for setting a prefix to a group  
`bungeeperms.group.suffix` - for setting a suffix to a group  
`bungeeperms.group.perms.add` - for adding permissions to a group  
`bungeeperms.group.perms.remove` - for removeing permissions from a user  
`bungeeperms.group.perms.has` - for checking permissions of a user  
`bungeeperms.group.perms.list` - for listing permissions of a user  
`bungeeperms.promote` - for promoting a user  
`bungeeperms.demote` - for demoting a user  
`bungeeperms.cleanup` - for cleanup  
`bungeeperms.format` - for formatting  
`bungeeperms.backend` - for showing the currently used backend or changing it  
`bungeeperms.migrate` - for migrating backend, uuid use and uuid-player database  
`bungeeperms.uuid` - for uuid command  




**Commands:**

`/bungeeperms` - Welcomes you to BungeePerms  
`/bungeeperms help` - Shows the help  
`/bungeeperms reload` - Reloads the permissions  
`/bungeeperms users` - Lists the users (add '-c' for counting)  
`/bungeeperms user <username/uuid> add <permission> [server] [world]` - adds a permission to the given user  
`/bungeeperms user <username/uuid> remove <permission> [server] [world]` - remove a permission from the given user  
`/bungeeperms user <username/uuid> has <permission> [server] [world]` - checks if the given user has the given permission  
`/bungeeperms user <username/uuid> list` - lists the permissions of the given user  
`/bungeeperms user <username/uuid> groups` - lists the groups the given user is in  
`/bungeeperms user <username/uuid> info` - shows group, .. info  
`/bungeeperms user <username/uuid> delete` - deletes a user  
`/bungeeperms user <username/uuid> addgroup <groupname>` - add a group to a user  
`/bungeeperms user <username/uuid> removegroup <groupname>` - remove a group from a user  
`/bungeeperms user <username/uuid> setgroup <groupname>` - sets the main group for a user  

`/bungeeperms groups` - Lists the groups  
`/bungeeperms group <groupname> add <permission> [server] [world]` - adds a permission to the given group  
`/bungeeperms group <groupname> remove <permission> [server] [world]` - remove a permission from the given group  
`/bungeeperms group <groupname> has <permission> [server] [world]` - checks if the given groupname has the given permission  
`/bungeeperms group <groupname> list` - lists the permissions of the given group  
`/bungeeperms group <groupname> info` - shows info to a group  
`/bungeeperms group <groupname> create` - creates a new group  
`/bungeeperms group <groupname> delete` - deletes a group  
`/bungeeperms group <groupname> addinherit <group>` - adds an inheritance to a group  
`/bungeeperms group <groupname> removeinherit <group>` - removes an inheritance from a group  
`/bungeeperms group <groupname> rank <new rank>` - sets the rank for a group  
`/bungeeperms group <groupname> ladder <new ladder>` - sets the ladderfor a group  
`/bungeeperms group <groupname> default <true|false>` - determines whether the group is default  
`/bungeeperms group <groupname> display <displayname>` - set the displayname for the group  
`/bungeeperms group <groupname> prefix <prefix>` - sets the prefix for the group  
`/bungeeperms group <groupname> suffix <suffix>` - sets the suffix for the group  

`/bungeeperms promote <username/uuid> [ladder]` - promotes the given user to the next rank [in the give ladder]  
`/bungeeperms demote <username/uuid> [ladder]` - demotes the given user to the previous rank [in the give ladder]  
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
    - a. second.permission​
    rank: 1000​
  mod:
    default: false
    inheritances:
    - default​
    permissions:
    - bungeeperms.promote
    - bungeeperms.demote
    - bungeeperms.user.groups​
    rank: 500​
  admin:
    default: false
    inheritances:
    - default
    - mod​
    permissions:
    - bungeecord.command.*
    - bungeeperms.*​
    rank: 1​
users:
  wea_ondara:
    groups:
    - admin​
    permissions: []​
    met_me:
    groups:
    - default​
    permissions:
    - an.extra.permission.here
    - -a.permission.which.this.user.should.not.have​
version: 1
```


**Group nodes:**

- default: if this rank is a default rank (automatically added to user on first join)  
- inharitances: list of inharitated groups (also recursive)  
- permissions: the permissions of this group  
- rank: the lower the number the higher the rank  
- weigt: the weight of a group (used for maingroup detection); order same as rank property  
- display: the display text of a group  
- prefix: the prefix of a group  
- suffix: the suffix of a group  

User nodes:

- groups: the groups the player is in.
- permissions: the extra permissions of the user
