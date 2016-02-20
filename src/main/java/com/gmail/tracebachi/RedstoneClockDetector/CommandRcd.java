package com.gmail.tracebachi.RedstoneClockDetector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.bukkit.ChatColor.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 2/14/16.
 */
public class CommandRcd implements CommandExecutor, Listener
{
    private boolean listenerEnabled;
    private int taskId = -1;
    private List<AreaCounter> counterList = new ArrayList<>(1024);
    private Main plugin;

    public CommandRcd(Main plugin)
    {
        this.plugin = plugin;

        plugin.getCommand("rcd").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void shutdown()
    {
        plugin.getCommand("rcd").setExecutor(null);
        HandlerList.unregisterAll(this);

        counterList.clear();
        counterList = null;
        plugin = null;
    }

    @EventHandler
    public void onRedstoneEvent(BlockRedstoneEvent event)
    {
        if(listenerEnabled)
        {
            Location location = event.getBlock().getLocation();
            AreaCounter counter = new AreaCounter(location.getX(), location.getY(), location.getZ());
            int counterIndex = counterList.indexOf(counter);

            if(counterIndex >= 0)
            {
                counterList.get(counterIndex).increment();
            }
            else
            {
                counterList.add(counter);
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if(!sender.hasPermission("rcd"))
        {
            sender.sendMessage(RED + "You do not have permission to use RedstoneClockDetector.");
            return true;
        }

        if(args.length >= 2 && args[0].equalsIgnoreCase("start"))
        {
            Integer ticks = parseInt(args[1]);

            if(ticks == null || ticks < 1)
            {
                sender.sendMessage(RED + "Invalid tick amount: " + args[1]);
            }
            else
            {
                handleStart(ticks);
            }
        }
        else if(args.length >= 2 && args[0].equalsIgnoreCase("tp"))
        {
            if(!(sender instanceof Player))
            {
                sender.sendMessage(RED + "RCD teleport is only available for players.");
                return true;
            }

            Integer number = parseInt(args[1]);

            if(number == null || number < 1)
            {
                sender.sendMessage(RED + "Invalid list number: " + args[1]);
            }
            else if(number > counterList.size())
            {
                sender.sendMessage(RED + "Number must be between 1 ~ " + Math.max(1, counterList.size()));
            }
            else
            {
                handleTp((Player) sender, number);
            }
        }
        else if(args.length >= 1 && args[0].equalsIgnoreCase("list"))
        {
            Integer displaySize = 3;

            if(args.length >= 2)
            {
                displaySize = parseInt(args[1]);

                if(displaySize == null || displaySize < 1)
                {
                    sender.sendMessage(RED + "Invalid display size: " + args[1]);
                    return true;
                }
            }

            handleList(sender, displaySize);
        }
        else if(args.length >= 1 && args[0].equalsIgnoreCase("reload"))
        {
            plugin.reloadLengths();
        }
        else
        {
            sender.sendMessage(GRAY + "/rcd start <time in ticks>");
            sender.sendMessage(GRAY + "/rcd tp <number on list>");
            sender.sendMessage(GRAY + "/rcd list [display size]");
            sender.sendMessage(GRAY + "/rcd reload");
        }
        return true;
    }

    private void handleStart(int ticks)
    {
        if(taskId != -1)
        {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }

        counterList.clear();
        listenerEnabled = true;

        Bukkit.broadcast(GREEN + "RCD enabled for " + ticks + " ticks.", "rcd");

        taskId = Bukkit.getScheduler().runTaskLater(plugin, () ->
        {
            listenerEnabled = false;
            Bukkit.broadcast(GREEN + "RCD sample of " + ticks + " ticks complete.", "rcd");
        }, ticks).getTaskId();
    }

    private void handleList(CommandSender sender, int displaySize)
    {
        Collections.sort(counterList, (o1, o2) -> o2.getCount() - o1.getCount());

        sender.sendMessage(GREEN + "Report for redstone events:");

        for(int i = 0; i < displaySize && i < counterList.size(); i++)
        {
            sender.sendMessage(GRAY + " #" + WHITE + (i + 1) +
                GRAY + " : " +
                counterList.get(i).toString());
        }
    }

    private void handleTp(Player sender, int number)
    {
        AreaCounter counter = counterList.get(number - 1);
        Location location = counter.getLocation(sender.getWorld());

        sender.sendMessage(GREEN + "Teleporting to #" + number);
        sender.teleport(location);
    }

    private Integer parseInt(String src)
    {
        try
        {
            return Integer.parseInt(src);
        }
        catch(NumberFormatException e)
        {
            return null;
        }
    }
}
