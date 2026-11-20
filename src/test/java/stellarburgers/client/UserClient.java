package stellarburgers.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import stellarburgers.model.User;
import stellarburgers.model.UserCredentials;

import static io.restassured.RestAssured.given;

public class UserClient extends BaseApiClient {
    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String USER_PATH = "/api/auth/user";

    @Step("Создание нового пользователя")
    public Response createUser(User user) {
        return given()
                .spec(getBaseSpec())
                .body(user)
                .when()
                .post(REGISTER_PATH);
    }

    @Step("Авторизация пользователя")
    public Response loginUser(UserCredentials credentials) {
        return given()
                .spec(getBaseSpec())
                .body(credentials)
                .when()
                .post(LOGIN_PATH);
    }

    @Step("Обновление данных авторизованного пользователя")
    public Response updateUser(String accessToken, User user) {
        return given()
                .spec(getBaseSpec())
                .header("Authorization", accessToken)
                .body(user)
                .when()
                .patch(USER_PATH);
    }

    @Step("Обновление данных пользователя без авторизации")
    public Response updateUserWithoutAuth(User user) {
        return given()
                .spec(getBaseSpec())
                .body(user)
                .when()
                .patch(USER_PATH);
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String accessToken) {
        return given()
                .spec(getBaseSpec())
                .header("Authorization", accessToken)
                .when()
                .delete(USER_PATH);
    }
}