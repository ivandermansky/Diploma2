/*
Тест shouldGetOrdersWithAuth() проверяет получение списка заказов авторизованным пользователем.
Сначала создаёт заказ через createOrderWithAuth (чтобы были данные для получения).
Далее вызывает getOrdersWithAuth(accessToken) — отправляет GET‑запрос на получение заказов.

Проверяет:
- статус 200 OK;
- success: true.
Ожидаемый результат: сервер возвращает список заказов пользователя.
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
import static org.junit.Assert.*;


public class OrderGetWithAuthTest {
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
        assertTrue("Пользователь должен быть создан", isUserCreated);

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
    @DisplayName("Получение заказов авторизованным пользователем")
    @Description("Тест проверяет, что авторизованный пользователь может получить список своих заказов.\n\n" +
            "Шаги теста:\n" +
            "- создаётся тестовый заказ с фиксированным набором ингредиентов (через createOrderWithAuth);\n" +
            "- выполняется GET‑запрос на получение списка заказов пользователя (getOrdersWithAuth) с использованием accessToken;\n" +
            "- извлекаются статус ответа и значение поля success из тела ответа.\n\n" +
            "Проверки:\n" +
            "- статус ответа равен 200 (SC_OK);\n" +
            "- поле success в ответе равно true (операция выполнена успешно).\n\n" +
            "Ожидаемый результат:\n" +
            "- сервер возвращает статус 200 OK, подтверждая успешный запрос;\n" +
            "- в теле ответа содержится поле success: true;\n" +
            "- возвращается JSON‑массив с информацией о заказах пользователя (как минимум один заказ — созданный в тесте);\n" +
            "- данные заказа соответствуют отправленным при создании.")
    public void shouldGetOrdersWithAuth() {
        Order order = new Order();
        List<String> ingredients = new ArrayList<>(Arrays.asList("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa6f"));
        order.setIngredients(ingredients);

        orderActivities.createOrderWithAuth(accessToken, order);
        ValidatableResponse getOrdersResponse = orderActivities.getOrdersWithAuth(accessToken);
        int statusCode = getOrdersResponse.extract().statusCode();
        boolean areOrdersRetrieved = getOrdersResponse.extract().path("success");

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(statusCode)
                .as("Ожидается статус 200 OK")
                .isEqualTo(SC_OK);

        softly.assertThat(areOrdersRetrieved)
                .as("Ожидается успешное получение заказов (success: true)")
                .isTrue();

        softly.assertAll();
    }
}
