/*
Тест shouldCreateOrderWithAuth() проверяет возможность создания заказа авторизованным пользователем:

- создаёт объект Order с тестовыми ингредиентами (фиксированные ID ингредиентов).
- вызывает orderActivities.createOrderWithAuth(accessToken, order) — отправляет POST‑запрос с токеном авторизации.

Тест проверяет:
- статус ответа 200 OK (SC_OK);
- поле success: true в ответе сервера.
*/

package OrderTestPackage;

import Order.Order;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;


public class OrderCreationWithAuthTest {
private OrderActivities orderActivities;
private String accessToken;
private UserActivities userActivities;
private User testUser;

@Before
public void setUp() {
    userActivities = new UserActivities();
    orderActivities = new OrderActivities();

    testUser = GenerateUserData.createUniqueUser();
    ValidatableResponse createUserResponse = userActivities.createUser(testUser);

    int userCreationStatus = createUserResponse.extract().statusCode();
    boolean isUserCreated = createUserResponse.extract().path("success");
    assertEquals("Пользователь должен быть успешно создан", SC_OK, userCreationStatus);
    assertTrue("Пользователь должен быть создан", isUserCreated);

    accessToken = createUserResponse.extract().path("accessToken");
}

@After
public void tearDown() {
    if (accessToken != null) {
        userActivities.deleteUser(accessToken);
    }
}

    @Test
    @DisplayName("Создание заказа авторизованным пользователем")
    @Description("Тест shouldCreateOrderWithAuth() проверяет возможность создания заказа авторизованным пользователем:\n" +
            "- создаёт объект Order с тестовыми ингредиентами (фиксированные ID ингредиентов).\n" +
            "- вызывает orderActivities.createOrderWithAuth(accessToken, order) — отправляет POST‑запрос с токеном авторизации.\n\n" +
            "Тест проверяет:\n" +
            "- статус ответа 200 OK (SC_OK);\n" +
            "- поле success: true в ответе сервера.")
    public void shouldCreateOrderWithAuth() {
        Order order = new Order();
        List<String> ingredients = new ArrayList<>(Arrays.asList("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa6f"));
        order.setIngredients(ingredients);

        ValidatableResponse createOrderResponse = orderActivities.createOrderWithAuth(accessToken, order);
        int statusCode = createOrderResponse.extract().statusCode();
        boolean isOrderCreated = createOrderResponse.extract().path("success");

        // Создать экземпляр SoftAssertions
        SoftAssertions softly = new SoftAssertions();

        // Проверки через softly
        softly.assertThat(statusCode)
                .as("Ожидается статус 200")
                .isEqualTo(SC_OK);

        softly.assertThat(isOrderCreated)
                .as("Ожидается успешное создание заказа")
                .isTrue();

        // Выполнить все собранные проверки
        softly.assertAll();
    }
}
