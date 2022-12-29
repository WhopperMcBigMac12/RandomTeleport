package com.shanebeestudios.rtp.permission;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

public class Permissions {

    public static final Permissions UPDATE_CHECK = get("update.check", false);
    public static final Permissions COMMAND_RTP_OTHER = get("command.rtp.other", false);
    public static final Permissions COMMAND_RTP_WORLD = get("command.rtp.world", false);

    private static Permissions get(String permission, boolean defaultPerm) {
        String perm = "randomteleport." + permission;
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (pluginManager.getPermission(perm) == null) {
            Permission bukkitPerm = new Permission(perm, PermissionDefault.OP);
            bukkitPerm.setDefault(defaultPerm ? PermissionDefault.TRUE : PermissionDefault.OP);
            pluginManager.addPermission(bukkitPerm);
        }
        return new Permissions(perm);
    }

    String permission;

    public Permissions(String permission) {
        this.permission = permission;
    }

    public boolean check(Permissible permissible) {
        return permissible.hasPermission(permission);
    }

}
