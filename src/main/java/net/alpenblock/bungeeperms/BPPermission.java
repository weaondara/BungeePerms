package net.alpenblock.bungeeperms;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class BPPermission implements Comparable<BPPermission>
{

    private String permission;
    private String origin;
    private boolean isGroup;
    private String server;
    private String world;
    private Date timedStart;
    private Integer timedDuration;

    @Override
    public int compareTo(BPPermission o)
    {
        int ret = permission == null && o.permission == null ? 0 : (permission == null ? -1 : (o.permission == null ? 1 : String.CASE_INSENSITIVE_ORDER.compare(permission, o.permission)));
        if (ret != 0)
            return ret;
        ret = Boolean.compare(isGroup, o.isGroup);
        if (ret != 0)
            return ret;
        ret = origin == null && o.origin == null ? 0 : (origin == null ? -1 : (o.origin == null ? 1 : String.CASE_INSENSITIVE_ORDER.compare(origin, o.origin)));
        if (ret != 0)
            return ret;
        ret = server == null && o.server == null ? 0 : (server == null ? -1 : (o.server == null ? 1 : String.CASE_INSENSITIVE_ORDER.compare(server, o.server)));
        if (ret != 0)
            return ret;
        ret = world == null && o.world == null ? 0 : (world == null ? -1 : (o.world == null ? 1 : String.CASE_INSENSITIVE_ORDER.compare(world, o.world)));
        if (ret != 0)
            return ret;
        ret = timedStart == null && o.timedStart == null ? 0 : (timedStart == null ? -1 : (o.timedStart == null ? 1 : timedStart.compareTo(o.timedStart)));
        if (ret != 0)
            return ret;
        ret = timedDuration == null && o.timedDuration == null ? 0 : (timedDuration == null ? -1 : (o.timedDuration == null ? 1 :Integer.compare(timedDuration, o.timedDuration)));
        return ret;
    }
}
