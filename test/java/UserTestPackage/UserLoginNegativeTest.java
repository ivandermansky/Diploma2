/*

Тест использует параметризованный подход (@RunWith(Parameterized.class)) для проверки обработки ошибок при попытке входа пользователя с неполными или некорректными данными.

 */


package UserTestPackage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
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
@Epic("Тестирование API пользователей")
@Feature("Аутентификация пользователя")
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
    public void setUp() {
        userActivities = new UserActivities();
    }

    @Test
    @DisplayName("Вход пользователя с пропущенным обязательным полем")
    @Description("Проверка обработки ошибок при отсутствии обязательных полей в запросе аутентификации")
    public void shouldNotLoginWithMissingField() {
        performLoginRequest();
        verifyLoginErrorResponse();
    }

    @Step("Отправить запрос на аутентификацию с неполными данными")
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

    private void setTestResults(int statusCode, boolean isUserLoggedIn, String message) {
        this.responseStatusCode = statusCode;
        this.userLoggedIn = isUserLoggedIn;
        this.responseMessage = message;
    }

    @Step("Проверить ответ об ошибке аутентификации")
    private void verifyLoginErrorResponse() {
        assertEquals(
                "Ожидается статус " + expectedStatusCode,
                expectedStatusCode,
                responseStatusCode
        );
        assertFalse(
                "Ожидается неуспешный вход",
                userLoggedIn
        );
        assertEquals(
                "Ожидается сообщение об ошибке",
                expectedMessage,
                responseMessage
        );
    }
}
