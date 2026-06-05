/*

Класс Base — базовый класс для настройки HTTP‑запросов в тестах с использованием библиотеки REST Assured.

Он служит для:

- централизованного хранения базового URL API;

- единообразной настройки заголовков и параметров запросов;

- уменьшения дублирования кода в тестовых классах.

 */

package User;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class Base {
    private static final String BASE_URL = "https://stellarburgers.education-services.ru/";

    protected RequestSpecification getSpec() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(BASE_URL)
                .build();
    }
}