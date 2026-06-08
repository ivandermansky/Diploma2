/*
Тест shouldNotGetOrdersWithoutAuth() проверяет невозможность получения заказов без авторизации.
Вызывает getOrdersWithoutAuth() — отправляет GET‑запрос без токена.

Проверяет:
- статус 401 Unauthorized;
- success: false;
- сообщение "You should be authorised".

Ожидаемый результат: сервер отклоняет запрос и требует авторизации.
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

import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.*;


public class OrderGetWithoutAuthTest {
    private OrderActivities orderActivities;
    private String accessToken;
    private UserActivities userActivities;
    private User testUser;

    @Before
    @Description("Подготовка тестового окружения:\n" +
            "- создаётся тестовый пользователь;\n" +
            "- проверяется успешность создания пользователя (статус 200 и success: true);\n" +
            "- сохраняется accessToken для возможного использования в tearDown.")
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
    @DisplayName("Получение заказов неавторизованным пользователем")
    @Description("Тест проверяет, что неавторизованный пользователь не может получить список заказов.\n\n" +
            "Шаги теста:\n" +
            "- выполняется GET‑запрос на получение списка заказов (getOrdersWithoutAuth) без передачи accessToken;\n" +
            "- извлекаются статус ответа, значение поля success и сообщение об ошибке из тела ответа.\n\n" +
            "Проверки:\n" +
            "- статус ответа равен 401 (SC_UNAUTHORIZED);\n" +
            "- поле success в ответе равно false (операция не выполнена);\n" +
            "- сообщение об ошибке в ответе равно 'You should be authorised'.\n\n" +
            "Ожидаемый результат:\n" +
            "- сервер возвращает статус 401 Unauthorized, подтверждая отсутствие авторизации;\n" +
            "- в теле ответа содержится поле success: false;\n" +
            "- возвращается сообщение 'You should be authorised', указывающее на необходимость авторизации.")
    public void shouldNotGetOrdersWithoutAuth() {
        ValidatableResponse getOrdersResponse = orderActivities.getOrdersWithoutAuth();
        int statusCode = getOrdersResponse.extract().statusCode();
        boolean areOrdersRetrieved = getOrdersResponse.extract().path("success");
        String errorMessage = getOrdersResponse.extract().path("message");

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(statusCode)
                .as("Ожидается статус 401 Unauthorized")
                .isEqualTo(SC_UNAUTHORIZED);

        softly.assertThat(areOrdersRetrieved)
                .as("Ожидается неуспешное получение заказов (success: false)")
                .isFalse();

        softly.assertThat(errorMessage)
                .as("Ожидается сообщение об ошибке 'You should be authorised'")
                .isEqualTo("You should be authorised");

        softly.assertAll();
    }
}
