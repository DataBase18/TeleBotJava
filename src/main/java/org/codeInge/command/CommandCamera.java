package org.codeInge.command;


import com.github.sarxos.webcam.Webcam;
import org.codeInge.bot.ChatManager;
import org.codeInge.bot.MainBot;
import org.codeInge.commandTexts.CommandCameraTexts;
import org.codeInge.utilities.GlobalConstants;

import org.codeInge.utilities.GlobalMethods;
import org.codeInge.utilities.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CommandCamera extends Command {
    public static CommandCamera instance = new CommandCamera();

    @Override
    public void firstMessageAfterToEnter(Update update) {
        Long chatId  ;
        if ( update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }else{
            chatId = update.getMessage().getChatId();
        }

        //Clear the steps and conversation for this context
        ChatManager.clearAll(chatId);

        InlineKeyboardButton takePicture = InlineKeyboardButton.builder()
                .text(CommandCameraTexts.TAKE_PICTURE_TEXT)
                .callbackData(CommandCameraTexts.SELECT_CAM_METHOD)
                .build();


        InlineKeyboardRow rowOne = new InlineKeyboardRow();
        rowOne.add(takePicture);
        InlineKeyboardRow rowTwo = new InlineKeyboardRow();
        rowTwo.add(getBackButton());

        InlineKeyboardMarkup buttonsGroup = InlineKeyboardMarkup.builder()
                .keyboardRow( rowOne)
                .keyboardRow( rowTwo)
                .build();

        SendMessage message = SendMessage
                .builder()
                .chatId(chatId.toString())
                .text(CommandCameraTexts.NOTES_WELCOME_TEXT)
                .replyMarkup(buttonsGroup)
                .build();

        ChatManager.registerNextStep(chatId, this::processButtons);
        MainBot.sendMessageTo(message);
    }

    @Override
    public void processButtons(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();

        if (data.equalsIgnoreCase(CommandCameraTexts.SELECT_CAM_METHOD)){
            selectWebCam(update);
        }else if (GlobalMethods.splitNameMethod(data).equals(CommandCameraTexts.TAKE_PICTURE_METHOD)){
            takePhoto(update, GlobalMethods.splitValueCallbackData(data));
        }else if (data.equalsIgnoreCase(CommandCameraTexts.SAVE_PHOTO_METHOD)){
            saveTempPhoto(update);
        }else if (data.equals(GlobalConstants.BACK_MENU_METHOD_NAME)) {
            backButtonAction(update);
        }
    }



    private void saveTempPhoto(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        boolean successMove = GlobalMethods.moveFile(
        GlobalConstants.PathToPictures+"temp.png",
                GlobalConstants.PathToPictures,
                "png"
        );

        String messageText = CommandCameraTexts.SUCCESS_SAVED_PHOTO_MESSAGE ;
        if (!successMove){
            messageText = CommandCameraTexts.FAILED_SAVED_PHOTO;
        }



        SendMessage message = SendMessage
            .builder()
            .chatId(chatId.toString())
            .text(messageText)
            .build();

        MainBot.sendMessageTo(message);

        firstMessageAfterToEnter(update);
    }


    private void selectWebCam(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        List<Webcam> list =  Webcam.getWebcams();
        List<InlineKeyboardRow> cameras = new ArrayList<>();
        for  (Webcam webcam : list) {
            InlineKeyboardButton webCamButton = InlineKeyboardButton.builder()
                .text(webcam.getName())
                .callbackData(CommandCameraTexts.TAKE_PICTURE_METHOD+"="+webcam.getName())
                .build();
            InlineKeyboardRow row= new InlineKeyboardRow();
            row.add(webCamButton);
            cameras.add(row);
        }

        //To back main menu
        InlineKeyboardRow backRow= new InlineKeyboardRow();
        backRow.add(getBackButton());

        InlineKeyboardMarkup buttonsGroup = InlineKeyboardMarkup.builder()
            .keyboard(cameras)
            .keyboardRow( backRow )
            .build();

        //Delete previous message to set edited effect (Bug photo edited remove with this code)
        //Remove last message
        deleteLastMessage(update);

        SendMessage message = SendMessage
            .builder()
            .chatId(chatId.toString())
            .text(CommandCameraTexts.NOTES_WELCOME_TEXT)
            .replyMarkup(buttonsGroup)
            .build();


        ChatManager.registerNextStep(chatId, this::processButtons);
        MainBot.sendMessageTo(message);
    }

    private void takePhoto(Update update, String camName) {
        Long  chatId = update.getCallbackQuery().getMessage().getChatId();

        Webcam webcam = Webcam.getWebcamByName(camName);
        webcam.open();

        boolean photoSuccess = false;
        try {
            ImageIO.write(webcam.getImage(), "PNG", new File(GlobalConstants.PathToPictures+"temp.png"));
            photoSuccess = true;
            webcam.close();
        } catch (IOException e) {
            Logger.saveLog(Logger.formatLog(e.getMessage()));
            Logger.saveLog(Logger.formatLog(e.getCause().getMessage()));
            Logger.saveLog(Logger.formatLog(e.getLocalizedMessage()));
            Logger.saveLog("---------------------------------");
        }

        //Create input file
        InputFile photo =  new InputFile();
        photo.setMedia(new File(GlobalConstants.PathToPictures+"temp.png"));

        if (photoSuccess) {

            //Create save button, or take other photo
            InlineKeyboardButton permanentlySave = InlineKeyboardButton.builder()
                .text(CommandCameraTexts.SAVE_PERMANENTLY_PHOTO)
                .callbackData(CommandCameraTexts.SAVE_PHOTO_METHOD)
                .build();

            InlineKeyboardButton retakePhoto = InlineKeyboardButton.builder()
                .text(CommandCameraTexts.RETAKE_PHOTO)
                .callbackData(CommandCameraTexts.SELECT_CAM_METHOD)
                .build();

            InlineKeyboardRow saveRow = new InlineKeyboardRow();
            saveRow.add(permanentlySave);
            InlineKeyboardRow retakeRow = new InlineKeyboardRow();
            saveRow.add(retakePhoto);

            InlineKeyboardMarkup buttonsGroup = InlineKeyboardMarkup.builder()
                .keyboardRow(  saveRow  )
                .keyboardRow( retakeRow )
                .keyboardRow( new InlineKeyboardRow(getBackButton() )) //Back button
                .build();

            //Delete previous message to set edited effect (Bug photo edited remove with this code and remove sum messages)
            //Remove last message
            deleteLastMessage(update);

            SendPhoto msg = SendPhoto
                .builder()
                .chatId(chatId)
                .photo(photo)
                .caption(CommandCameraTexts.SUCCESS_PHOTO_TAKE)
                .replyMarkup(buttonsGroup)
                .build();

            MainBot.sendPhoto(msg);
            ChatManager.registerNextStep(chatId, this::processButtons);
        }else{
            SendMessage message = SendMessage
                .builder()
                .chatId(chatId.toString())
                .text(CommandCameraTexts.FAILED_TAKE_PHOTO)
                .build();

            MainBot.sendMessageTo(message);
            firstMessageAfterToEnter(update);
        }
    }



}
