package net.alpenblock.bungeeperms.io;

public enum UUIDPlayerDBType
{

    None,
    YAML,
    MySQL;

    public static UUIDPlayerDBType getByName(String name)
    {
        for (UUIDPlayerDBType t : values())
        {
            if (t.name().equalsIgnoreCase(name))
            {
                return t;
            }
        }
        return null;
    }
}
