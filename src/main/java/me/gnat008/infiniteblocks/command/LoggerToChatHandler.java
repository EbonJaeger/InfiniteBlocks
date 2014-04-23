package me.gnat008.infiniteblocks.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by Gnat008 on 4/23/2014.
 */
public class LoggerToChatHandler extends Handler {

    private CommandSender player;

    public LoggerToChatHandler(CommandSender player) {
        this.player = player;
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(LogRecord record) {
        player.sendMessage(ChatColor.GRAY + record.getLevel().getName() + ": " + ChatColor.WHITE + record.getMessage());
    }
}
