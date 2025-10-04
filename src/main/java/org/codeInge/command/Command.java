package org.codeInge.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class Command {

    public abstract void firstMessageAfterToEnter(Update update);

    public abstract void processButtons(Update update);
    public abstract void backButtonAction(Update update);
}
