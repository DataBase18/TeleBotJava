package org.codeInge.utilities;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    static {
        File folder = new File(GlobalConstants.PathToLog);
        if (!folder.exists()) {
            boolean result = folder.mkdirs();
        }
    }

    private static String getLogFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
        return GlobalConstants.PathToLog + "log_" + date + ".txt";
    }

    public static String formatLog(String content) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String timestamp = sdf.format(new Date());
        return "[" + timestamp + "] - " + content;
    }

    public static void saveLog(String content) {
        String line = formatLog(content);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getLogFileName(), true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error al guardar log: " + e.getMessage());
        }
    }

    public static String readLog() {
        StringBuilder sb = new StringBuilder();
        try {
            Files.lines(Paths.get(getLogFileName())).forEach(line -> sb.append(line).append("\n"));
        } catch (IOException e) {
            System.err.println("Error al leer log: " + e.getMessage());
        }
        return sb.toString();
    }
}
