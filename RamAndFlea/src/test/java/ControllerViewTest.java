import org.example.Controller.WindowController;
import org.example.Model.UserManager;
import org.example.View.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllerViewTest {

    @Mock
    private StartWindow startWindow;
    @Mock
    private GameWindow gameWindow;
    @Mock
    private LoginWindow loginWindow;
    @Mock
    private AccountWindow accountWindow;
    @Mock
    private UserManager userManager;
    @Mock
    private JFrame frame;
    @Mock
    private Container contentPane;

    private WindowController controller;

    @BeforeEach
    void setUp() throws Exception {
        // Настраиваем мок фрейма
        when(frame.getContentPane()).thenReturn(contentPane);

        // Создаём реальный контроллер
        controller = new WindowController();

        // Подменяем приватные поля через рефлексию
        setPrivateField(controller, "startWindow", startWindow);
        setPrivateField(controller, "gameWindow", gameWindow);
        setPrivateField(controller, "loginWindow", loginWindow);
        setPrivateField(controller, "accountWindow", accountWindow);
        setPrivateField(controller, "userManager", userManager);
        setPrivateField(controller, "frame", frame);
        // Сбрасываем состояние, которое могло быть установлено в конструкторе
        setPrivateField(controller, "state", null);
    }

    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(obj, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }


    @Test
    void testSwitchToStartAddsStartWindow() {
        controller.switchTo(GameState.START);
        verify(frame).add(startWindow);
        verify(frame).revalidate();
        verify(frame).repaint();
        assertEquals(GameState.START, controller.getState());
    }

    @Test
    void testSwitchToGameAddsGameWindow() {
        controller.switchTo(GameState.GAME);
        verify(frame).add(gameWindow);
        assertEquals(GameState.GAME, controller.getState());
    }

    @Test
    void testSwitchToLoginAddsLoginWindow() {
        controller.switchTo(GameState.LOGIN);
        verify(frame).add(loginWindow);
        assertEquals(GameState.LOGIN, controller.getState());
    }

    @Test
    void testSwitchToAccountAddsAccountWindow() {
        controller.switchTo(GameState.ACCOUNT);
        verify(frame).add(accountWindow);
        assertEquals(GameState.ACCOUNT, controller.getState());
    }

    @Test
    void testSwitchToCustomPanel() {
        JPanel customPanel = new JPanel();
        controller.switchTo(customPanel);
        verify(frame).add(customPanel);
    }


}