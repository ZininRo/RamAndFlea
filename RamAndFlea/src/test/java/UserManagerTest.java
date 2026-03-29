
import org.example.Model.User;
import org.example.Model.UserManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import java.lang.reflect.Field;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {
    private UserManager userManager;
    private static final String FILE_NAME = "users.json";
    private static final String BACKUP_FILE = "users_backup.json";

    @BeforeEach
    void setUp() throws Exception {
        File original = new File(FILE_NAME);
        File backup = new File(BACKUP_FILE);
        if (original.exists()) {
            Files.copy(original.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            original.delete(); // убирает оригинал
        }
        userManager = new UserManager();
        // очищение памяти
        Field usersField = UserManager.class.getDeclaredField("users");
        usersField.setAccessible(true);
        Map<String, User> users = (Map<String, User>) usersField.get(userManager);
        users.clear();
    }

    @AfterEach
    void tearDown() throws IOException {
        File original = new File(FILE_NAME);
        File backup = new File(BACKUP_FILE);
        // удаляем тестовый файл
        if (original.exists()) {
            original.delete();
        }
        // возвращаем оригинал
        if (backup.exists()) {
            Files.move(backup.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Test
    void testRegisterAndLogin() {
        boolean registered = userManager.registerUser("testuser", "pass");
        assertTrue(registered);
        User logged = userManager.loginUser("testuser", "pass");
        assertNotNull(logged);
        assertEquals("testuser", logged.getUsername());
        assertEquals("pass", logged.getPassword());
        User wrong = userManager.loginUser("testuser", "wrong");
        assertNull(wrong);
    }

    @Test
    void testRegisterDuplicate() {
        assertTrue(userManager.registerUser("user1", "pass"));
        assertFalse(userManager.registerUser("user1", "pass2"));
    }

    @Test
    void testUpdateLevel() {
        userManager.registerUser("player", "pwd");
        User user = userManager.loginUser("player", "pwd");
        userManager.updateLevel(user, 1, 2);
        assertEquals(2, user.getLevelStars(1));
    }

    @Test
    void testResetUserProgress() {
        userManager.registerUser("prog", "pwd");
        User user = userManager.loginUser("prog", "pwd");
        user.setLevelStars(1, 3);
        user.setLevelStars(2, 2);
        userManager.resetUserProgress(user);
        assertEquals(0, user.getLevelStars(1));
        assertEquals(0, user.getLevelStars(2));
    }

    @Test
    void testDeleteUser() {
        userManager.registerUser("todelete", "pwd");
        assertTrue(userManager.deleteUser("todelete"));
        assertNull(userManager.loginUser("todelete", "pwd"));
        assertFalse(userManager.deleteUser("nonexistent"));
    }

    @Test
    void testGetUserByUsername() {
        userManager.registerUser("alice", "123");
        User u = userManager.getUserByUsername("alice");
        assertNotNull(u);
        assertEquals("alice", u.getUsername());
        assertNull(userManager.getUserByUsername("bob"));
    }

    @Test
    void testLevelUnlockLogic() {
        userManager.registerUser("progress", "pwd");
        User user = userManager.loginUser("progress", "pwd");
        assertTrue(user.isLevelUnlocked(1));
        assertFalse(user.isLevelUnlocked(2));
        user.setLevelStars(1, 1);
        assertTrue(user.isLevelUnlocked(2));
        assertFalse(user.isLevelUnlocked(3));
        user.setLevelStars(2, 1);
        assertTrue(user.isLevelUnlocked(3));
    }
}