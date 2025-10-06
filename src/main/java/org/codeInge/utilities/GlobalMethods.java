package org.codeInge.utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GlobalMethods {

    public static String splitNameMethod (String method){
        String[] parts = method.split("=");
        if (parts.length != 0){
            return parts[0];
        }
        return "---N/A---";
    }
    public static String splitValueCallbackData(String data){
        String[] parts = data.split("=");
        if (parts.length != 0){
            return parts[1];
        }
        return "---N/A---";
    }

    public static Float tryParseFloat(String cadena) {
        if (cadena == null || cadena.trim().isEmpty()) return null;
        try {
            return Float.parseFloat(cadena);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    public static Integer tryParseInt(String cadena) {
        if (cadena == null || cadena.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(cadena);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean moveFile(String filePath, String pathDestination, String extensionFile){
        //Generate new name
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = sdf.format(new Date());

        //Move photo
        try {
            Files.move(new File(filePath).toPath(),
                    new File(pathDestination+timestamp+"."+extensionFile).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            Logger.saveLog(Logger.formatLog(e.getMessage()));
            Logger.saveLog(Logger.formatLog(e.getCause().getMessage()));
            Logger.saveLog(Logger.formatLog(e.getLocalizedMessage()));
            Logger.saveLog("---------------------------------");
            return false;
        }
    }
}
