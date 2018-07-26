package me.xfl03.rift.tweakloader;

import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class TweakLoader {
    private static final File LOG_FILE = new File(Launch.minecraftHome, "RTL.log");
    private static final Logger logger = new Logger(LOG_FILE);
    private static final File MODS_DIR = new File(Launch.minecraftHome, "mods");

    public TweakLoader() {
        try {
            ADD_URL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            ADD_URL.setAccessible(true);
        } catch (Exception e) {
            logger.warning("Error occurs while init ADDURL : " + e);
        }
    }

    public void load() {
        List<TweakMod> tweakMods = scanTweakMods();
        loadTweakMods(tweakMods);
    }

    public List<TweakMod> scanTweakMods() {
        List<TweakMod> tweakMods = new ArrayList<>();//TweakMods
        if (!MODS_DIR.exists() || !MODS_DIR.isDirectory() || MODS_DIR.listFiles() == null) {
            logger.info("Dir 'mods' not found.");
            return tweakMods;
        }

        File[] files = MODS_DIR.listFiles();
        for (File file : files) {
            if (file == null || !file.exists() || file.isDirectory() || !file.getName().toLowerCase().endsWith(".jar")) {
                logger.info("'" + (file == null ? "null" : file.getName()) + "' is not a jar file.");
                continue;
            }
            try {
                logger.info("Find a jar file : '" + file.getName() + "'.");
                JarFile jar = new JarFile(file);
                Attributes at = jar.getManifest().getMainAttributes();

                String tweakClass = at.getValue("TweakClass");
                if (tweakClass == null || tweakClass.isEmpty()) {
                    logger.info("TweakClass not found in '" + file.getName() + "', it will be ignored.");
                    continue;
                }
                tweakMods.add(new TweakMod(tweakClass, file));
            } catch (Exception e) {
                logger.warning("Error occurs while scanning mod '" + file.getName() + "' : " + e);
            }
        }
        return tweakMods;
    }

    public void loadTweakMods(List<TweakMod> tweakMods) {
        for (TweakMod tweakMod : tweakMods) {
            try {
                loadTweakMod(tweakMod);
            } catch (Exception e) {
                logger.warning("Error occurs while loading mod '" + tweakMod.modFile.getName() + "' : " + e);
            }
        }
    }

    private static Method ADD_URL;
    private List<URL> urls = Launch.classLoader.getSources();

    public void loadTweakMod(TweakMod tweakMod) throws Exception {
        URL url = tweakMod.modFile.toURI().toURL();
        if (!urls.contains(url)) {
            ADD_URL.invoke(Launch.classLoader.getClass().getClassLoader(), tweakMod.modFile.toURI().toURL());
            Launch.classLoader.addURL(url);
            logger.info("'" + tweakMod.modFile.getName() + "' will be loaded.");
        }

        List<String> tweakers = (List<String>) Launch.blackboard.get("TweakClasses");
        if (tweakers.contains(tweakMod.tweakClass)) {
            logger.info("TweakClass '" + tweakMod.tweakClass + "' will be ignored.");
            return;
        }

        tweakers.add(tweakMod.tweakClass);
        logger.info("TweakClass '" + tweakMod.tweakClass + "' has been added.");
    }
}
