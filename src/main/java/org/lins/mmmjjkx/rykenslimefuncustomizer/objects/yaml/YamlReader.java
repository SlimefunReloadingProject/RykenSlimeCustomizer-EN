package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomAddonConfig;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

public abstract class YamlReader<T> {
    private final List<String> lateInits;
    protected final ProjectAddon addon;
    protected final YamlConfiguration configuration;

    public YamlReader(YamlConfiguration config, ProjectAddon addon) {
        this.configuration = config;
        this.lateInits = new ArrayList<>();
        this.addon = addon;
    }

    public abstract T readEach(String section);

    public final void preload() {
        for (String key : configuration.getKeys(false)) {
            ConfigurationSection section = configuration.getConfigurationSection(key);
            if (section == null) continue;
            ConfigurationSection register = section.getConfigurationSection("register");
            if (!checkForRegistration(key, register)) continue;

            List<SlimefunItemStack> items = preloadItems(key);

            if (items == null || items.isEmpty()) continue;

            for (SlimefunItemStack item : items) {
                addon.getPreloadItems().put(item.getItemId(), item);
                ExceptionHandler.debugLog("已预加载物品: " + item.getItemId());
            }
        }
    }

    @Nullable protected final SlimefunItemStack getPreloadItem(String itemId) {
        return addon.getPreloadItems().get(itemId);
    }

    public final List<T> readAll() {
        List<T> objects = new ArrayList<>();
        for (String key : configuration.getKeys(false)) {
            ConfigurationSection section = configuration.getConfigurationSection(key);
            if (section == null) continue;

            ExceptionHandler.debugLog("Start reading section: " + key);

            ConfigurationSection register = section.getConfigurationSection("register");
            if (!checkForRegistration(key, register)) continue;

            ExceptionHandler.debugLog("Check lateInit...");

            if (section.getBoolean("lateInit", false)) {
                putLateInit(key);
                ExceptionHandler.debugLog("Check result: lateInit");
                continue;
            }

            ExceptionHandler.debugLog("Start reading object...");

            var object = readEach(key);
            if (object != null) {
                objects.add(object);
                ExceptionHandler.debugLog("SUCCESS | Reading section " + key + "success！");
            } else {
                ExceptionHandler.debugLog("FAILURE | Reading section " + key + "failed！");
            }
        }
        return objects;
    }

    protected void putLateInit(String key) {
        lateInits.add(key);
    }

    public List<T> loadLateInits() {
        List<T> objects = new ArrayList<>();
        lateInits.forEach(key -> {
            ExceptionHandler.debugLog("Start reading lateInit section：" + key);
            var object = readEach(key);
            if (object != null) {
                objects.add(object);
                ExceptionHandler.debugLog("SUCCESS | Reading section " + key + "success！");
            } else {
                ExceptionHandler.debugLog("FAILURE | Reading section " + key + "failed！");
            }
        });

        lateInits.clear();

        return objects;
    }

    public abstract List<SlimefunItemStack> preloadItems(String s);

    private boolean checkForRegistration(String key, ConfigurationSection section) {
        if (section == null) return true;

        List<String> conditions = section.getStringList("conditions");
        boolean warn = section.getBoolean("warn", false);
        boolean unfinished = section.getBoolean("unfinished", false);

        if (unfinished) return false;

        for (String condition : conditions) {
            String[] splits = condition.split(" ");
            String head = splits[0];
            if (head.equalsIgnoreCase("hasplugin")) {
                if (splits.length != 2) {
                    ExceptionHandler.handleError("Found invalid condition while reading register conditions in " + key
                            + ": hasplugin only takes one argument");
                    continue;
                }
                boolean b = Bukkit.getPluginManager().isPluginEnabled(splits[1]);
                if (!b) {
                    if (warn) {
                        ExceptionHandler.handleError(key + "needs server plugin " + splits[1] + " to be registered");
                    }
                    return false;
                }
            } else if (head.equalsIgnoreCase("!hasplugin")) {
                if (splits.length != 2) {
                    ExceptionHandler.handleError("Found invalid condition while reading register conditions in " + key
                            + ": !hasplugin only takes one argument");
                    continue;
                }
                boolean b = Bukkit.getPluginManager().isPluginEnabled(splits[1]);
                if (b) {
                    if (warn) {
                        ExceptionHandler.handleError(key + "needs removing server plugin " + splits[1]
                                + " to be registered(may have conflicts?)");
                    }
                    return false;
                }
            } else if (head.equalsIgnoreCase("version")) {
                if (splits.length != 3) {
                    ExceptionHandler.handleError("Found invalid condition while reading register conditions in " + key
                            + ": version needs two arguments");
                    continue;
                }

                int current = CommonUtils.versionToCode(Bukkit.getMinecraftVersion());
                int destination = CommonUtils.versionToCode(splits[2]);

                if (!intCheck(splits[1], key, "version", current, destination, (op) -> "Needs version is " + op + splits[2] + " so that it can be registered", warn)) {
                    return false;
                }
            } else if (head.contains("config")) {
                CustomAddonConfig config = addon.getConfig();
                if (config == null) {
                    ExceptionHandler.handleError("Found an issue while reading the registration conditions for " + key + ": cannot found the config");
                    continue;
                }

                switch (head) {
                    case "config.boolean" -> {
                        if (splits.length != 2) {
                            ExceptionHandler.handleError("Found an issue while reading the registration conditions for " + key + ": config.boolean takes one argument");
                            continue;
                        }

                        if (!config.config().getBoolean(splits[1])) {
                            if (warn) {
                                ExceptionHandler.handleError(key + " needs config option " + splits[1] + "'s value is true so that it can be registered");
                            }
                            return false;
                        }
                    }
                    case "config.string" -> {
                        if (splits.length != 3) {
                            ExceptionHandler.handleError("Found an issue while reading the registration conditions for " + key + ": config.string takes two arguments");
                            continue;
                        }

                        if (!Objects.equals(config.config().getString(splits[1]), splits[2])) {
                            if (warn) {
                                ExceptionHandler.handleError(key + " needs config option " + splits[1] + "'s value is" + splits[2] + " so that it can be registered");
                            }
                            return false;
                        }
                    }
                    case "config.int" -> {
                        if (splits.length != 4) {
                            ExceptionHandler.handleError("Found an issue while reading the registration conditions for " + key + ": config.int takes three arguments");
                            continue;
                        }

                        String configKey = splits[1];
                        int current = config.config().getInt(splits[2]);
                        int destination = Integer.parseInt(splits[3]);

                        if (!intCheck(splits[1], key, "config.int", current, destination, (op) -> " needs config option" + configKey + "'s value is " + op + splits[3] + " so that it can be registered", warn)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean intCheck(String operator, String key, String regParam, int current, int destination, Function<String, String> msg, boolean warn) {
        String operation = "";
        boolean b = switch (operator) {
            case ">" -> {
                operation = "greater than";
                yield current > destination;
            }
            case "<" -> {
                operation = "less than";
                yield current < destination;
            }
            case ">=" -> {
                operation = "greater than or equal to";
                yield current >= destination;
            }
            case "<=" -> {
                operation = "less than or equal to";
                yield current <= destination;
            }
            case "==" -> {
                operation = "equals";
                yield current == destination;
            }
            case "!=" -> {
                operation = "not equals";
                yield current != destination;
            }
            default -> {
                ExceptionHandler.handleError("Found an issue while reading the registration conditions for " + key + ": " + regParam + " requires a valid operator!");
                yield true;
            }
        };

        if (!b) {
            if (warn) {
                ExceptionHandler.handleError(key + msg.apply(operation));
            }
        }

        return b;
    }
}
