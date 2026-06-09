/*

Тест shouldLoginSuccessfully() проверяет позитивный сценарий входа пользователя в систему.
Проверяется полный цикл: регистрация нового пользователя → авторизация → проверка корректности ответа сервера.

 */


package UserTestPackage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import User.Credentials;
import User.User;
import User.UserActivities;
import User.GenerateUserData;

import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.*;

@Epic("Тестирование API пользователей")
@Feature("Аутентификация пользователя")
public class UserLoginPositiveTest {
    private UserActivities userActivities;
    private User testUser;
    private String accessToken;

    @Before
    public void setUp() {
        userActivities = new UserActivities();
        testUser = GenerateUserData.createUniqueUser();
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userActivities.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Вход пользователя")
    @Description("Проверка успешного входа зарегистрированного пользователя в систему")
    public void shouldLoginSuccessfully() {
        createTestUser();
        performLogin();
        verifySuccessfulLogin();
    }

    @Step("Создать тестового пользователя для аутентификации")
    private void createTestUser() {
        ValidatableResponse createUserResponse = userActivities.createUser(testUser);

        // Добавить вложение с данными создания пользователя
        io.qameta.allure.Allure.addAttachment(
                "Create user request data",
                "text/plain",
                testUser.toString()
        );
        io.qameta.allure.Allure.addAttachment(
                "Create user response",
                "application/json",
                createUserResponse.extract().asString()
        );

        int createStatusCode = createUserResponse.extract().statusCode();
        boolean isUserCreated = createUserResponse.extract().path("success");

        assertEquals("Пользователь должен быть успешно создан", SC_OK, createStatusCode);
        assertTrue("Пользователь должен быть создан", isUserCreated);
    }

    @Step("Выполнить вход пользователя в систему")
    private void performLogin() {
        ValidatableResponse loginUserResponse = userActivities.loginUser(Credentials.from(testUser));

        // Добавить вложение с данными запроса на вход
        io.qameta.allure.Allure.addAttachment(
                "Login request data",
                "text/plain",
                testUser.toString()
        );
        io.qameta.allure.Allure.addAttachment(
                "Login response data",
                "application/json",
                loginUserResponse.extract().asString()
        );

        int statusCode = loginUserResponse.extract().statusCode();
        boolean isUserLoggedIn = loginUserResponse.extract().path("success");
        accessToken = loginUserResponse.extract().path("accessToken");

        setLoginResults(statusCode, isUserLoggedIn);
    }

    // Поля для хранения результатов аутентификации
    private int loginStatusCode;
    private boolean userLoggedIn;

    private void setLoginResults(int statusCode, boolean isUserLoggedIn) {
        this.loginStatusCode = statusCode;
        this.userLoggedIn = isUserLoggedIn;
    }

    @Step("Проверить успешный вход пользователя")
    private void verifySuccessfulLogin() {
        assertEquals("Ожидается статус 200", SC_OK, loginStatusCode);
        assertTrue("Ожидается успешный вход", userLoggedIn);
        assertNotNull("Токен доступа должен быть получен", accessToken);
    }
}
