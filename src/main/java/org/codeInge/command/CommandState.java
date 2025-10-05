package org.codeInge.command;

import org.codeInge.bot.MainBot;
import org.codeInge.commandTexts.CommandStateTexts;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

public class CommandState extends Command {

    public static CommandState instance = new CommandState();

    @Override
    public void firstMessageAfterToEnter(Update update) {
        Long chatId  ;
        if ( update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }else{
            chatId = update.getMessage().getChatId();
        }

        String state = getStatus();
        SendMessage message = SendMessage
                .builder()
                .chatId(chatId.toString())
                .text(state)
                .build();

        MainBot.sendMessageTo(message);

        CommandStart.instance.firstMessageAfterToEnter(update);
    }

    @Override
    public void processButtons(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
    }

    private String getStatus(){
        StringBuilder report = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.##");

        // CPU
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        report.append(CommandStateTexts.CPU_STATUS).append(osBean.getAvailableProcessors()).append(" cores\n");
        report.append(CommandStateTexts.SYSTEM_WORK_STATUS).append(df.format(osBean.getSystemLoadAverage())).append("\n");

        // RAM
        Runtime rt = Runtime.getRuntime();
        long totalRam = rt.totalMemory();
        long freeRam = rt.freeMemory();
        report.append(CommandStateTexts.RAM_STATUS).append(df.format(freeRam / 1024.0 / 1024)).append(" MB libres de ")
                .append(df.format(totalRam / 1024.0 / 1024)).append(" MB\n");

        // Disco
        File root = new File("/");
        long totalDisk = root.getTotalSpace();
        long freeDisk = root.getFreeSpace();
        report.append(CommandStateTexts.HDD_STATUS).append(df.format(freeDisk / 1024.0 / 1024 / 1024)).append(" GB libres de ")
                .append(df.format(totalDisk / 1024.0 / 1024 / 1024)).append(" GB\n");

        // Red local
        try {
            InetAddress local = InetAddress.getLocalHost();
            report.append(CommandStateTexts.LOCAL_IP_STATUS).append(local.getHostAddress()).append("\n");
            report.append(CommandStateTexts.HOST_NAME).append(local.getHostName()).append("\n");
        } catch (UnknownHostException e) {
            report.append(CommandStateTexts.UNKNOW_HOST);
        }



        // Uptime
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        report.append(CommandStateTexts.UPTIME).append(hours).append("h ").append(minutes % 60).append("m ").append(seconds % 60).append("s\n");
        return report.toString();
    }
}