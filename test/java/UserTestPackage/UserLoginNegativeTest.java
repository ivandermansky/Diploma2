/*
Тест использует параметризованный подход (@RunWith(Parameterized.class)) для проверки обработки ошибок при попытке входа пользователя с неполными или некорректными данными.
*/

package UserTestPackage;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import User.Credentials;
import User.User;
import User.UserActivities;
import User.GenerateUserData;

import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)

public class UserLoginNegativeTest {
    private UserActivities userActivities;
    private final User user;
    private final int expectedStatusCode;
    private final String expectedMessage;

    // Поля для хранения результатов запроса
    private int responseStatusCode;
    private boolean userLoggedIn;
    private String responseMessage;

    public UserLoginNegativeTest(User user, int expectedStatusCode, String expectedMessage) {
        this.user = user;
        this.expectedStatusCode = expectedStatusCode;
        this.expectedMessage = expectedMessage;
    }

    @Parameterized.Parameters(name = "Тест {index}: {0} → статус {1}")
    public static Object[][] getTestData() {
        return new Object[][]{
                {
                        GenerateUserData.createUserWithEmailOnly(),
                        SC_UNAUTHORIZED,
                        "email or password are incorrect"
                },
                {
                        GenerateUserData.createUserWithPasswordOnly(),
                        SC_UNAUTHORIZED,
                        "email or password are incorrect"
                }
        };
    }

    @Before
    @Description("Подготовка тестового окружения:\n" +
            "- инициализируется UserActivities.")
    public void setUp() {
        userActivities = new UserActivities();
    }

    @Test
    @DisplayName("Вход пользователя с пропущенным обязательным полем")
    @Description("Проверка обработки ошибок при отсутствии обязательных полей в запросе аутентификации.\n\n" +
            "Шаги теста:\n" +
            "- отправляется запрос на аутентификацию с неполными данными;\n" +
            "- прикрепляются данные запроса и ответа к отчёту Allure;\n" +
            "- извлекаются статус ответа, success и сообщение из тела ответа;\n" +
            "- проверяются статус, success: false и сообщение об ошибке.\n\n" +
            "Ожидаемый результат:\n" +
            "- сервер возвращает статус 401 Unauthorized для запросов с неполными данными;\n" +
            "- в теле ответа содержится поле success: false;\n" +
            "- возвращается сообщение с указанием на некорректные данные аутентификации.")
    public void shouldNotLoginWithMissingField() {
        performLoginRequest();
        verifyLoginErrorResponse();
    }

    @Step("Отправить запрос на аутентификацию с неполными данными")
    @Description("Отправка запроса на аутентификацию с неполным набором данных и сохранение результатов для проверки.\n" +
            "Действия:\n" +
            "- выполняется POST‑запрос loginUser с данными user;\n" +
            "- прикрепляются данные запроса и ответа к отчёту Allure;\n" +
            "- извлекаются: статус ответа, success, сообщение об ошибке;\n" +
            "- сохраняются результаты в поля класса для последующей проверки.\n\n" +
            "Возвращаемые данные:\n" +
            "- статус ответа;\n" +
            "- флаг успешности аутентификации (success);\n" +
            "- сообщение об ошибке.")
    private void performLoginRequest() {
        ValidatableResponse loginUserResponse = userActivities.loginUser(Credentials.from(user));

        // Добавить вложения для отладки в отчёт Allure
        io.qameta.allure.Allure.addAttachment(
                "Login request data (missing fields)",
                "text/plain",
                user.toString()
        );
        io.qameta.allure.Allure.addAttachment(
                "Login response data",
                "application/json",
                loginUserResponse.extract().asString()
        );

        int statusCode = loginUserResponse.extract().statusCode();
        boolean isUserLoggedIn = loginUserResponse.extract().path("success");
        String actualMessage = loginUserResponse.extract().path("message");

        // Сохранить результаты для проверки
        setTestResults(statusCode, isUserLoggedIn, actualMessage);
    }

    public void setTestResults(int statusCode, boolean isUserLoggedIn, String message) {
        this.responseStatusCode = statusCode;
        this.userLoggedIn = isUserLoggedIn;
        this.responseMessage = message;
    }

    @Step("Проверить ответ об ошибке аутентификации")
    @Description("Проверка параметров ответа при ошибке аутентификации.\n" +
            "Проверки:\n" +
            "- статус ответа соответствует ожидаемому (401);\n" +
            "- поле success в ответе равно false;\n" +
            "- сообщение об ошибке соответствует ожидаемому тексту.\n\n" +
            "Ожидаемый результат:\n" +
            "- все проверки проходят успешно;\n" +
            "- отчёт чётко показывает, какая проверка не прошла (если есть ошибка).")
    public void verifyLoginErrorResponse() {
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(responseStatusCode)
                .as("Ожидается статус " + expectedStatusCode)
                .isEqualTo(expectedStatusCode);

        softly.assertThat(userLoggedIn)
                .as("Ожидается неуспешный вход (success: false)")
                .isFalse();

        softly.assertThat(responseMessage)
                .as("Ожидается сообщение об ошибке: '" + expectedMessage + "'")
                .isEqualTo(expectedMessage);

        softly.assertAll();
    }
}
