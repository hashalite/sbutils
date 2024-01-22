package net.xolt.sbutils.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.TypeDescriptor;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.xolt.sbutils.SbUtils.LOGGER;

public class IOHandler {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("[YYYY-MM-dd HH:mm:ss] ");
    public static final File mcDirectory = FabricLoader.getInstance().getGameDir().toFile();

    public static final File modDirectory = new File(mcDirectory + File.separator + "sbutils");
    public static final File autoAdvertDir = new File(modDirectory + File.separator + "autoadvert");
    public static final File joinCommandsDir = new File(modDirectory + File.separator + "joincommands");
    public static final File loggerDir = new File(modDirectory + File.separator + "chatlogger");
    public static final File globalJoinCmdsFile = new File(joinCommandsDir + File.separator + "global.txt");
    public static final File messageLogFile = new File(loggerDir + File.separator + "messages.txt");
    public static final File transactionLogFile = new File(loggerDir + File.separator + "transactions.txt");
    public static final File visitLogFile = new File(loggerDir + File.separator + "visits.txt");
    public static final File dpLogFile = new File(loggerDir + File.separator + "dp.txt");
    public static final File autoKitDir = new File(modDirectory + File.separator + "autokit");
    public static final File autoKitFile = new File(autoKitDir + File.separator + "autokit.json");

    public static boolean createAll() {
        return createDirectories() && createFiles();
    }

    private static boolean createDirectories() {
        try {
            modDirectory.mkdir();
            autoAdvertDir.mkdir();
            joinCommandsDir.mkdir();
            loggerDir.mkdir();
            autoKitDir.mkdir();
        } catch (SecurityException e) {
            LOGGER.error("Unable to create directory: " + e.getMessage());
            return false;
        }
        return true;
    }

    private static boolean createFiles() {
        try {
            globalJoinCmdsFile.createNewFile();
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

    public static String readGlobalJoinCmds() {
        return readFile(globalJoinCmdsFile);
    }

    public static String readJoinCmdsForAccount(GameProfile account) {
        File nameFile = new File(joinCommandsDir + File.separator + account.getName() + ".txt");
        File uuidFile = new File(joinCommandsDir + File.separator + account.getId().toString() + ".txt");
        boolean nameFilePresent = nameFile.exists();
        boolean uuidFilePresent = uuidFile.exists();

        if (!nameFilePresent && !uuidFilePresent) {
            LOGGER.info("No join commands file found for " + account.getName() + ".");
            return "";
        }

        String cmdsForAccount = "";
        try {
            if (uuidFilePresent) { // UUID file takes precedence over username file
                cmdsForAccount += Files.readString(uuidFile.toPath());
                return cmdsForAccount;
            }
            if (nameFilePresent) {
                cmdsForAccount += Files.readString(nameFile.toPath());
            }
        } catch (IOException e) {
            LOGGER.error("Unable to read join commands for " + account.getName() + ":" + e.getMessage());
        }

        return cmdsForAccount;
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
        Util.getOperatingSystem().open(modDirectory);
    }

    public static void logTransaction(Text message,long messageReceivedAt) {
        logToFile(dateFormat.format(new Date(messageReceivedAt)) + message.getString(), transactionLogFile);
    }

    public static void logMessage(Text message,long messageReceivedAt) {
        logToFile(dateFormat.format(new Date(messageReceivedAt)) + message.getString(), messageLogFile);
    }

    public static void logVisit(Text message,long messageReceivedAt) {
        logToFile(dateFormat.format(new Date(messageReceivedAt)) + message.getString(), visitLogFile);
    }

    public static void logDpWinner(Text message, long messageReceivedAt) {
        logToFile(dateFormat.format(new Date(messageReceivedAt)) + message.getString(), dpLogFile);
    }

    public static boolean writeAdverts(List<String> adverts, String adFile) {
        File advertFile = new File(autoAdvertDir + File.separator + adFile);

        return overwriteFile(advertFile, adverts);
    }

    public static boolean writeAccountCommands(GameProfile account, List<String> commands) {
        File nameFile = new File(joinCommandsDir + File.separator + account.getName() + ".txt");
        File uuidFile = new File(joinCommandsDir + File.separator + account.getId().toString() + ".txt");

        if (!nameFile.exists() && !uuidFile.exists()) {
            return overwriteFile(uuidFile, commands);
        }

        if (uuidFile.exists()) {
            return overwriteFile(uuidFile, commands);
        }

        if (nameFile.exists()) {
            return overwriteFile(nameFile, commands);
        }

        return false;
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

    public static void writeAutoKitData(Map<String, Map<String, Long>> autoKitData) {
        Gson gson = new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.IDENTITY).setPrettyPrinting().create();
        overwriteFile(autoKitFile, List.of(gson.toJson(autoKitData)));
    }

    public static Map<String, Map<String, Long>> readAutoKitData() {
        String stringData = readFile(autoKitFile);
        if (stringData == null || stringData.isEmpty()) {
            return new HashMap<>();
        }
        Gson gson = new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.IDENTITY).create();
        return gson.fromJson(stringData, new TypeToken<Map<String, Map<String, Long>>>(){}.getType());
    }
}
