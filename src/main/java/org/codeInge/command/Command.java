package org.codeInge.command;

import org.codeInge.bot.ChatManager;
import org.codeInge.bot.MainBot;
import org.codeInge.commandTexts.CommandStartTexts;
import org.codeInge.utilities.GlobalConstants;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public abstract class Command {

    public abstract void firstMessageAfterToEnter(Update update);

    public abstract void processButtons(Update update);
    public void backButtonAction(Update update){
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

    public InlineKeyboardButton getBackButton (){
        return InlineKeyboardButton.builder()
                .text(GlobalConstants.BACK_BUTTON_TEXT)
                .callbackData(GlobalConstants.BACK_MENU_METHOD_NAME)
                .build();
    }

    public void deleteLastMessage(Update update){
        Long  chatId = 0L;
        if(update.hasCallbackQuery()){
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }else if (update.getMessage().hasCaption()){
            chatId = update.getMessage().getChatId();
        }
        //Delete previous message to set edited effect (Bug photo edited remove with this code)
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(chatId.toString())
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .build();
        MainBot.deleteMessage(deleteMessage);
    }
}
