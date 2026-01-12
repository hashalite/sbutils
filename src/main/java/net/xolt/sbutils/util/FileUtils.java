package net.xolt.sbutils.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
//? if >= 1.21.11 {
import net.minecraft.util.Util;
//? } else {
/*import net.minecraft.Util;
 *///? }

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.xolt.sbutils.SbUtils.LOGGER;

public class FileUtils {
    private static final SimpleDateFormat dateTimeLogFormat = new SimpleDateFormat("[YYYY-MM-dd HH:mm:ss] ");
    public static final File mcDirectory = FabricLoader.getInstance().getGameDir().toFile();

    public static final File modDirectory = new File(mcDirectory + File.separator + "sbutils");
    public static final File autoAdvertDir = new File(modDirectory + File.separator + "autoadvert");
    public static final File loggerDir = new File(modDirectory + File.separator + "chatlogger");
    public static final File messageLogFile = new File(loggerDir + File.separator + "messages.txt");
    public static final File transactionLogFile = new File(loggerDir + File.separator + "transactions.txt");
    public static final File visitLogFile = new File(loggerDir + File.separator + "visits.txt");
    public static final File dpLogFile = new File(loggerDir + File.separator + "dp.txt");
    public static final File autoKitDir = new File(modDirectory + File.separator + "autokit");
    public static final File autoKitFile = new File(autoKitDir + File.separator + "autokit.json");
    public static final File mapSaverDir = new File(modDirectory + File.separator + "mapsaver");

    public static boolean createAll() {
        return createDirectories() && createFiles();
    }

    private static boolean createDirectories() {
        try {
            modDirectory.mkdir();
            autoAdvertDir.mkdir();
            loggerDir.mkdir();
            autoKitDir.mkdir();
            mapSaverDir.mkdir();
        } catch (SecurityException e) {
            LOGGER.error("Unable to create directory: " + e.getMessage());
            return false;
        }
        return true;
    }

    private static boolean createFiles() {
        try {
            transactionLogFile.createNewFile();
            messageLogFile.createNewFile();
            visitLogFile.createNewFile();
            dpLogFile.createNewFile();
            autoKitFile.createNewFile();
        } catch (IOException e) {
            LOGGER.error("Unable to create file: " + e.getMessage());
            return false;
        }
        return true;
    }

    public static String readAdFile(String filename) {
        return readFile(new File(autoAdvertDir + File.separator + filename));
    }

    public static String readFile(File file) {
        ensureFileExists(file);
        String contents;
        try {
            contents = Files.readString(file.toPath());
        } catch (IOException e) {
            LOGGER.error("Unable to read from " + file + ": " + e.getMessage());
            return null;
        }
        return contents;
    }

    public static void openModDirectory() {
        Util.getPlatform().openFile(modDirectory);
    }

    public static void logTransaction(Component message,long messageReceivedAt) {
        logToFile(dateTimeLogFormat.format(new Date(messageReceivedAt)) + message.getString(), transactionLogFile);
    }

    public static void logMessage(Component message,long messageReceivedAt) {
        logToFile(dateTimeLogFormat.format(new Date(messageReceivedAt)) + message.getString(), messageLogFile);
    }

    public static void logVisit(Component message,long messageReceivedAt) {
        logToFile(dateTimeLogFormat.format(new Date(messageReceivedAt)) + message.getString(), visitLogFile);
    }

    public static void logDpWinner(Component message, long messageReceivedAt) {
        logToFile(dateTimeLogFormat.format(new Date(messageReceivedAt)) + message.getString(), dpLogFile);
    }

    public static boolean writeAdverts(List<String> adverts, String adFile) {
        File advertFile = new File(autoAdvertDir + File.separator + adFile);

        return overwriteFile(advertFile, adverts);
    }

    public static boolean logToFile(String text, File file) {
        ensureFileExists(file);
        try {
            Files.writeString(file.toPath(), text + "\n", StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.error("Unable to write to " + file + ":" + e.getMessage());
            return false;
        }
        return true;
    }

    private static boolean overwriteFile(File file, List<String> contents) {
        ensureFileExists(file);
        try {
            Files.write(file.toPath(), contents, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Unable to write to " + file + ":" + e.getMessage());
            return false;
        }
        return true;
    }

    // Creates the provided file and its parent directories if they don't exist
    private static void ensureFileExists(File file) {
        try {
            Files.createDirectories(file.getParentFile().toPath());

        } catch (IOException e) {}

        try {
            Files.createFile(file.toPath());
        } catch (IOException e) {}
    }

    public static void writeAutoKitData(Map<String, Map<String, Map<String, Long>>> autoKitData) {
        Gson gson = new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.IDENTITY).setPrettyPrinting().create();
        overwriteFile(autoKitFile, List.of(gson.toJson(autoKitData)));
    }

    public static Map<String, Map<String, Map<String, Long>>> readAutoKitData() {
        String stringData = readFile(autoKitFile);
        if (stringData == null || stringData.isEmpty()) {
            return new HashMap<>();
        }
        Gson gson = new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.IDENTITY).create();

        try {
            return gson.fromJson(stringData, new TypeToken<Map<String, Map<String, Map<String, Long>>>>(){}.getType());
        } catch (JsonSyntaxException e) {
            LOGGER.error("Auto Kit data is in the wrong format, resetting.");
            return new HashMap<>();
        }
    }

    public static boolean saveMapImage(int mapId, String servername, NativeImage image) {
        File serverDir = new File(mapSaverDir + File.separator + servername);
        File imagePath = new File(serverDir + File.separator + servername + "-" + mapId + ".png");
        int suffix = 2;
        while (imagePath.exists())
            imagePath = new File(serverDir + File.separator + servername + "-" + mapId + "-" + suffix++ + ".png");
        try {
            serverDir.mkdir();
            image.writeToFile(imagePath);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
