package org.codeInge.command;

import org.codeInge.Main;
import org.codeInge.bot.ChatManager;
import org.codeInge.bot.MainBot;
import org.codeInge.commandTexts.CommandNotesTexts;
import org.codeInge.commandTexts.CommandStartTexts;
import org.codeInge.models.ConversationChatContext;
import org.codeInge.utilities.GlobalConstants;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;


public class CommandNotes extends Command {

    public static CommandNotes instance = new CommandNotes();


    @Override
    public void firstMessageAfterToEnter(Update update) {

        Long chatId = update.getMessage().getChatId();

        //Clear the steps and conversation for this context
        ChatManager.clearConversationContext(chatId);
        ChatManager.clearStep(chatId);


        InlineKeyboardButton viewNotesBtn = InlineKeyboardButton.builder()
                .text(CommandNotesTexts.VIEW_NOTES_BTN_TEXT)
                .callbackData(CommandNotesTexts.VIEW_NOTES_METHOD_NAME)
                .build();
        InlineKeyboardButton createNoteBtn = InlineKeyboardButton.builder()
                .text(CommandNotesTexts.CREATE_NOTE_BTN_TEXT)
                .callbackData(CommandNotesTexts.ASK_NOTE_DATA_METHOD_NAME)
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text(GlobalConstants.BACK_BUTTON_TEXT)
                .callbackData(GlobalConstants.BACK_MENU_METHOD_NAME)
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(viewNotesBtn);
        row.add(createNoteBtn);
        row.add(backButton);

        InlineKeyboardMarkup buttonsGroup = InlineKeyboardMarkup.builder()
            .keyboardRow( row)
            .build();


        SendMessage message = SendMessage
            .builder()
            .chatId(chatId.toString())
            .text(CommandNotesTexts.NOTES_WELCOME_TEXT)
            .replyMarkup(buttonsGroup)
            .build();


        ChatManager.registerNextStep(chatId, this::processButtons);
        MainBot.sendMessageTo(message);
    }



    @Override
    public void processButtons(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        switch (data) {
            case CommandNotesTexts.ASK_NOTE_DATA_METHOD_NAME:
                askNoteData(update);
                break;
            case GlobalConstants.BACK_MENU_METHOD_NAME:
                backButtonAction(update);
                break;
        }
    }

    @Override
    public void backButtonAction(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        EditMessageText editedMessageToMainMenu = EditMessageText.builder()
            .chatId(chatId)
            .messageId(update.getCallbackQuery().getMessage().getMessageId())
            .text(CommandStartTexts.WELCOME_TEXT)
            .build();
        MainBot.editMessageTo(editedMessageToMainMenu);

        //Clear the context
        ChatManager.clearConversationContext(chatId);
        ChatManager.clearStep(chatId);
    }

    public void askNoteData(Update update ) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        SendMessage message = SendMessage
            .builder()
            .chatId(chatId.toString())
            .text(CommandNotesTexts.NEW_NOTE_TITLE_ASK)
            .build();
        MainBot.sendMessageTo(message);
        ChatManager.registerNextStep(chatId, this::askTitleNote);
    }

    public void askTitleNote (Update update) {

        Long chatId = update.getMessage().getChatId();

        if (update.hasMessage() && update.getMessage().hasText()) {
            String title = update.getMessage().getText();

            if (existsNoteInDir(title)) {
                SendMessage message = SendMessage
                        .builder()
                        .chatId(chatId.toString())
                        .text(CommandNotesTexts.NOTE_ALREADY_EXISTS_MESSAGE)
                        .build();
                MainBot.sendMessageTo(message);

                ChatManager.registerNextStep(chatId, this::askTitleNote);
            }else {
                ConversationChatContext conversation = new ConversationChatContext(title, "");
                ChatManager.setConversationContext(chatId, conversation);

                SendMessage message = SendMessage
                        .builder()
                        .chatId(chatId.toString())
                        .text(CommandNotesTexts.NEW_NOTE_CONTENT_ASK)
                        .build();
                MainBot.sendMessageTo(message);

                ChatManager.registerNextStep(chatId, this::askContentNote);
            }

        }else {
            SendMessage message = SendMessage
                    .builder()
                    .chatId(chatId.toString())
                    .text(CommandNotesTexts.INVALID_NOTE_TITLE_MESSAGE)
                    .build();
            MainBot.sendMessageTo(message);
            ChatManager.registerNextStep(chatId, this::askTitleNote);
        }
    }

    public void askContentNote (Update update) {
        Long chatId = update.getMessage().getChatId();

        if (update.hasMessage() && update.getMessage().hasText()) {
            String content = update.getMessage().getText();

            ConversationChatContext conversation =  ChatManager.getConversationContext(chatId );
            conversation.setCurrentNoteText(content);

            //Create real note
            boolean createdNote = createNoteInDir(
                conversation.getCurrentNoteTitle(),
                conversation.getCurrentNoteText()
            );

            String messageToResponse = CommandNotesTexts.SUCCESS_CREATE_NOTE;
            if (!createdNote) {
                messageToResponse = CommandNotesTexts.FAILED_CREATE_NOTE;
            }

            SendMessage message = SendMessage
                    .builder()
                    .chatId(chatId.toString())
                    .text(messageToResponse)
                    .build();
            MainBot.sendMessageTo(message);

            firstMessageAfterToEnter(update);
        }else {
            SendMessage message = SendMessage
                    .builder()
                    .chatId(chatId.toString())
                    .text(CommandNotesTexts.INVALID_NOTE_MESSAGE)
                    .build();
            MainBot.sendMessageTo(message);
            ChatManager.registerNextStep(chatId, this::askContentNote);
        }
    }


    private Boolean createNoteInDir (String title, String content) {
        try {
            Files.writeString(
                    Path.of(GlobalConstants.PathToNotes+title+".txt"),
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String readNoteFromDir(String note){
        String read = null;
        try {
            read = Files.readString(Path.of(GlobalConstants.PathToNotes+note+".txt"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
        return read;
    }

    private boolean existsNoteInDir(String nameNote){
        File validator =  new File(GlobalConstants.PathToNotes+nameNote+".txt");
        return validator.exists();
    }

    private void viewNotesInDir(Update update) {
        Long chatId = update.getMessage().getChatId();

    }

}
