/*
Тест использует параметризованный подход (@RunWith(Parameterized.class)) для проверки обработки ошибок при создании пользователя с неполными данными.
Вместо написания отдельных тестов для каждого сценария — один шаблон теста выполняется с разными наборами входных данных.

В тесте определены два сценария:
1. Пользователь, у которого заполнено только поле email
2. Пользователь, у которого заполнено только поле password

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
import User.User;
import User.UserActivities;
import User.GenerateUserData;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
@Epic("Тестирование API пользователей")
@Feature("Валидация обязательных полей при создании пользователя")
public class CreateUserWithMissingFields {
    private UserActivities userActivities;
    private final User user;
    private final int expectedStatusCode;
    private final String expectedMessage;

    public CreateUserWithMissingFields(User user, int expectedStatusCode, String expectedMessage) {
        this.user = user;
        this.expectedStatusCode = expectedStatusCode;
        this.expectedMessage = expectedMessage;
    }

    @Parameterized.Parameters(name = "Тест {index}: {0} → статус {1}")
    public static Object[][] getTestData() {
        return new Object[][]{
                {
                        GenerateUserData.createUserWithEmailOnly(),
                        SC_FORBIDDEN,
                        "Email, password and name are required fields"
                },
                {
                        GenerateUserData.createUserWithPasswordOnly(),
                        SC_FORBIDDEN,
                        "Email, password and name are required fields"
                }
        };
    }

    @Before
    public void setUp() {
        userActivities = new UserActivities();
    }

    @Test
    @DisplayName("Создание пользователя с пропущенным обязательным полем")
    @Description("Проверка обработки ошибок при отсутствии обязательных полей в запросе создания пользователя")
    public void shouldNotCreateUserWithMissingField() {
        performUserCreationRequest();
        verifyErrorResponse();
    }

    @Step("Отправить запрос на создание пользователя с неполными данными")
    private void performUserCreationRequest() {
        ValidatableResponse createUserResponse = userActivities.createUser(user);

        // Добавляем вложения для отладки в отчёт Allure
        io.qameta.allure.Allure.addAttachment(
                "Request data (missing fields)",
                "text/plain",
                user.toString()
        );
        io.qameta.allure.Allure.addAttachment(
                "Response data",
                "application/json",
                createUserResponse.extract().asString()
        );

        int statusCode = createUserResponse.extract().statusCode();
        boolean isUserCreated = createUserResponse.extract().path("success");
        String actualMessage = createUserResponse.extract().path("message");

        // Сохраняем результаты в поля класса для проверки
        setTestResults(statusCode, isUserCreated, actualMessage);
    }

    // Поля для хранения результатов запроса
    private int responseStatusCode;
    private boolean userCreated;
    private String responseMessage;

    private void setTestResults(int statusCode, boolean isUserCreated, String message) {
        this.responseStatusCode = statusCode;
        this.userCreated = isUserCreated;
        this.responseMessage = message;
    }

    @Step("Проверить ответ об ошибке")
    private void verifyErrorResponse() {
        assertEquals(
                "Ожидается статус " + expectedStatusCode,
                expectedStatusCode,
                responseStatusCode
        );
        assertFalse(
                "Ожидается неуспешное создание пользователя",
                userCreated
        );
        assertEquals(
                "Ожидается сообщение об ошибке",
                expectedMessage,
                responseMessage
        );
    }
}
