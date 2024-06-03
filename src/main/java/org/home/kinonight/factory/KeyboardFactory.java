package org.home.kinonight.factory;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static org.home.kinonight.constants.Buttons.CREATE_NEW_LIST;
import static org.home.kinonight.constants.Buttons.LOGOUT;

public class KeyboardFactory {
    public static InlineKeyboardMarkup chooseFilmList(List<String> filmLists) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();

        for (String filmList : filmLists) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(filmList);
            inlineKeyboardButton.setCallbackData(filmList);
            inlineButtons.add(List.of(inlineKeyboardButton));
        }
        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return inlineKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup logout(){
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(newButton(LOGOUT));
        keyboardRows.add(row);
        return new ReplyKeyboardMarkup(keyboardRows);
    }

    public static ReplyKeyboardMarkup optionButtons(){
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(newButton(CREATE_NEW_LIST));
        row.add(newButton(LOGOUT));
        keyboardRows.add(row);
        return new ReplyKeyboardMarkup(keyboardRows);
    }

    private static KeyboardButton newButton(String text){
        return new KeyboardButton(text);
    }
}
