package ru.yuraender.discord.util;

import lombok.experimental.UtilityClass;
import ru.yuraender.discord.Main;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

@UtilityClass
public class LibsUtil {

    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    public void loadLibs() {
        int count = 0;
        File library = new File(Main.getDataFolder().toFile() + File.separator + "libs");
        if (!library.exists()) {
            library.mkdir();
        }
        for (File file : library.listFiles((dir, name) -> name.endsWith(".jar"))) {
            try {
                loadLib(file);
                count++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("Загружено " + count + " библиотек.");
    }

    private void loadLib(File file) throws Exception {
        URLClassLoader loader = (URLClassLoader) Main.class.getClassLoader();
        Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addUrl.setAccessible(true);
        addUrl.invoke(loader, file.toURI().toURL());
        addUrl.setAccessible(false);
    }
}
