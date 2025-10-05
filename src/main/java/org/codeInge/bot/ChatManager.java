package org.codeInge.bot;

import org.codeInge.models.ConversationChatContext;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ChatManager {

    //To manager conversation step
    private static final Map<Long, Consumer<Update>> nextStepMap = new HashMap<>();

    public static void registerNextStep(Long chatId, Consumer<Update> nextStep) {
        nextStepMap.put(chatId, nextStep);
    }

    public static Consumer<Update> getNextStep(Long chatId) {
        return nextStepMap.get(chatId);
    }

    public static void clearStep(Long chatId) {
        nextStepMap.remove(chatId);
    }


    //To Manager data chat
    public static final Map<Long, ConversationChatContext> conversations =  new HashMap<>();

    public static ConversationChatContext getConversationContext(Long chatId) {
        return conversations.get(chatId);
    }

    public static void setConversationContext(Long chatId, ConversationChatContext conversationChatContext) {
        conversations.put(chatId, conversationChatContext);
    }



    public static void clearConversationContext(Long chatId) {
        conversations.remove(chatId);
    }
}
