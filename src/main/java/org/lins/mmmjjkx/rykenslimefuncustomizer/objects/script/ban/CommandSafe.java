package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ban;

import java.util.List;

public class CommandSafe {
    private static final List<String> badCommands = List.of(
            "stop",
            "restart",
            "op",
            "deop",
            "whitelist",
            "ban-ip",
            "banlist",
            "pardon",
            "kick",
            "ban",
            "pardon-ip",
            "save-all",
            "unban",
            "luckperms",
            "lp",
            "cmi:ban",
            "cmi:pardon",
            "cmi:banlist",
            "cmi:unban",
            "essentials:ban",
            "essentials:pardon",
            "essentials:banlist",
            "essentials:unban",
            "essentials:mute",
            "essentials:unmute",
            "essentials:jail",
            "essentials:unjail",
            "cmi:jail",
            "cmi:unjail",
            "cmi:mute",
            "cmi:unmute",
            "essentials:sudo",
            "cmi:sudo");

    public static boolean isBadCommand(String command) {
        return badCommands.contains(command.toLowerCase());
    }
}
