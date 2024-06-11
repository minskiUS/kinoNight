package org.home.kinonight.factory;

import org.springframework.core.env.Environment;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.home.kinonight.constants.Buttons.*;

public class KeyboardFactory {
    public static InlineKeyboardMarkup chooseFromList(List<String> buttons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();

        for (String filmList : buttons) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(filmList);
            inlineKeyboardButton.setCallbackData(filmList);

            inlineButtons.add(List.of(inlineKeyboardButton));
        }
        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return inlineKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup optionButtons(Environment environment) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        boolean isDev = Arrays.stream(environment.getActiveProfiles()).anyMatch(profile -> profile.equalsIgnoreCase("dev"));
        KeyboardRow row = new KeyboardRow();
        row.add(newButton(CREATE_NEW_LIST));
        if (isDev) {
            row.add(newButton(LOGOUT));
            row.add(newButton(REMOVE_USER_LIST));
        } else {
            row.add(newButton(REMOVE_USER_LIST));
        }
        keyboardRows.add(row);
        return new ReplyKeyboardMarkup(keyboardRows);
    }

    public static ReplyKeyboardMarkup addFilmDeleteFilmBackLogoutButtons() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardRow row1 = new KeyboardRow();
        row.add(newButton(ADD_FILM));
        row.add(newButton(REMOVE_FILM));
        row1.add(newButton(BACK));
        row1.add(newButton(LOGOUT));
        keyboardRows.add(row);
        keyboardRows.add(row1);
        return new ReplyKeyboardMarkup(keyboardRows);
    }

    public static ReplyKeyboardMarkup deleteFilmBackMarkAsWatchedButtons() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardRow row1 = new KeyboardRow();
        row.add(newButton(MARK_AS_WATCHED));
        row1.add(newButton(BACK));
        row1.add(newButton(REMOVE_FILM));
        keyboardRows.add(row);
        keyboardRows.add(row1);
        return new ReplyKeyboardMarkup(keyboardRows);
    }

    private static KeyboardButton newButton(String text) {
        return new KeyboardButton(text);
    }
}
