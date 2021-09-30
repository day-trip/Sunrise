package com.daytrip.sunrise.hack;

import com.daytrip.sunrise.SunriseClient;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.BlockPos;

import java.util.List;

public class CommandHack extends CommandBase {
    @Override
    public String getCommandName() {
        return "hack";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.hack.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if(args.length != 2) {
            throw new WrongUsageException("commands.hack.usage");
        } else {
            if(!args[1].equals("enable") && !args[1].equals("disable") && !args[1].equals("toggle")) {
                throw new WrongUsageException("commands.hack.usage");
            } else {
                Hack hack = getHack(args[0]);
                if(hack == null) {
                    throw new WrongUsageException("commands.hack.usage");
                } else {
                    if(args[1].equals("enable")) {
                        hack.setEnabled(true);
                    }
                    if(args[1].equals("disable")) {
                        hack.setEnabled(false);
                    }
                    if(args[1].equals("toggle")) {
                        hack.toggle();
                    }
                }
            }
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if(args.length == 1) {
            return getListOfStringsMatchingLastWord(args, getHacksArray());
        }
        if(args.length == 2) {
            return getListOfStringsMatchingLastWord(args, "enable", "disable", "toggle");
        }
        return null;
    }

    private String[] getHacksArray() {
        String[] array = new String[SunriseClient.hacks.size()];
        for(int i = 0; i < SunriseClient.hacks.size(); i++) {
            array[i] = SunriseClient.hacks.get(i).getId();
        }
        return array;
    }

    private Hack getHack(String name) {
        for(Hack hack : SunriseClient.hacks) {
            if(hack.getId().equals(name)) {
                return hack;
            }
        }
        return null;
    }
}
