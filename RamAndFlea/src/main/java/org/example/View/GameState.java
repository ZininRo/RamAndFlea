package org.example.View;
/**
 * Перечисление состояний игры (экранов приложения).
 * Определяет, какое окно в данный момент отображается в главном фрейме.
 */
public enum GameState {
    START, // Главное меню (StartWindow)
    GAME, //Окно выбора уровня (GameWindow)
    LOGIN, //Окно входа/регистрации (LoginWindow)
    ACCOUNT // Окно аккаунта (AccountWindow)
}