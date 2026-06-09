/*
Тест shouldNotCreateDuplicateUser() проверяет негативный сценарий создания пользователя.
Цель теста: убедиться, что система не позволяет зарегистрировать дублирующий аккаунт с теми же данными.
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
import User.GenerateUserData;
import User.User;
import User.UserActivities;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class CreateUserNegativeTest {

    private UserActivities userActivities;
    private User testUser;
    private String accessToken;

    @Before
    @Description("Подготовка тестового окружения:\n" +
            "- инициализируется UserActivities;\n" +
            "- создаётся тестовый пользователь с уникальными данными.")
    public void setUp() {
        userActivities = new UserActivities();
        testUser = GenerateUserData.createUniqueUser();
    }

    @After
    @Description("Очистка тестового окружения: удаление тестового пользователя по accessToken, если он существует.")
    public void tearDown() {
        if (accessToken != null) {
            userActivities.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Пользователь не может быть создан дважды")
    @Description("Проверка невозможности создания дублирующего аккаунта пользователя.\n\n" +
            "Шаги теста:\n" +
            "- создаётся пользователь первый раз с уникальными данными;\n" +
            "- проверяется успешность создания (статус 200, success: true);\n" +
            "- сохраняется accessToken для очистки окружения;\n" +
            "- выполняется повторная попытка создания того же пользователя;\n" +
            "- проверяются статус ответа (403), success: false и сообщение 'User already exists'.\n\n" +
            "Ожидаемый результат:\n" +
            "- первый пользователь создаётся успешно;\n" +
            "- при попытке создать дубликат сервер возвращает статус 403;\n" +
            "- в теле ответа содержится success: false;\n" +
            "- возвращается сообщение 'User already exists', указывающее на существование пользователя.")
    public void shouldNotCreateDuplicateUser() {
        createUserFirstTime();
        tryToCreateDuplicateUser();
    }

    @Step("Создать пользователя первый раз")
    @Description("Создание пользователя с уникальными данными и проверка успешности операции.\n" +
            "Проверки:\n" +
            "- статус ответа равен 200 OK;\n" +
            "- поле success в ответе равно true.\n\n" +
            "Ожидаемый результат:\n" +
            "- сервер возвращает статус 200, подтверждая успешное создание пользователя;\n" +
            "- в теле ответа содержится поле success: true.")
    private void createUserFirstTime() {
        ValidatableResponse firstCreateResponse = userActivities.createUser(testUser);
        int firstStatusCode = firstCreateResponse.extract().statusCode();
        boolean isFirstUserCreated = firstCreateResponse.extract().path("success");

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(firstStatusCode)
                .as("Первый пользователь должен быть создан со статусом 200 OK")
                .isEqualTo(SC_OK);

        softly.assertThat(isFirstUserCreated)
                .as("Первый пользователь должен быть успешно создан (success: true)")
                .isTrue();

        softly.assertAll();

        accessToken = firstCreateResponse.extract().path("accessToken");
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
        int secondStatusCode = secondCreateResponse.extract().statusCode();
        boolean isSecondUserCreated = secondCreateResponse.extract().path("success");
        String actualMessage = secondCreateResponse.extract().path("message");

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(secondStatusCode)
                .as("Ожидается статус 403 Forbidden при попытке создать дубликат")
                .isEqualTo(SC_FORBIDDEN);

        softly.assertThat(isSecondUserCreated)
                .as("Повторное создание пользователя должно быть отклонено (success: false)")
                .isFalse();

        softly.assertThat(actualMessage)
                .as("Ожидается сообщение об ошибке 'User already exists'")
                .isEqualTo("User already exists");

        softly.assertAll();
    }
}
