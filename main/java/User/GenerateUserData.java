/*

Класс GenerateUserData предназначен для автоматического создания тестовых данных пользователей с уникальными значениями. Разберу подробнее.

Класс упрощает подготовку тестовых данных для:

- регистрации новых пользователей;

- проверки валидации полей (обязательные/необязательные);

- тестирования сценариев с неполными данными;

- избежания конфликтов из‑за дублирования данных (например, одинаковых email).

 */


package User;

import java.util.Random;

public abstract class GenerateUserData {
    private static final Random random = new Random();

    private static int getRandomNumber() {
        return random.nextInt(100000);
    }

    public static User createUniqueUser() {
        return new User("uniqueUser" + getRandomNumber() + "@test.com", "uniquePass" + getRandomNumber(), "UniqueUser" + getRandomNumber());
    }

    public static User createUserWithEmailOnly() {
        return new User("emailOnly" + getRandomNumber() + "@test.com", null, "EmailOnlyUser" + getRandomNumber());
    }

    public static User createUserWithPasswordOnly() {
        return new User(null, "passwordOnly" + getRandomNumber(), "PasswordOnlyUser" + getRandomNumber());
    }
}