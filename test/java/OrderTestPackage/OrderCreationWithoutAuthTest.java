/*
Тест shouldNotCreateOrderWithoutAuth() проверяет невозможность создания заказа неавторизованным пользователем:
- создаёт объект Order с теми же ингредиентами.
- вызывает orderActivities.createOrderWithoutAuth(order) — отправляет POST‑запрос без токена авторизации.
- логирует статус и тело ответа для диагностики.

Проверяет:
- статус ответа 401 Unauthorized (SC_UNAUTHORIZED);
- поле success: false в ответе.

Ожидаемый результат: сервер отклоняет запрос, возвращает 401.
!!! Текущий баг: тест падает, потому что сервер возвращает 200 и создаёт заказ даже без авторизации !!!
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.Assert.*;

public class OrderCreationWithoutAuthTest {
private OrderActivities orderActivities;
private String accessToken;
private UserActivities userActivities;
private User testUser;

@Before
@Description("Подготовка тестового окружения:\n" +
        "- создаётся тестовый пользователь;\n" +
        "- проверяется успешность создания пользователя (статус 200);\n" +
        "- сохраняется accessToken для последующего удаления пользователя.")
public void setUp() {
    userActivities = new UserActivities();
    orderActivities = new OrderActivities();

    testUser = GenerateUserData.createUniqueUser();
    ValidatableResponse createUserResponse = userActivities.createUser(testUser);

    int userCreationStatus = createUserResponse.extract().statusCode();
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
@DisplayName("Создание заказа неавторизованным пользователем")
@Description("Тест проверяет, что сервер отклоняет создание заказа без авторизации.\n\n" +
        "Шаги теста:\n" +
        "- создаётся объект Order с фиксированным набором ингредиентов;\n" +
        "- отправляется POST‑запрос на создание заказа без токена авторизации (createOrderWithoutAuth);\n" +
        "- логируются статус и тело ответа сервера для диагностики;\n" +
        "- извлекаются статус ответа и значение поля success из тела ответа.\n\n" +
        "Проверки:\n" +
        "- статус ответа равен 401 (SC_UNAUTHORIZED);\n" +
        "- поле success в ответе равно false.\n\n" +
        "Ожидаемый результат:\n" +
        "- сервер возвращает статус 401, отклоняя неавторизованный запрос;\n" +
        "- в ответе поле success: false, заказ не создан.\n\n" +
        "!!! Текущий баг системы:\n" +
        "- сервер возвращает статус 200 OK;\n" +
        "- создаёт заказ, несмотря на отсутствие авторизации.")
public void shouldNotCreateOrderWithoutAuth() {
    Order order = new Order();
    List<String> ingredients = new ArrayList<>(Arrays.asList("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa6f"));
    order.setIngredients(ingredients);

    ValidatableResponse createOrderResponse = orderActivities.createOrderWithoutAuth(order);

    System.out.println("Статус ответа: " + createOrderResponse.extract().statusCode());
    System.out.println("Тело ответа: " + createOrderResponse.extract().body().asString());

    int statusCode = createOrderResponse.extract().statusCode();
    boolean isOrderCreated = createOrderResponse.extract().path("success");

    // Проверка статуса 401 (протокол)
    assertEquals("Ожидается статус 401", SC_UNAUTHORIZED, statusCode);

    // Проверка success: false (бизнес‑логика)
    assertFalse("Ожидается неуспешное создание заказа", isOrderCreated);
}
}
