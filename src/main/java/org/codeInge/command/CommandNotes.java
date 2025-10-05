package org.codeInge.command;

import org.codeInge.bot.ChatManager;
import org.codeInge.bot.MainBot;
import org.codeInge.commandTexts.CommandNotesTexts;
import org.codeInge.commandTexts.CommandStartTexts;
import org.codeInge.models.ConversationChatContext;
import org.codeInge.utilities.GlobalConstants;
import org.codeInge.utilities.GlobalMethods;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class CommandNotes extends Command {

    public static CommandNotes instance = new CommandNotes();


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


        InlineKeyboardButton viewNotesBtn = InlineKeyboardButton.builder()
                .text(CommandNotesTexts.VIEW_NOTES_BTN_TEXT)
                .callbackData(CommandNotesTexts.VIEW_NOTES_METHOD_NAME)
                .build();
        InlineKeyboardButton createNoteBtn = InlineKeyboardButton.builder()
                .text(CommandNotesTexts.CREATE_NOTE_BTN_TEXT)
                .callbackData(CommandNotesTexts.ASK_NOTE_DATA_METHOD_NAME)
                .build();


        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(viewNotesBtn);
        row.add(createNoteBtn);
        row.add(getBackButton());

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

        if (data.equals(CommandNotesTexts.ASK_NOTE_DATA_METHOD_NAME)) {
            askNoteData(update);
        } else if (data.equals(CommandNotesTexts.VIEW_NOTES_METHOD_NAME)) {
            viewNotesInDir(update);
        } else if (data.equals(GlobalConstants.BACK_MENU_METHOD_NAME)) {
            backButtonAction(update);
        } else if (GlobalMethods.splitNameMethod(data).equals(CommandNotesTexts.VIEW_INDIVIDUAL_NOTE_METHOD_NAME)) {
            showNoteBy(update);
        }else if(data.equals(GlobalConstants.NEXT_BUTTON_METHOD_NAME)){
            updateNotesShowedInMessage(update, 1);
        }else if(data.equals(GlobalConstants.PREVIOUS_BUTTON_METHOD_NAME)){

            updateNotesShowedInMessage(update, -1);
        }
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
                ConversationChatContext conversation = new ConversationChatContext();
                conversation.setCurrentNoteTitle(title);
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

    private void showNoteBy (Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();

        //read note
        String content = readNoteFromDir(GlobalMethods.splitValueCallbackData(data));


        //Send note content
        SendMessage message = SendMessage
            .builder()
            .chatId(chatId.toString())
            .text(CommandNotesTexts.CONTENT_NOTE_HEADER+content)
            .parseMode("HTML")
            .build();

        MainBot.sendMessageTo(message);

        //For send main menu notes after any seconds
        ScheduledExecutorService schedulerToSendMessage = Executors.newScheduledThreadPool(1);
        schedulerToSendMessage.schedule(() -> firstMessageAfterToEnter(update), 3, TimeUnit.SECONDS);

    }

    private String readNoteFromDir(String note){
        String read ;
        try {
            read = Files.readString(Path.of(GlobalConstants.PathToNotes+note+".txt"), StandardCharsets.UTF_8);
            return read;
        } catch (IOException e) {
            return "";
        }
    }

    private boolean existsNoteInDir(String nameNote){
        File validator =  new File(GlobalConstants.PathToNotes+nameNote+".txt");
        return validator.exists();
    }

    private InlineKeyboardMarkup generateGroupButtons(ArrayList<ArrayList<String>> allGroups, int indexToGroup){
        ArrayList<String> list = allGroups.get(indexToGroup);

        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (String note : list){
            InlineKeyboardButton noteButton = InlineKeyboardButton.builder()
                    .text(note)
                    .callbackData(CommandNotesTexts.VIEW_INDIVIDUAL_NOTE_METHOD_NAME+"="+note)
                    .build();

            InlineKeyboardRow oneRow = new InlineKeyboardRow();
            oneRow.add(noteButton);

            rows.add(oneRow);
        }

        //Back button
        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text(GlobalConstants.BACK_BUTTON_TEXT)
                .callbackData(GlobalConstants.BACK_MENU_METHOD_NAME)
                .build();

        InlineKeyboardRow backRowButton = new InlineKeyboardRow();

        //Add to row button Previous if apply
        if  (indexToGroup>0){
            //Previous button
            InlineKeyboardButton nextButton = InlineKeyboardButton.builder()
                .text(GlobalConstants.PREVIOUS_BUTTON_ICON)
                .callbackData(GlobalConstants.PREVIOUS_BUTTON_METHOD_NAME)
                .build();
            backRowButton.add(nextButton);
        }

        //Add back button for middle position
        backRowButton.add(backButton);

        //Add to row button to next if apply
        if (allGroups.size() > 1 && allGroups.size() > (indexToGroup+1)){
            //Next button
            InlineKeyboardButton nextButton = InlineKeyboardButton.builder()
                    .text(GlobalConstants.NEXT_BUTTON_ICON)
                    .callbackData(GlobalConstants.NEXT_BUTTON_METHOD_NAME)
                    .build();
            backRowButton.add(nextButton);
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .keyboardRow(backRowButton)
                .build();
    }

    private void viewNotesInDir(Update update) {
        File directoryOfNotes = new File(GlobalConstants.PathToNotes);
        File[] notes = directoryOfNotes.listFiles((file) -> file.isFile() && file.getName().endsWith(".txt"));

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        if(notes != null &&  notes.length > 0) {

            //Get the conversation context
            ConversationChatContext conversation =  ChatManager.getConversationContext(chatId);
            if (conversation == null) {
                ChatManager.setConversationContext(chatId, new ConversationChatContext());
                conversation  = new ConversationChatContext();
            }

            //Calculate groups
            ArrayList<File> notesInDir = new ArrayList<>(Arrays.stream(notes).toList());
            int totalElements = notesInDir.size();
            int groupsCount = (int) Math.ceil((double) totalElements / GlobalConstants.maxButtonsToPagesStandardValue);

            //Reset context for notes and set vars
            conversation.setNotesList(new ArrayList<>());
            conversation.setCurrentGroupPagination(0);
            for (int i = 0; i < groupsCount; i++) {
                int start = i * GlobalConstants.maxButtonsToPagesStandardValue;
                int end = Math.min(start + GlobalConstants.maxButtonsToPagesStandardValue, totalElements);
                ArrayList<File> group = new  ArrayList<>(notesInDir.subList(start, end));
                ArrayList<String> notesGroup = new ArrayList<>();
                for (File note : group) {
                    notesGroup.add(note.getName().replace(".txt", ""));
                }
                conversation.getNotesList().add(notesGroup);
            }

            //Generate init notes group
            InlineKeyboardMarkup allNotesToShow = generateGroupButtons(conversation.getNotesList(), 0);

            //Send message
            SendMessage message = SendMessage
                    .builder()
                    .chatId(chatId.toString())
                    .text(CommandNotesTexts.SELECT_NOTE_MESSAGE)
                    .replyMarkup(allNotesToShow)
                    .build();
            MainBot.sendMessageTo(message);

            //Save state
            ChatManager.setConversationContext(chatId, conversation);

        } else {
            InlineKeyboardButton createNoteBtn = InlineKeyboardButton.builder()
                .text(CommandNotesTexts.CREATE_NOTE_BTN_TEXT)
                .callbackData(CommandNotesTexts.ASK_NOTE_DATA_METHOD_NAME)
                .build();
            InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text(GlobalConstants.BACK_BUTTON_TEXT)
                .callbackData(GlobalConstants.BACK_MENU_METHOD_NAME)
                .build();

            InlineKeyboardRow oneRow = new InlineKeyboardRow();
            oneRow.add(createNoteBtn);
            InlineKeyboardRow twoRow = new InlineKeyboardRow();
            twoRow.add(backButton);

            InlineKeyboardMarkup buttonsGroup = InlineKeyboardMarkup.builder()
                .keyboardRow( oneRow)
                .keyboardRow( twoRow)
                .build();

            EditMessageText message = EditMessageText
                    .builder()
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .chatId(chatId.toString())
                    .text(CommandNotesTexts.NOT_EXISTS_NOTES_MESSAGE)
                    .replyMarkup(buttonsGroup)
                    .build();
            MainBot.editMessageTo(message);
        }

        ChatManager.registerNextStep(chatId, this::processButtons);
    }


    /// typeMovement can take value -1 or 1 to plus of the index the buttons group
    private void updateNotesShowedInMessage(Update update, int typeMovement){
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        ConversationChatContext conversation = ChatManager.getConversationContext(chatId);

        //update index to group
        int newIndex = conversation.getCurrentGroupPagination()+typeMovement;
        conversation.setCurrentGroupPagination(newIndex);

        //get new buttons
        InlineKeyboardMarkup allNotesToShow = generateGroupButtons(conversation.getNotesList(), newIndex);

        EditMessageText messageEdited = EditMessageText
            .builder()
            .messageId(update.getCallbackQuery().getMessage().getMessageId())
            .chatId(chatId.toString())
            .text(CommandNotesTexts.NOT_EXISTS_NOTES_MESSAGE)
            .replyMarkup(allNotesToShow)
            .build();
        MainBot.editMessageTo(messageEdited);

        //Save next step
        ChatManager.registerNextStep(chatId, this::processButtons);

        //Update state
        ChatManager.setConversationContext(chatId, conversation);
    }

}
