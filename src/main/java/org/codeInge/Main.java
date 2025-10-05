package org.codeInge;

import org.codeInge.bot.MainBot;
import org.codeInge.utilities.Config;
import org.codeInge.utilities.GlobalConstants;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {

        Config config;
        try {
            config = new Config(GlobalConstants.PathToProperties);
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        try {
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(config.getProperty(GlobalConstants.TelegramTokenProperty), new MainBot(config));
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}