package com.gmail.tracebachi.RedstoneClockDetector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
import java.util.HashMap;
import java.util.List;

import static org.bukkit.ChatColor.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 2/14/16.
 */
public class CommandRcd implements CommandExecutor, Listener
{
    private boolean listenerEnabled;
    private int taskId = -1;
    private HashMap<String, List<AreaCounter>> counterListMap = new HashMap<>(3);
    private RedstoneClockDetector plugin;

    public CommandRcd(RedstoneClockDetector plugin)
    {
        this.plugin = plugin;

        plugin.getCommand("rcd").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void shutdown()
    {
        plugin.getCommand("rcd").setExecutor(null);
        HandlerList.unregisterAll(this);

        listenerEnabled = false;
        counterListMap.clear();
        counterListMap = null;
        plugin = null;
    }

    @EventHandler
    public void onRedstoneEvent(BlockRedstoneEvent event)
    {
        if(listenerEnabled)
        {
            Location location = event.getBlock().getLocation();
            World world = location.getWorld();
            List<AreaCounter> counterList = counterListMap.get(world.getName());

            if(counterList == null)
            {
                counterList = new ArrayList<>(512);
                counterListMap.put(world.getName(), counterList);
            }

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
            sender.sendMessage(RED + "You do not have permission to use RedstoneClockDetector");
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
                handleStart(sender, ticks);
            }
        }
        else if(args.length >= 2 && args[0].equalsIgnoreCase("tp"))
        {
            if(!(sender instanceof Player))
            {
                sender.sendMessage(RED + "RCD teleport is only available for players");
                return true;
            }

            Player player = (Player) sender;
            Integer number = parseInt(args[1]);
            World world = player.getWorld();

            if(args.length >= 3)
            {
                world = Bukkit.getWorld(args[2]);
                if(world == null)
                {
                    sender.sendMessage(RED + args[2] + " is not a valid world");
                    return true;
                }
            }

            List<AreaCounter> counterList = counterListMap.getOrDefault(
                world.getName(),
                Collections.emptyList());

            if(counterList.isEmpty())
            {
                player.sendMessage(RED + "There are no redstone areas to teleport to in " +
                    WHITE + world.getName());
            }
            else if(number == null || number < 1)
            {
                player.sendMessage(RED + "Invalid list number: " + args[1]);
            }
            else if(number > counterList.size())
            {
                player.sendMessage(RED + "Number must be between 1 ~ " +
                    Math.max(1, counterList.size()));
            }
            else
            {
                handleTp(player, world, counterList, number);
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

            sender.sendMessage(GREEN + "Displaying RCD list for all worlds ...");

            for(String key : counterListMap.keySet())
            {
                handleList(sender, key, displaySize);
            }
        }
        else if(args.length >= 1 && args[0].equalsIgnoreCase("reload"))
        {
            plugin.reloadLengths();
        }
        else
        {
            sender.sendMessage(GRAY + "/rcd start <time in ticks>");
            sender.sendMessage(GRAY + "/rcd tp <number on list> [world]");
            sender.sendMessage(GRAY + "/rcd list [display size]");
            sender.sendMessage(GRAY + "/rcd reload");
        }
        return true;
    }

    private void handleStart(CommandSender sender, int ticks)
    {
        if(taskId != -1)
        {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }

        counterListMap.clear();
        listenerEnabled = true;

        String startMessage = GREEN + "RCD enabled for " + ticks + " ticks";
        if(RedstoneClockDetector.shouldBroadcastRcdStart())
        {
            Bukkit.broadcast(startMessage, "rcd");
        }
        else
        {
            sender.sendMessage(startMessage);
        }

        String senderName = sender.getName();
        String completionMessage = GREEN + "RCD sample of " + ticks + " ticks complete.";
        taskId = Bukkit.getScheduler().runTaskLater(plugin, () ->
        {
            listenerEnabled = false;

            if(RedstoneClockDetector.shouldBroadcastRcdStart())
            {
                Bukkit.broadcast(completionMessage, "rcd");
                return;
            }

            if(senderName.equalsIgnoreCase("console"))
            {
                Bukkit.getConsoleSender().sendMessage(completionMessage);
                return;
            }

            Player player = Bukkit.getPlayerExact(senderName);
            if(player != null)
            {
                sender.sendMessage(completionMessage);
            }
        }, ticks).getTaskId();
    }

    private void handleList(CommandSender sender, String worldName, int displaySize)
    {
        List<AreaCounter> counterList = counterListMap.getOrDefault(
            worldName,
            Collections.emptyList());

        Collections.sort(counterList, (o1, o2) -> o2.getCount() - o1.getCount());

        sender.sendMessage(GREEN + "Report for # of redstone events on " +
            WHITE + worldName +
            GREEN + ":");

        for(int i = 0; i < displaySize && i < counterList.size(); i++)
        {
            sender.sendMessage(GRAY + " #" + WHITE + (i + 1) +
                GRAY + " : " +
                counterList.get(i).toString());
        }
    }

    private void handleTp(Player sender, World world, List<AreaCounter> counterList, int number)
    {
        AreaCounter counter = counterList.get(number - 1);
        Location location = counter.getLocation(world);

        sender.sendMessage(GREEN + "Teleporting to #" + number + " in " + WHITE + world.getName());
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
