package com.gmail.nossr50.commands.party.teleport;

import com.gmail.nossr50.datatypes.party.PartyTeleportRecord;
import com.gmail.nossr50.mcMMO;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PtpAcceptCommand implements CommandExecutor {

    private final mcMMO pluginRef;

    public PtpAcceptCommand(mcMMO pluginRef) {
        this.pluginRef = pluginRef;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!pluginRef.getPermissionTools().partyTeleportAccept(sender)) {
            sender.sendMessage(command.getPermissionMessage());
            return true;
        }

        if (pluginRef.getUserManager().getPlayer((Player) sender) == null) {
            sender.sendMessage(pluginRef.getLocaleManager().getString("Profile.PendingLoad"));
            return true;
        }

        Player player = (Player) sender;
        PartyTeleportRecord ptpRecord = pluginRef.getUserManager().getPlayer(player).getPartyTeleportRecord();

        if (!ptpRecord.hasRequest()) {
            player.sendMessage(pluginRef.getLocaleManager().getString("Commands.ptp.NoRequests"));
            return true;
        }

        if (pluginRef.getSkillTools().cooldownExpired(ptpRecord.getTimeout(), pluginRef.getConfigManager().getConfigParty().getPTP().getPtpRequestTimeout())) {
            ptpRecord.removeRequest();
            player.sendMessage(pluginRef.getLocaleManager().getString("Commands.ptp.RequestExpired"));
            return true;
        }

        Player target = ptpRecord.getRequestor();
        ptpRecord.removeRequest();

        if (!pluginRef.getPartyManager().canTeleport(sender, player, target.getName())) {
            return true;
        }

        if (pluginRef.getConfigManager().getConfigParty().getPTP().isPtpWorldBasedPermissions()) {
            World targetWorld = target.getWorld();
            World playerWorld = player.getWorld();

            if (!pluginRef.getPermissionTools().partyTeleportAllWorlds(target)) {
                if (!pluginRef.getPermissionTools().partyTeleportWorld(target, targetWorld)) {
                    target.sendMessage(pluginRef.getLocaleManager().getString("Commands.ptp.NoWorldPermissions", targetWorld.getName()));
                    return true;
                } else if (targetWorld != playerWorld && !pluginRef.getPermissionTools().partyTeleportWorld(target, playerWorld)) {
                    target.sendMessage(pluginRef.getLocaleManager().getString("Commands.ptp.NoWorldPermissions", playerWorld.getName()));
                    return true;
                }
            }
        }

        pluginRef.getPartyManager().handleTeleportWarmup(target, player);
        return true;
    }
}