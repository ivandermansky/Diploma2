/*
Тест использует параметризованный подход (@RunWith(Parameterized.class)) для проверки обработки ошибок при создании пользователя с неполными данными.
Вместо написания отдельных тестов для каждого сценария — один шаблон теста выполняется с разными наборами входных данных.

В тесте определены два сценария:
1. Пользователь, у которого заполнено только поле email
2. Пользователь, у которого заполнено только поле password
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
import User.User;
import User.UserActivities;
import User.GenerateUserData;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)

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
    @Description("Подготовка тестового окружения:\n" +
            "- инициализируется UserActivities.")
    public void setUp() {
        userActivities = new UserActivities();
    }

    @Test
    @DisplayName("Создание пользователя с пропущенным обязательным полем")
    @Description("Проверка обработки ошибок при отсутствии обязательных полей в запросе создания пользователя.\n\n" +
            "Шаги теста:\n" +
            "- отправляется запрос на создание пользователя с неполными данными;\n" +
            "- прикрепляются данные запроса и ответа к отчёту Allure;\n" +
            "- извлекаются статус ответа, success и сообщение из тела ответа;\n" +
            "- проверяются статус, success: false и сообщение об ошибке.\n\n" +
            "Ожидаемый результат:\n" +
            "- сервер возвращает статус 403 Forbidden для запросов с неполными данными;\n" +
            "- в теле ответа содержится поле success: false;\n" +
            "- возвращается сообщение с указанием обязательных полей.")
    public void shouldNotCreateUserWithMissingField() {
        performUserCreationRequest();
        verifyErrorResponse();
    }

    @Step("Отправить запрос на создание пользователя с неполными данными")
    @Description("Отправка запроса на создание пользователя с неполным набором данных и сохранение результатов для проверки.\n" +
            "Действия:\n" +
            "- выполняется POST‑запрос createUser с данными user;\n" +
            "- прикрепляются данные запроса и ответа к отчёту Allure;\n" +
            "- извлекаются: статус ответа, success, сообщение об ошибке;\n" +
            "- сохраняются результаты в поля класса для последующей проверки.\n\n" +
            "Возвращаемые данные:\n" +
            "- статус ответа;\n" +
            "- флаг успешности создания (success);\n" +
            "- сообщение об ошибке.")
    private void performUserCreationRequest() {
        ValidatableResponse createUserResponse = userActivities.createUser(user);

        // Добавить вложения для отладки в отчёт Allure
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

        // Сохранить результаты в поля класса для проверки
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
    @Description("Проверка параметров ответа при ошибке создания пользователя.\n" +
            "Проверки:\n" +
            "- статус ответа соответствует ожидаемому (403);\n" +
            "- поле success в ответе равно false;\n" +
            "- сообщение об ошибке соответствует ожидаемому тексту.\n\n" +
            "Ожидаемый результат:\n" +
            "- все проверки проходят успешно;\n" +
            "- отчёт чётко показывает, какая проверка не прошла (если есть ошибка).")
    private void verifyErrorResponse() {
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(responseStatusCode)
                .as("Ожидается статус " + expectedStatusCode)
                .isEqualTo(expectedStatusCode);

        softly.assertThat(userCreated)
                .as("Ожидается неуспешное создание пользователя (success: false)")
                .isFalse();

        softly.assertThat(responseMessage)
                .as("Ожидается сообщение об ошибке: '" + expectedMessage + "'")
                .isEqualTo(expectedMessage);

        softly.assertAll();
    }
}
