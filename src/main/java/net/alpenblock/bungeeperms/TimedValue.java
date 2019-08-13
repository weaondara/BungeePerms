package net.alpenblock.bungeeperms;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class TimedValue<T extends Comparable<T>> implements Comparable<TimedValue<T>>
{

    private T value;
    private Date start;
    private int duration;

    @Override
    public int compareTo(TimedValue<T> o)
    {
        return value.compareTo(o.value);
    }
}
