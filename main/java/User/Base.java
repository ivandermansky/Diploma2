/*
Класс Base — базовый класс для настройки HTTP‑запросов в тестах с использованием библиотеки REST Assured.
Он служит для:
- централизованного хранения базового URL API;
- единообразной настройки заголовков и параметров запросов;
- передачи данных о результате создания пользователя;
- уменьшения дублирования кода в тестовых классах.
*/

package User;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.response.ValidatableResponse;

public class Base {
    private static final String BASE_URL = "https://stellarburgers.education-services.ru/";

    protected RequestSpecification getSpec() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(BASE_URL)
                .build();
    }

    // Вспомогательный класс для передачи данных о результате создания пользователя
    public static class CreationResult {
        private final int statusCode;
        private final boolean isUserCreated;
        private final String accessToken;
        private final String message; // Дополнительно: сообщение об ошибке

        public CreationResult(int statusCode, boolean isUserCreated, String accessToken, String message) {
            this.statusCode = statusCode;
            this.isUserCreated = isUserCreated;
            this.accessToken = accessToken;
            this.message = message;
        }

        // Геттеры
        public int getStatusCode() { return statusCode; }
        public boolean isUserCreated() { return isUserCreated; }
        public String getAccessToken() { return accessToken; }
        public String getMessage() { return message; }
    }


     //Создать CreationResult из ответа API
    public static CreationResult createFromResponse(ValidatableResponse response) {
        int statusCode = response.extract().statusCode();
        boolean isUserCreated = response.extract().path("success");
        String accessToken = response.extract().path("accessToken");
        String message = response.extract().path("message");

        return new CreationResult(statusCode, isUserCreated, accessToken, message);
    }
}
