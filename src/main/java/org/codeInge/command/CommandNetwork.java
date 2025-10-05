package org.codeInge.command;

import org.codeInge.bot.ChatManager;
import org.codeInge.bot.MainBot;
import org.codeInge.commandTexts.CommandNetworkTexts;
import org.codeInge.utilities.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandNetwork extends Command {

    public static CommandNetwork instance = new CommandNetwork();

    @Override
    public void firstMessageAfterToEnter(Update update) {
        Long chatId = update.getMessage().getChat().getId();

        SendMessage message = SendMessage
                .builder()
                .chatId(chatId.toString())
                .text(CommandNetworkTexts.WELCOME_TEXT)
                .build();

        MainBot.sendMessageTo(message);

        getAndShowHosts(update);
    }

    @Override
    public void processButtons(Update update) {

    }

    private void getAndShowHosts(Update update) {
        Long chatId = update.getMessage().getChat().getId();

        boolean successHost = false;
        StringBuilder stringBuilder = null;
        try {
            Process p = Runtime.getRuntime().exec("arp -a");
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            stringBuilder = new StringBuilder();
            String line ;
            while ((line = reader.readLine()) != null) {
                if (line.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*")) {
                    stringBuilder.append("• ").append(line.trim()).append("\n");
                }
            }
            successHost=true;
        } catch (IOException e) {
            Logger.saveLog(Logger.formatLog(e.getMessage()));
            Logger.saveLog(Logger.formatLog(e.getCause().getMessage()));
            Logger.saveLog(Logger.formatLog(e.getLocalizedMessage()));
            Logger.saveLog("---------------------------------");
        }

        SendMessage message;

        if (successHost) {
            String hosts = stringBuilder.toString();
            if (hosts.isEmpty()) {
                hosts = CommandNetworkTexts.NOT_HOST_FOUND_MESSAGE;
            }
            message = SendMessage
                    .builder()
                    .chatId(chatId.toString())
                    .text(hosts)
                    .build();
        }else {
            message = SendMessage
                    .builder()
                    .chatId(chatId.toString())
                    .text(CommandNetworkTexts.FAILED_SCAN_MESSAGE)
                    .build();
        }

        MainBot.sendMessageTo(message);
        CommandStart.instance.firstMessageAfterToEnter(update);

    }
}
