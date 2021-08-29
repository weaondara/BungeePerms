![Logo](https://drop.cutiepie.at/d/ou923gv3vjkal7er)

# BungeePerms
a permissions plugin for BungeeCord/Velocity and Spigot/PaperSpigot

BungeePerms is a permissions plugin for BungeeCord/Velocity and Spigot/PaperSpigot. It overrides the built-in permissions systems
so you don't need BungeeCord/Spigot permissions (anymore). BungeePerms can form a network so that it's a single
system managing all permissions in your network.

**Discord**

You can ask questions directly on our Discord-Server at https://discord.gg/NaA6eQ2

**Binaries**

Dev builds of this project are available at http://ci.wea-ondara.net/job/BungeePerms/  

**Maven Repo**


For BungeePerms 4 use
```xml
<repositories>
    <repository>
        <id>bungeeperms-repo</id>
        <url>https://repo.wea-ondara.net/repository/public/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>net.alpenblock</groupId>
        <artifactId>BungeePerms</artifactId>
        <version>4.0-dev-108</version>
    </dependency>
</dependencies>
```

For BungeePerms 3 (outdated) use
```xml
<repositories>
    <repository>
        <id>bungeeperms-repo</id>
        <url>https://repo.wea-ondara.net/repository/public/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>net.alpenblock</groupId>
        <artifactId>BungeePerms</artifactId>
        <version>3.0-dev-80</version>
    </dependency>
</dependencies>
```

**Functionality:**

- One permissions system for all your servers
- Manages all permissions of your network
- Permission groups
- Timed permissions and groups
- Promote/demote functionality
- Per server and world permissions
- In-game permission check (check a user's/group's permission)
- In-game list for defined users/groups
- Complete in-game user and group management
- Group inheritances, timed inheritances
- Group ladders
- Prefixes, suffixes and display for users and groups
- Mysql and YAML support
- UUID support (optional, almost a requirement)
- Regex permissions (optional)


For more information go to the wiki https://github.com/weaondara/BungeePerms/wiki
