package com.gmail.tracebachi.RedstoneClockDetector;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 2/14/16.
 */
public class AreaCounter
{
    private final double x;
    private final double y;
    private final double z;

    private int count;

    public AreaCounter(double x, double y, double z)
    {
        this(x, y, z, 1);
    }

    public AreaCounter(double x, double y, double z, int count)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.count = count;
    }

    public int getCount()
    {
        return count;
    }

    public int increment()
    {
        return (count += 1);
    }

    public Location getLocation(World world)
    {
        return new Location(world, ((int) x) + 0.5, ((int) y) + 0.5, ((int) z) + 0.5);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof AreaCounter)
        {
            AreaCounter other = (AreaCounter) obj;

            return Math.abs(x - other.x) <= RedstoneClockDetector.xLength() &&
                Math.abs(y - other.y) <= RedstoneClockDetector.yLength() &&
                Math.abs(z - other.z) <= RedstoneClockDetector.zLength();
        }
        return false;
    }

    @Override
    public Object clone()
    {
        return new AreaCounter(x, y, z, count);
    }

    @Override
    public String toString()
    {
        return count + " near ( " + (int) x + " , " + (int) y + " , " + (int) z + " )";
    }
}
