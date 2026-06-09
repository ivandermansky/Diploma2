/*

Класс UserActivities предназначен для выполнения действий с пользователями через API.
Он инкапсулирует HTTP‑запросы к серверу и предоставляет удобные методы для тестирования пользовательских сценариев.

1 Класс позволяет выполнять следующие операции с пользователями:

2 Регистрация (createUser) — отправка POST‑запроса на /api/auth/register.

3 Авторизация (loginUser) — отправка POST‑запроса на /api/auth/login.

4 Удаление аккаунта (deleteUser) — отправка DELETE‑запроса на /api/auth/user (с токеном авторизации).

5 Обновление данных с авторизацией (updateUserWithAuth) — отправка PATCH‑запроса на /api/auth/user (с токеном).

6 Обновление данных без авторизации (updateUserWithoutAuth) — отправка PATCH‑запроса без токена (для проверки защиты API).


 */


package User;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.filter.Filter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

import static io.restassured.RestAssured.given;

public class UserActivities extends Base {

    private final Filter requestFilter = new RequestLoggingFilter();
    private final Filter responseFilter = new ResponseLoggingFilter();

    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String USER_PATH = "/api/auth/user";

    @Step("Создать пользователя")
    public ValidatableResponse createUser(User user) {
        return given()
                .filters(requestFilter, responseFilter)
                .spec(getSpec())
                .body(user)
                .when()
                .post(REGISTER_PATH)
                .then();
    }

    @Step("Вход для пользователя")
    public ValidatableResponse loginUser(Credentials credentials) {
        return given()
                .filters(requestFilter, responseFilter)
                .spec(getSpec())
                .body(credentials)
                .when()
                .post(LOGIN_PATH)
                .then();
    }

    @Step("Удалить пользователя")
    public ValidatableResponse deleteUser(String accessToken) {
        return given()
                .filters(requestFilter, responseFilter)
                .spec(getSpec())
                .header("Authorization", accessToken)
                .when()
                .delete(USER_PATH)
                .then();
    }

    @Step("Обновить данные пользователя с авторизацией")
    public ValidatableResponse updateUserWithAuth(String accessToken, Credentials credentials) {
        return given()
                .filters(requestFilter, responseFilter)
                .spec(getSpec())
                .header("Authorization", accessToken)
                .body(credentials)
                .when()
                .patch(USER_PATH)
                .then();
    }

    @Step("Обновить данные пользователя без авторизации")
    public ValidatableResponse updateUserWithoutAuth(Credentials credentials) {
        return given()
                .filters(requestFilter, responseFilter)
                .spec(getSpec())
                .body(credentials)
                .when()
                .patch(USER_PATH)
                .then();
    }
}