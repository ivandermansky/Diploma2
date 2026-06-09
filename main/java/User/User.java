/*

Класс User представляет полную модель пользователя в системе:

- используется при создании пользователя (createUser);

- содержит все данные пользователя, которые могут храниться в БД;

- отражает бизнес‑сущность "Пользователь";

 */


package User;

public class User {
    private String email;
    private String password;
    private String name;

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}