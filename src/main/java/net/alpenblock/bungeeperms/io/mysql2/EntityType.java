package net.alpenblock.bungeeperms.io.mysql2;

public enum EntityType
{

    User(0),
    Group(1),
    Version(2);

    private int code;

    private EntityType(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }

    public static EntityType getByCode(int code)
    {
        for (EntityType et : values())
        {
            if (et.getCode() == code)
            {
                return et;
            }
        }
        return null;
    }
}
