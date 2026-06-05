/*

Тест shouldCreateOrderWithAuth() проверяет возможность создания заказа авторизованным пользователем
Создаёт объект Order с тестовыми ингредиентами (фиксированные ID ингредиентов).
Вызывает orderActivities.createOrderWithAuth(accessToken, order) — отправляет POST‑запрос с токеном авторизации.
Проверяет:
- статус ответа 200 OK (SC_OK);
- поле success: true в ответе сервера.


Тест shouldNotCreateOrderWithoutAuth() проверяет невозможность создания заказа неавторизованным пользователем.
Создаёт объект Order с теми же ингредиентами.
Вызывает orderActivities.createOrderWithoutAuth(order) — отправляет POST‑запрос без токена авторизации.
Логирует статус и тело ответа для диагностики.
Проверяет:
- статус ответа 401 Unauthorized (SC_UNAUTHORIZED);
- поле success: false в ответе.
Ожидаемый результат: сервер отклоняет запрос, возвращает 401.
!!! Текущий баг: тест падает, потому что сервер возвращает 200 и создаёт заказ даже без авторизации !!!


Тест shouldNotCreateOrderWithoutIngredients() проверяет обработку ошибки при создании заказа без ингредиентов.
Вызывает orderActivities.createOrderWithoutIngredients(accessToken) — отправляет запрос без списка ингредиентов.
Проверяет:
- статус 400 Bad Request (SC_BAD_REQUEST);
- success: false;
- сообщение об ошибке "Ingredient ids must be provided".
Ожидаемый результат: сервер возвращает ошибку валидации.


Тест shouldNotCreateOrderWithInvalidIngredient() проверяет обработку ошибки при использовании неверного ID ингредиента.
Создаёт заказ с одним некорректным ID ("invalid_ingredient_id") и одним корректным.
Отправляет запрос с авторизацией.
Проверяет статус 500 Internal Server Error (SC_INTERNAL_SERVER_ERROR).
Ожидаемый результат: сервер не может обработать запрос из‑за неверного ингредиента и возвращает 500.


Тест shouldGetOrdersWithAuth() проверяет получение списка заказов авторизованным пользователем.
Сначала создаёт заказ через createOrderWithAuth (чтобы были данные для получения).
Далее вызывает getOrdersWithAuth(accessToken) — отправляет GET‑запрос на получение заказов.
Проверяет:
- статус 200 OK;
- success: true.
Ожидаемый результат: сервер возвращает список заказов пользователя.


Тест shouldNotGetOrdersWithoutAuth() проверяет невозможность получения заказов без авторизации.
Вызывает getOrdersWithoutAuth() — отправляет GET‑запрос без токена.
Проверяет:
- статус 401 Unauthorized;
- success: false;
- сообщение "You should be authorised".
Ожидаемый результат: сервер отклоняет запрос и требует авторизации.

*/




package OrderTestPackage;

import Order.Order;
import Order.OrderActivities;
import User.GenerateUserData;
import User.User;
import User.UserActivities;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class OrderTests {
    private OrderActivities orderActivities;
    private String accessToken;
    private UserActivities userActivities;
    private User testUser;

    @Before
    public void setUp() {
        userActivities = new UserActivities();
        orderActivities = new OrderActivities();

        // Создать уникального пользователя
        testUser = GenerateUserData.createUniqueUser();
        ValidatableResponse createUserResponse = userActivities.createUser(testUser);

        // Проверить успешность создания пользователя перед извлечением токена
        int userCreationStatus = createUserResponse.extract().statusCode();
        boolean isUserCreated = createUserResponse.extract().path("success");
        assertEquals("Пользователь должен быть успешно создан", SC_OK, userCreationStatus);
        assertTrue("Пользователь должен быть создан", isUserCreated);

        // Извлечь токен доступа
        accessToken = createUserResponse.extract().path("accessToken");
    }

    @After
    public void tearDown() {
        // Удалить пользователя с использованием инициализированного userActivities
        if (accessToken != null) {
            userActivities.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание заказа авторизованным пользователем")
    public void shouldCreateOrderWithAuth() {
        Order order = new Order();
        List<String> ingredients = new ArrayList<>(Arrays.asList("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa6f"));
        order.setIngredients(ingredients);

        ValidatableResponse createOrderResponse = orderActivities.createOrderWithAuth(accessToken, order);
        int statusCode = createOrderResponse.extract().statusCode();
        boolean isOrderCreated = createOrderResponse.extract().path("success");

        assertEquals("Ожидается статус 200", SC_OK, statusCode);
        assertTrue("Ожидается успешное создание заказа", isOrderCreated);
    }

    @Test
    @DisplayName("Создание заказа неавторизованным пользователем")
    public void shouldNotCreateOrderWithoutAuth() {
        Order order = new Order();
        List<String> ingredients = new ArrayList<>(Arrays.asList("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa6f"));
        order.setIngredients(ingredients);

        ValidatableResponse createOrderResponse = orderActivities.createOrderWithoutAuth(order);

        // Логирование для диагностики
        System.out.println("Статус ответа: " + createOrderResponse.extract().statusCode());
        System.out.println("Тело ответа: " + createOrderResponse.extract().body().asString());

        int statusCode = createOrderResponse.extract().statusCode();
        boolean isOrderCreated = createOrderResponse.extract().path("success");

        assertEquals("Ожидается статус 401", SC_UNAUTHORIZED, statusCode);
        assertFalse("Ожидается неуспешное создание заказа", isOrderCreated);
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    public void shouldNotCreateOrderWithoutIngredients() {
        ValidatableResponse createOrderResponse = orderActivities.createOrderWithoutIngredients(accessToken);
        int statusCode = createOrderResponse.extract().statusCode();
        boolean isOrderCreated = createOrderResponse.extract().path("success");
        String errorMessage = createOrderResponse.extract().path("message");

        assertEquals("Ожидается статус 400", SC_BAD_REQUEST, statusCode);
        assertFalse("Ожидается неуспешное создание заказа", isOrderCreated);
        assertEquals("Ожидается сообщение об ошибке", "Ingredient ids must be provided", errorMessage);
    }

    @Test
    @DisplayName("Создание заказа с неверным ингредиентом")
    public void shouldNotCreateOrderWithInvalidIngredient() {
        Order order = new Order();
        List<String> ingredients = new ArrayList<>(Arrays.asList("invalid_ingredient_id", "61c0c5a71d1f82001bdaaa6f"));
        order.setIngredients(ingredients);
        ValidatableResponse createOrderResponse = orderActivities.createOrderWithAuth(accessToken, order);
        int statusCode = createOrderResponse.extract().statusCode();
        assertEquals("Ожидается статус 500", SC_INTERNAL_SERVER_ERROR, statusCode);
    }

    @Test
    @DisplayName("Получение заказов авторизованным пользователем")
    public void shouldGetOrdersWithAuth() {
        Order order = new Order();
        List<String> ingredients = new ArrayList<>(Arrays.asList("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa6f"));
        order.setIngredients(ingredients);

        orderActivities.createOrderWithAuth(accessToken, order);
        ValidatableResponse getOrdersResponse = orderActivities.getOrdersWithAuth(accessToken);
        int statusCode = getOrdersResponse.extract().statusCode();
        boolean areOrdersRetrieved = getOrdersResponse.extract().path("success");

        assertEquals("Ожидается статус 200", SC_OK, statusCode);
        assertTrue("Ожидается успешное получение заказов", areOrdersRetrieved);
    }

    @Test
    @DisplayName("Получение заказов неавторизованным пользователем")
    public void shouldNotGetOrdersWithoutAuth() {
        ValidatableResponse getOrdersResponse = orderActivities.getOrdersWithoutAuth();
        int statusCode = getOrdersResponse.extract().statusCode();
        boolean areOrdersRetrieved = getOrdersResponse.extract().path("success");
        String errorMessage = getOrdersResponse.extract().path("message");

        assertEquals("Ожидается статус 401", SC_UNAUTHORIZED, statusCode);
        assertFalse("Ожидается неуспешное получение заказов", areOrdersRetrieved);
        assertEquals("Ожидается сообщение об ошибке", "You should be authorised", errorMessage);
    }
}



/*

Главные принципы тестирования с помощью OrderTests:
1 Изоляция тестов: каждый тест выполняется в своём контексте — создаётся и удаляется отдельный пользователь.

2 Использование тестовых данных:
- уникальные email/пароль пользователя (генерируются GenerateUserData);
- фиксированные ID ингредиентов для заказа.

3 Проверка бизнес‑логики: тесты покрывают:
- авторизацию;
- валидацию данных;


4 Валидация ответов: для каждого запроса проверяется:
- HTTP‑статус;
- структура JSON‑ответа (success);
- содержание сообщений об ошибках.

Итог:
- Класс OrderTests выполняет комплексное тестирование API заказов, проверяя:
- корректную работу основных функций (создание, получение заказов);
- обработку ошибок (неверные данные, отсутствие авторизации);
- соблюдение правил безопасности (ограничение доступа без токена).

Тесты помогают выявить проблемы на ранних этапах — например, текущий баг с созданием заказа без авторизации (shouldNotCreateOrderWithoutAuth).

 */