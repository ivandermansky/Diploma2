/*
Тест shouldNotCreateOrderWithInvalidIngredient() проверяет обработку ошибки при использовании неверного ID ингредиента.
Создаёт заказ с одним некорректным ID ("invalid_ingredient_id") и одним корректным.
Отправляет запрос с авторизацией.
Проверяет статус 500 Internal Server Error (SC_INTERNAL_SERVER_ERROR).
Ожидаемый результат: сервер не может обработать запрос из‑за неверного ингредиента и возвращает 500.
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

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.*;


public class OrderCreationWithInvalidIngredientTest {
    private OrderActivities orderActivities;
    private String accessToken;
    private UserActivities userActivities;
    private User testUser;

    @Before
    @Description("Подготовка тестового окружения:\n" +
            "- создаётся тестовый пользователь;\n" +
            "- проверяется успешность создания пользователя (статус 200);\n" +
            "- сохраняется accessToken для последующих запросов.")
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
    @DisplayName("Создание заказа с неверным ингредиентом")
    @Description("Тест проверяет, что сервер корректно обрабатывает ошибку при использовании некорректного ID ингредиента.\n\n" +
            "Шаги теста:\n" +
            "- создаётся объект Order с ингредиентами: один неверный ID ('invalid_ingredient_id'), один корректный;\n" +
            "- отправляется POST‑запрос на создание заказа с токеном авторизации;\n" +
            "- извлекается статус ответа сервера.\n\n" +
            "Проверки:\n" +
            "- статус ответа равен 500 (SC_INTERNAL_SERVER_ERROR).\n\n" +
            "Ожидаемый результат:\n" +
            "- сервер возвращает статус 500, так как не может обработать заказ с некорректным ингредиентом.")
    public void shouldNotCreateOrderWithInvalidIngredient() {
        Order order = new Order();
        List<String> ingredients = new ArrayList<>(Arrays.asList("invalid_ingredient_id", "61c0c5a71d1f82001bdaaa6f"));
        order.setIngredients(ingredients);
        ValidatableResponse createOrderResponse = orderActivities.createOrderWithAuth(accessToken, order);
        int statusCode = createOrderResponse.extract().statusCode();

        assertEquals("Ожидается статус 500", SC_INTERNAL_SERVER_ERROR, statusCode);
    }
}
