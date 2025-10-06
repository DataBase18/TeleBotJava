package org.codeInge.command;

import org.codeInge.bot.ChatManager;
import org.codeInge.bot.MainBot;
import org.codeInge.commandTexts.CommandAudioTexts;
import org.codeInge.commandTexts.CommandCameraTexts;
import org.codeInge.commandTexts.CommandNotesTexts;
import org.codeInge.utilities.GlobalConstants;
import org.codeInge.utilities.GlobalMethods;
import org.codeInge.utilities.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CommandAudio extends Command {
    public static CommandAudio instance = new CommandAudio();

    @Override
    public void firstMessageAfterToEnter(Update update) {
        Long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }

        SendMessage message = SendMessage
            .builder()
            .chatId(chatId.toString())
            .text(CommandAudioTexts.WELCOME_TEXT)
            .build();

        ChatManager.registerNextStep(chatId, this::askSecondsToRecord);
        MainBot.sendMessageTo(message);
    }

    @Override
    public void processButtons(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();

        if (data.equalsIgnoreCase(CommandAudioTexts.ASK_SECONDS_METHOD)){
            askSecondsToRecord(update);
        }else if (data.equalsIgnoreCase(GlobalConstants.BACK_MENU_METHOD_NAME)){
            backButtonAction(update);
        }else if (data.equalsIgnoreCase(CommandAudioTexts.SAVE_AUDIO_METHOD)){
            moveAudioTemp(update);
        }else if (data.equalsIgnoreCase(CommandAudioTexts.ASK_SECONDS_AGAIN_METHOD)){
            firstMessageAfterToEnter(update);
        }
    }

    private void moveAudioTemp(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        boolean successMove = GlobalMethods.moveFile(
                GlobalConstants.PathToRecords+"temp.wav",
                GlobalConstants.PathToRecords,
                "wav"
        );

        String messageText = CommandAudioTexts.SUCCESS_SAVED_AUDIO_MESSAGE ;
        if (!successMove){
            messageText = CommandAudioTexts.FAILED_SAVED_AUDIO;
        }


        SendMessage message = SendMessage
                .builder()
                .chatId(chatId.toString())
                .text(messageText)
                .build();

        MainBot.sendMessageTo(message);
        CommandStart.instance.firstMessageAfterToEnter(update);
    }


    private void askSecondsToRecord(Update update) {
        SendMessage sendMessage = null;
        Long chatId =  0L;
        if (update.hasMessage() && update.getMessage().hasText()){
            chatId = update.getMessage().getChatId();
        }else {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }


        if (
            !(update.hasMessage() && update.getMessage().hasText())
            || GlobalMethods.tryParseInt(update.getMessage().getText()) == null
        ) {
            sendMessage = SendMessage
                    .builder()
                    .chatId(chatId.toString())
                    .text(CommandAudioTexts.INVALID_TIME)
                    .build();
            ChatManager.registerNextStep(chatId, this::askSecondsToRecord);
            MainBot.sendMessageTo(sendMessage);
        }else{
            sendMessage = SendMessage
                    .builder()
                    .chatId(chatId.toString())
                    .text(CommandAudioTexts.RECORDING_AUDIO_MESSAGE)
                    .build();
            MainBot.sendMessageTo(sendMessage);
            recordAudio(update, GlobalMethods.tryParseInt(update.getMessage().getText()));
        }

    }




    private void recordAudio(Update update, int seconds)  {
        Long chatId = update.getMessage().getChatId();
        AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            SendMessage message = SendMessage
                .builder()
                .chatId(chatId.toString())
                .text(CommandAudioTexts.NOT_MIRCO_FOUND_SUPPORT)
                .build();

            MainBot.sendMessageTo(message);
            CommandStart.instance.firstMessageAfterToEnter(update);
            return;
        }

        boolean successRecord = false;
        try {
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            AudioInputStream ais = new AudioInputStream(line);
            File archivo = new File(GlobalConstants.PathToRecords+"temp.wav");

            Thread record = new Thread(() -> {
                try {
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, archivo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            record.start();
            Thread.sleep(seconds * 1000L);
            line.stop();
            line.close();
            successRecord= true;
        }catch (Exception e) {
            Logger.saveLog(Logger.formatLog(e.getMessage()));
            Logger.saveLog(Logger.formatLog(e.getCause().getMessage()));
            Logger.saveLog(Logger.formatLog(e.getLocalizedMessage()));
            Logger.saveLog("---------------------------------");
        }

        //Save temp record
        if (successRecord) {
            //Prepare buttons
            InlineKeyboardButton savePermanentlyAudio = InlineKeyboardButton.builder()
                    .text(CommandAudioTexts.SAVE_AUDIO_TEXT)
                    .callbackData(CommandAudioTexts.SAVE_AUDIO_METHOD)
                    .build();
            InlineKeyboardButton recordAgain = InlineKeyboardButton.builder()
                    .text(CommandAudioTexts.RECORD_AGAIN_TEXT)
                    .callbackData(CommandAudioTexts.ASK_SECONDS_AGAIN_METHOD)
                    .build();

            InlineKeyboardRow rowOptions = new InlineKeyboardRow();
            rowOptions.add(savePermanentlyAudio);
            rowOptions.add(recordAgain);

            InlineKeyboardRow backMenuRow = new InlineKeyboardRow();
            backMenuRow.add(getBackButton());

            InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                    .keyboardRow(rowOptions)
                    .keyboardRow(backMenuRow)
                    .build();

            //Get input file to response record
            InputFile audio = new InputFile(new File(GlobalConstants.PathToRecords+"temp.wav"));

            SendAudio audioMessage = SendAudio.builder()
                    .chatId(chatId.toString())
                    .audio(audio)
                    .replyMarkup(markup)
                    .build();

            MainBot.sendAudioMessage(audioMessage);
            ChatManager.registerNextStep(chatId, this::processButtons);
        }else{
            SendMessage message = SendMessage
                .builder()
                .chatId(chatId.toString())
                .text(CommandAudioTexts.FAIL_RECORD_AUDIO)
                .build();

            MainBot.sendMessageTo(message);
            CommandStart.instance.firstMessageAfterToEnter(update);
        }
    }
}