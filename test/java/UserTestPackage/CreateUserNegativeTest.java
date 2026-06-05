/*

Тест shouldNotCreateDuplicateUser() проверяет негативный сценарий создания пользователя.
Цель теста: убеждиться, что система не позволяет зарегистрировать дублирующий аккаунт с теми же данными.

 */


package UserTestPackage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import User.GenerateUserData;
import User.User;
import User.UserActivities;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

@Epic("Тестирование пользователей")
@Feature("Создание пользователя")
public class CreateUserNegativeTest {

    private UserActivities userActivities;
    private User testUser;
    private String accessToken;

    @Before
    public void setUp() {
        userActivities = new UserActivities();
        testUser = GenerateUserData.createUniqueUser();
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userActivities.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Пользователь не может быть создан дважды")
    @Description("Проверка невозможности создания дублирующего аккаунта пользователя")
    public void shouldNotCreateDuplicateUser() {
        createUserFirstTime();
        tryToCreateDuplicateUser();
    }

    @Step("Создать пользователя первый раз")
    private void createUserFirstTime() {
        ValidatableResponse firstCreateResponse = userActivities.createUser(testUser);
        int firstStatusCode = firstCreateResponse.extract().statusCode();
        boolean isFirstUserCreated = firstCreateResponse.extract().path("success");

        assertEquals("Первый пользователь должен быть успешно создан", SC_OK, firstStatusCode);
        assertTrue("Первый пользователь должен быть создан", isFirstUserCreated);

        accessToken = firstCreateResponse.extract().path("accessToken");
    }

    @Step("Попытаться создать того же пользователя повторно")
    private void tryToCreateDuplicateUser() {
        ValidatableResponse secondCreateResponse = userActivities.createUser(testUser);
        int secondStatusCode = secondCreateResponse.extract().statusCode();
        boolean isSecondUserCreated = secondCreateResponse.extract().path("success");
        String actualMessage = secondCreateResponse.extract().path("message");

        assertEquals("Ожидается статус 403 при попытке создать дубликат", SC_FORBIDDEN, secondStatusCode);
        assertFalse("Повторное создание пользователя должно быть отклонено", isSecondUserCreated);
        assertEquals("Ожидается сообщение об ошибке", "User already exists", actualMessage);
    }
}
