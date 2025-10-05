package org.codeInge.command;

import org.codeInge.bot.MainBot;
import org.codeInge.commandTexts.CommandLogTexts;
import org.codeInge.utilities.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class CommandLog extends Command{


    public static CommandLog instance = new CommandLog();

    @Override
    public void firstMessageAfterToEnter(Update update) {
        Long  chatId = update.getMessage().getChatId();
        String content = Logger.readLog();

        if (content.isEmpty()){
            content = CommandLogTexts.NOT_LOG_MESSAGE;
        }

        SendMessage message = SendMessage
                .builder()
                .chatId(chatId.toString())
                .text(content)
                .build();
        MainBot.sendMessageTo(message);

        CommandStart.instance.firstMessageAfterToEnter(update);
    }

    @Override
    public void processButtons(Update update) {

    }
}
