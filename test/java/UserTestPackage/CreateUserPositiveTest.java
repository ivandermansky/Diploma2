
/*
Тест CreateUserPositiveTest выполняет комплексное тестирование эндпоинта создания пользователей, проверяя:

- корректную работу основного функционала (регистрация нового пользователя);
- обработку ошибок (предотвращение дублирования);
- соблюдение бизнес‑правил (уникальность email/логина).
*/


package UserTestPackage;

import User.GenerateUserData;
import User.User;
import User.UserActivities;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

@Epic("Тестирование API пользователей")
@Feature("Создание пользователя")
public class CreateUserPositiveTest {
    private UserActivities userActivities;
    private User testUser;
    private String accessToken;

    @Before
    public void setUp() {
        userActivities = new UserActivities();
        testUser = GenerateUserData.createUniqueUser(); // Генерировать уникальные данные
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userActivities.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание нового пользователя")
    @Description("Проверка успешного создания нового пользователя с уникальными данными")
    public void shouldCreateUserSuccessfully() {
        CreationResult result = createUserAndExtractData();
        verifySuccessfulUserCreation(result);
    }

    @Step("Создать пользователя и извлечь данные из ответа")
    private CreationResult createUserAndExtractData() {
        ValidatableResponse createUserResponse = userActivities.createUser(testUser);

        int statusCode = createUserResponse.extract().statusCode();
        boolean isUserCreated = createUserResponse.extract().path("success");
        String extractedAccessToken = createUserResponse.extract().path("accessToken");

        // Сохраняем данные для Allure
        io.qameta.allure.Allure.addAttachment("Request data", "text/plain",
                testUser.toString());
        io.qameta.allure.Allure.addAttachment("Response data", "application/json",
                createUserResponse.extract().asString());

        return new CreationResult(statusCode, isUserCreated, extractedAccessToken);
    }

    @Step("Проверить успешное создание пользователя")
    private void verifySuccessfulUserCreation(CreationResult result) {
        assertEquals("Ожидается статус 200", SC_OK, result.getStatusCode());
        assertTrue("Ожидается успешное создание пользователя", result.isUserCreated());
        accessToken = result.getAccessToken();
    }

    @Test
    @DisplayName("Попытка создать существующего пользователя")
    @Description("Проверка предотвращения создания дублирующего аккаунта пользователя")
    public void shouldNotCreateExistingUser() {
        String firstAccessToken = createFirstUser();
        tryToCreateDuplicateUser();
        deleteTestUser(firstAccessToken);
    }

    @Step("Создать пользователя первый раз")
    private String createFirstUser() {
        ValidatableResponse firstCreateResponse = userActivities.createUser(testUser);
        return firstCreateResponse.extract().path("accessToken");
    }

    @Step("Попытаться создать того же пользователя повторно")
    private void tryToCreateDuplicateUser() {
        ValidatableResponse secondCreateResponse = userActivities.createUser(testUser);

        int statusCode = secondCreateResponse.extract().statusCode();
        boolean isUserCreated = secondCreateResponse.extract().path("success");
        String errorMessage = secondCreateResponse.extract().path("message");

        io.qameta.allure.Allure.addAttachment("Duplicate request data", "text/plain",
                testUser.toString());
        io.qameta.allure.Allure.addAttachment("Duplicate response", "application/json",
                secondCreateResponse.extract().asString());

        assertEquals("Ожидается статус 403", SC_FORBIDDEN, statusCode);
        assertFalse("Ожидается неуспешное создание пользователя", isUserCreated);
        assertEquals("Ожидается сообщение об ошибке", "User already exists", errorMessage);
    }

    @Step("Удалить тестового пользователя после теста")
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
