package org.lins.mmmjjkx.rykenslimefuncustomizer;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lins.mmmjjkx.rykenslimefuncustomizer.commands.MainCommand;
import org.lins.mmmjjkx.rykenslimefuncustomizer.listeners.BlockListener;
import org.lins.mmmjjkx.rykenslimefuncustomizer.listeners.ScriptableEventListener;
import org.lins.mmmjjkx.rykenslimefuncustomizer.listeners.SingleItemRecipeGuideListener;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

public final class RykenSlimefunCustomizer extends JavaPlugin implements SlimefunAddon {
    private static boolean runtime = false;

    public static RykenSlimefunCustomizer INSTANCE;
    public static ProjectAddonManager addonManager;

    @Override
    public void onLoad() {
        setupLibraries();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");

        INSTANCE = this;
        addonManager = new ProjectAddonManager();

        CommonUtils.completeFile("config.yml");

        if (getConfig().getBoolean("saveExample", false)) {
            saveExample();
        }

        Objects.requireNonNull(getCommand("rykenslimecustomizer")).setExecutor(new MainCommand());

        addonManager.setup(this);

        new BlockListener();
        new SingleItemRecipeGuideListener();
        new ScriptableEventListener();

        ExceptionHandler.info("RykenSlimeCustomizer loaded successfully！");

        /*
        if (getConfig().getBoolean("pluginUpdate")
                && getDescription().getVersion().startsWith("b")
                && getServer().getPluginManager().isPluginEnabled("GuizhanLibPlugin")) {
            GuizhanUpdater.start(this, getFile(), "SlimefunReloadingProject", "RykenSlimeCustomizer", "main");
        }

         */

        getServer().getScheduler().runTaskLater(this, () -> runtime = true, 1);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("RykenSlimeCustomizer disabled!");
    }

    public static void reload() {
        INSTANCE.reloadConfig();
        addonManager.reload(INSTANCE);

        if (INSTANCE.getConfig().getBoolean("saveExample")) {
            saveExample();
        }
    }

    @NotNull @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/SlimefunReloadingProject/RykenSlimeCustomizer/issues";
    }

    private void setupLibraries() {
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this);
        libraryManager.addMavenCentral();
        Library byteBuddy = Library.builder()
                .groupId("net{}bytebuddy")
                .artifactId("byte-buddy")
                .version("1.14.18")
                .build();
        Library javet = Library.builder()
                .groupId("com{}caoccao{}javet")
                .artifactId("javet")
                .version("3.1.5")
                .build();

        libraryManager.loadLibrary(byteBuddy);
        libraryManager.loadLibrary(javet);
    }

    public static boolean allowUpdate(String prjId) {
        if (runtime) return false;

        return INSTANCE.getConfig().getBoolean("update.auto")
                && !INSTANCE.getConfig().getStringList("update.blocks").contains(prjId);
    }

    public static void saveExample() {
        String head = "addons/example/info.yml";

        String filePath = new File(INSTANCE.getDataFolder(), head).getAbsolutePath();
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            INSTANCE.saveResource(head, true);
        }
    }
}
