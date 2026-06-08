/*
Тест shouldNotCreateOrderWithoutIngredients() проверяет обработку ошибки при создании заказа без ингредиентов.
Вызывает orderActivities.createOrderWithoutIngredients(accessToken) — отправляет запрос без списка ингредиентов.

Проверяет:
- статус 400 Bad Request (SC_BAD_REQUEST);
- success: false;
- сообщение об ошибке "Ingredient ids must be provided".

Ожидаемый результат: сервер возвращает ошибку валидации.
*/

package OrderTestPackage;

import Order.OrderActivities;
import User.GenerateUserData;
import User.User;
import User.UserActivities;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.*;



public class OrderCreationWithoutIngredientsTest {
private OrderActivities orderActivities;
private String accessToken;
private UserActivities userActivities;
private User testUser;

@Before
@Description("Подготовка тестового окружения:\n" +
        "- создаётся тестовый пользователь;\n" +
        "- проверяется успешность создания пользователя (статус 200 и success: true);\n" +
        "- сохраняется accessToken для последующих операций.")
public void setUp() {
    userActivities = new UserActivities();
    orderActivities = new OrderActivities();

    testUser = GenerateUserData.createUniqueUser();
    ValidatableResponse createUserResponse = userActivities.createUser(testUser);

    int userCreationStatus = createUserResponse.extract().statusCode();
    boolean isUserCreated = createUserResponse.extract().path("success");
    assertEquals("Пользователь должен быть успешно создан", SC_OK, userCreationStatus);

    accessToken = createUserResponse.extract().path("accessToken");
}

@After
@Description("Очистка тестового окружения: удаление тестового пользователя по accessToken, если он существует.")
public void tearDown() {
    if (accessToken != null) {
        userActivities.deleteUser(accessToken);
    }
}

@Test
@DisplayName("Создание заказа без ингредиентов")
@Description("Тест проверяет, что сервер корректно обрабатывает запрос на создание заказа без ингредиентов.\n\n" +
        "Шаги теста:\n" +
        "- выполняется запрос на создание заказа без списка ингредиентов с использованием accessToken;\n" +
        "- извлекаются статус ответа, значение поля success и сообщение об ошибке из тела ответа.\n\n" +
        "Проверки:\n" +
        "- статус ответа равен 400 (SC_BAD_REQUEST);\n" +
        "- поле success в ответе равно false (заказ не создан);\n" +
        "- сообщение об ошибке в ответе равно 'Ingredient ids must be provided'.\n\n" +
        "Ожидаемый результат:\n" +
        "- сервер возвращает статус 400 Bad Request, указывая на ошибку валидации входных данных;\n" +
        "- в теле ответа содержится чёткое сообщение о том, что ID ингредиентов не предоставлены;\n" +
        "- бизнес‑логика предотвращает создание некорректного заказа.")
public void shouldNotCreateOrderWithoutIngredients() {
    ValidatableResponse createOrderResponse = orderActivities.createOrderWithoutIngredients(accessToken);
    int statusCode = createOrderResponse.extract().statusCode();
    boolean isOrderCreated = createOrderResponse.extract().path("success");
    String errorMessage = createOrderResponse.extract().path("message");

    SoftAssertions softly = new SoftAssertions();

    softly.assertThat(statusCode)
            .as("Ожидается статус 400 Bad Request")
            .isEqualTo(SC_BAD_REQUEST);

    softly.assertThat(isOrderCreated)
            .as("Ожидается неуспешное создание заказа (success: false)")
            .isFalse();

    softly.assertThat(errorMessage)
            .as("Ожидается сообщение об ошибке 'Ingredient ids must be provided'")
            .isEqualTo("Ingredient ids must be provided");

    softly.assertAll();
}
}
