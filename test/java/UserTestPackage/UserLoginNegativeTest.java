/*
Тест shouldLoginSuccessfully() проверяет позитивный сценарий входа пользователя в систему.
Проверяется полный цикл: регистрация нового пользователя → авторизация → проверка корректности ответа сервера.
*/

package UserTestPackage;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import User.Credentials;
import User.User;
import User.UserActivities;
import User.GenerateUserData;

import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.*;

public class UserLoginPositiveTest {
    private UserActivities userActivities;
    private User testUser;
    private String accessToken;

    // Поля для хранения результатов создания пользователя
    private int createStatusCode;
    private boolean userCreated;

    // Поля для хранения результатов аутентификации
    private int loginStatusCode;
    private boolean userLoggedIn;

    @Before
    @Description("Подготовка тестового окружения:\n" +
            "- инициализируется UserActivities;\n" +
            "- создаётся уникальный тестовый пользователь.")
    public void setUp() {
        userActivities = new UserActivities();
        testUser = GenerateUserData.createUniqueUser();
    }

    @After
    @Description("Очистка тестовых данных:\n" +
            "- удаляется тестовый пользователь по accessToken (если он был получен).")
    public void tearDown() {
        if (accessToken != null) {
            userActivities.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Успешный вход пользователя в систему")
    @Description("Проверка полного цикла: регистрация → авторизация → валидация ответа сервера.\n\n" +
            "Шаги теста:\n" +
            "- создаётся новый пользователь;\n" +
            "- выполняется запрос на аутентификацию с корректными данными;\n" +
            "- проверяются статус, success: true и наличие accessToken;\n" +
            "- прикрепляются данные запросов и ответов к отчёту Allure.\n\n" +
            "Ожидаемый результат:\n" +
            "- сервер возвращает статус 200 OK для корректных данных;\n" +
            "- в теле ответа содержится поле success: true;\n" +
            "- возвращается валидный токен доступа.")
    public void shouldLoginSuccessfully() {
        createTestUser();
        performLogin();
        verifySuccessfulLogin();
    }

    @Step("Создать тестового пользователя для аутентификации")
    @Description("Отправка запроса на создание нового пользователя и сохранение результатов для проверки.\n" +
            "Действия:\n" +
            "- выполняется POST‑запрос createUser с данными testUser;\n" +
            "- прикрепляются данные запроса и ответа к отчёту Allure;\n" +
            "- извлекаются статус ответа и флаг success;\n" +
            "- сохраняются результаты в поля класса для последующей проверки.\n\n" +
            "Возвращаемые данные:\n" +
            "- статус ответа;\n" +
            "- флаг успешности создания пользователя (success).")
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

        int statusCode = createUserResponse.extract().statusCode();
        boolean isUserCreated = createUserResponse.extract().path("success");

        setCreateUserResults(statusCode, isUserCreated);
    }

    private void setCreateUserResults(int statusCode, boolean isUserCreated) {
        this.createStatusCode = statusCode;
        this.userCreated = isUserCreated;
    }

    @Step("Выполнить вход пользователя в систему")
    @Description("Отправка запроса на аутентификацию с корректными учётными данными и сохранение результатов.\n" +
            "Действия:\n" +
            "- выполняется POST‑запрос loginUser с данными testUser;\n" +
            "- прикрепляются данные запроса и ответа к отчёту Allure;\n" +
            "- извлекаются статус ответа, success и accessToken;\n" +
            "- сохраняются результаты в поля класса для последующей проверки.\n\n" +
            "Возвращаемые данные:\n" +
            "- статус ответа;\n" +
            "- флаг успешности входа (success);\n" +
            "- токен доступа (accessToken).")
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

    private void setLoginResults(int statusCode, boolean isUserLoggedIn) {
        this.loginStatusCode = statusCode;
        this.userLoggedIn = isUserLoggedIn;
    }

    @Step("Проверить успешный вход пользователя")
    @Description("Проверка параметров ответа при успешной аутентификации.\n" +
            "Проверки:\n" +
            "- статус ответа соответствует ожидаемому (200);\n" +
            "- поле success в ответе равно true;\n" +
            "- accessToken не равен null.\n\n" +
            "Ожидаемый результат:\n" +
            "- все проверки проходят успешно;\n" +
            "- отчёт чётко показывает, какая проверка не прошла (если есть ошибка).")
    private void verifySuccessfulLogin() {
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(createStatusCode)
                .as("Ожидается статус 200 при создании пользователя")
                .isEqualTo(SC_OK);

        softly.assertThat(userCreated)
                .as("Пользователь должен быть успешно создан")
                .isTrue();

        softly.assertThat(loginStatusCode)
                .as("Ожидается статус 200 при входе")
                .isEqualTo(SC_OK);

        softly.assertThat(userLoggedIn)
                .as("Ожидается успешный вход (success: true)")
                .isTrue();

        softly.assertThat(accessToken)
                .as("Токен доступа должен быть получен")
                .isNotNull();

        softly.assertAll();
    }
}
