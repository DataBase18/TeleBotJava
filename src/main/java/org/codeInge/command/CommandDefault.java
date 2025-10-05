package org.codeInge.command;

import org.codeInge.bot.MainBot;
import org.codeInge.commandTexts.CommandDefaultTexts;
import org.codeInge.commandTexts.CommandStartTexts;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CommandDefault extends Command{

    public static CommandDefault instance = new CommandDefault();

    @Override
    public void firstMessageAfterToEnter(Update update) {
        Long chatId = update.getMessage().getChatId();
        SendMessage message = new SendMessage(chatId.toString(), CommandDefaultTexts.DEFAULT_TEXT+ CommandStartTexts.WELCOME_TEXT);
        try {
            MainBot.telegramClient.execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void processButtons(Update update) {

    }

}
