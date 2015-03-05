package net.alpenblock.bungeeperms.io;

public enum BackEndType
{

    YAML,
    MySQL,
    MySQL2;

    public static BackEndType getByName(String name)
    {
        for (BackEndType t : values())
        {
            if (t.name().equalsIgnoreCase(name))
            {
                return t;
            }
        }
        return null;
    }
}
