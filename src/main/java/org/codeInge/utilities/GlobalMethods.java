package org.codeInge.utilities;

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
}
