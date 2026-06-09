/*
Тест CreateUserPositiveTest выполняет комплексное тестирование эндпоинта создания пользователей, проверяя:
- корректную работу основного функционала (регистрация нового пользователя);
- обработку ошибок (предотвращение дублирования);
- соблюдение бизнес‑правил (уникальность email/логина).
*/

package UserTestPackage;

import User.Base;
import User.GenerateUserData;
import User.User;
import User.UserActivities;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class CreateUserPositiveTest extends Base { // ← Наследование от Base
    private UserActivities userActivities;
    private User testUser;
    private String accessToken;

    @Before
    @Description("Подготовка тестового окружения:\n" +
            "- инициализируется UserActivities;\n" +
            "- создаётся тестовый пользователь с уникальными данными (email, логин).")
    public void setUp() {
        userActivities = new UserActivities();
        testUser = GenerateUserData.createUniqueUser(); // Генерировать уникальные данные
    }

    @After
    @Description("Очистка тестового окружения: удаление тестового пользователя по accessToken, если он существует.")
    public void tearDown() {
        if (accessToken != null) {
            userActivities.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание нового пользователя")
    @Description("Проверка успешного создания нового пользователя с уникальными данными.\n\n" +
            "Шаги теста:\n" +
            "- отправляется запрос на создание пользователя с уникальными данными;\n" +
            "- извлекаются статус ответа, поле success и accessToken из тела ответа;\n" +
            "- проверяются статус 200, success: true и наличие accessToken.\n\n" +
            "Ожидаемый результат:\n" +
            "- сервер возвращает статус 200 OK;\n" +
            "- в теле ответа содержится поле success: true;\n" +
            "- возвращается валидный accessToken для созданного пользователя.")
    public void shouldCreateUserSuccessfully() {
        Base.CreationResult result = createUserAndExtractData(); // ← Использовать Base.CreationResult
        verifySuccessfulUserCreation(result);
    }

    @Step("Создать пользователя и извлечь данные из ответа")
    @Description("Отправка запроса на создание пользователя и извлечение ключевых данных из ответа.\n" +
            "Действия:\n" +
            "- выполняется POST‑запрос createUser с данными testUser;\n" +
            "- извлекаются: статус ответа, success, accessToken;\n" +
            "- прикрепляются данные запроса и ответа к отчёту Allure.\n\n" +
            "Возвращаемые данные:\n" +
            "- статус ответа;\n" +
            "- флаг успешности создания (success);\n" +
            "- accessToken (если создан).")
    private Base.CreationResult createUserAndExtractData() { // ← Тип изменён на Base.CreationResult
        ValidatableResponse createUserResponse = userActivities.createUser(testUser);

        // Использовать метод из Base для создания результата
        Base.CreationResult result = Base.createFromResponse(createUserResponse);

        // Сохранить данные для Allure
        io.qameta.allure.Allure.addAttachment("Request data", "text/plain",
                testUser.toString());
        io.qameta.allure.Allure.addAttachment("Response data", "application/json",
                createUserResponse.extract().asString());

        return result;
    }

    @Step("Проверить успешное создание пользователя")
    @Description("Проверка ключевых параметров успешного создания пользователя.\n" +
            "Проверки:\n" +
            "- статус ответа равен 200 OK;\n" +
            "- поле success в ответе равно true;\n" +
            "- accessToken не null (пользователь создан).\n\n" +
            "Ожидаемый результат:\n" +
            "- все проверки проходят успешно;\n" +
            "- accessToken сохраняется для использования в очистке окружения.")
    private void verifySuccessfulUserCreation(Base.CreationResult result) { // ← Параметр изменён на Base.CreationResult
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(result.getStatusCode())
                .as("Ожидается статус 200 OK при успешном создании пользователя")
                .isEqualTo(SC_OK);

        softly.assertThat(result.isUserCreated())
                .as("Ожидается успешное создание пользователя (success: true)")
                .isTrue();

        softly.assertThat(result.getAccessToken())
                .as("AccessToken должен быть возвращён при успешном создании")
                .isNotNull();

        softly.assertAll();

        accessToken = result.getAccessToken();
    }

    @Test
    @DisplayName("Попытка создать существующего пользователя")
    @Description("Проверка предотвращения создания дублирующего аккаунта пользователя.\n\n" +
            "Шаги теста:\n" +
            "- создаётся пользователь первый раз (получается accessToken);\n" +
            "- выполняется повторная попытка создания того же пользователя;\n" +
            "- проверяются статус ответа (403), success: false и сообщение об ошибке.\n\n" +
            "Ожидаемый результат:\n" +
            "- первый пользователь создаётся успешно (статус 200);\n" +
            "- при попытке создать дубликат сервер возвращает статус 403;\n" +
            "- в теле ответа содержится success: false;\n" +
            "- возвращается сообщение 'User already exists', указывающее на существование пользователя.")
    public void shouldNotCreateExistingUser() {
        String firstAccessToken = createFirstUser();
        tryToCreateDuplicateUser();
        deleteTestUser(firstAccessToken);
    }

    @Step("Создать пользователя первый раз")
    @Description("Создание пользователя с уникальными данными для подготовки к проверке дублирования.\n" +
            "Действие:\n" +
            "- выполняется запрос createUser с данными testUser.\n\n" +
            "Возвращаемое значение:\n" +
            "- accessToken созданного пользователя (для последующей очистки).")
    private String createFirstUser() {
        ValidatableResponse firstCreateResponse = userActivities.createUser(testUser);
        return firstCreateResponse.extract().path("accessToken");
    }

    @Step("Попытаться создать того же пользователя повторно")
    @Description("Повторная попытка создания пользователя с теми же данными и проверка корректности обработки ошибки.\n" +
            "Проверки:\n" +
            "- статус ответа равен 403 Forbidden;\n" +
            "- поле success в ответе равно false;\n" +
            "- сообщение об ошибке равно 'User already exists'.\n\n" +
            "Ожидаемый результат:\n" +
            "- сервер возвращает статус 403, указывая на запрет операции;\n" +
            "- в теле ответа содержится поле success: false;\n" +
            "- возвращается сообщение 'User already exists', информирующее о причине отказа.")
    private void tryToCreateDuplicateUser() {
        ValidatableResponse secondCreateResponse = userActivities.createUser(testUser);

        int statusCode = secondCreateResponse.extract().statusCode();
        boolean isUserCreated = secondCreateResponse.extract().path("success");
        String errorMessage = secondCreateResponse.extract().path("message");

        io.qameta.allure.Allure.addAttachment("Duplicate request data", "text/plain",
                testUser.toString());
        io.qameta.allure.Allure.addAttachment("Duplicate response", "application/json",
                secondCreateResponse.extract().asString());

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(statusCode)
                .as("Ожидается статус 403 Forbidden при попытке создать дубликат")
                .isEqualTo(SC_FORBIDDEN);

        softly.assertThat(isUserCreated)
                .as("Повторное создание пользователя должно быть отклонено (success: false)")
                .isFalse();

        softly.assertThat(errorMessage)
                .as("Ожидается сообщение об ошибке 'User already exists'")
                .isEqualTo("User already exists");

        softly.assertAll();
    }

    @Step("Удалить тестового пользователя после теста")
    @Description("Очистка тестового окружения: удаление пользователя, созданного в ходе теста.\n" +
            "Действие:\n" +
            "- выполняется запрос deleteUser с указанным accessToken.\n\n" +
            "Условия выполнения:\n" +
            "- операция выполняется только если accessToken не null.")
    private void deleteTestUser(String accessToken) {
        if (accessToken != null) {
            userActivities.deleteUser(accessToken);
        }
    }

    // Вспомогательный класс для передачи данных между методами
    private static class CreationResult {
        private final int statusCode;
        private final boolean isUserCreated;
        private final String accessToken;

        public CreationResult(int statusCode, boolean isUserCreated, String accessToken) {
            this.statusCode = statusCode;
            this.isUserCreated = isUserCreated;
            this.accessToken = accessToken;
        }

        public int getStatusCode() { return statusCode; }
        public boolean isUserCreated() { return isUserCreated; }
        public String getAccessToken() { return accessToken; }
    }
}
