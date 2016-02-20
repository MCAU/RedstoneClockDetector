package com.gmail.tracebachi.RedstoneClockDetector;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 2/14/16.
 */
public class Main extends JavaPlugin
{
    private static double xLength;
    private static double yLength;
    private static double zLength;

    private CommandRcd commandRcd;

    @Override
    public void onLoad()
    {
        saveDefaultConfig();
    }

    @Override
    public void onEnable()
    {
        reloadLengths();
        commandRcd = new CommandRcd(this);
    }

    @Override
    public void onDisable()
    {
        commandRcd.shutdown();
        commandRcd = null;
    }

    public void reloadLengths()
    {
        reloadConfig();
        xLength = getConfig().getDouble("xLength", 24.0D);
        yLength = getConfig().getDouble("yLength", 24.0D);
        zLength = getConfig().getDouble("zLength", 24.0D);
    }

    public static double xLength()
    {
        return xLength;
    }

    public static double yLength()
    {
        return yLength;
    }

    public static double zLength()
    {
        return zLength;
    }
}
