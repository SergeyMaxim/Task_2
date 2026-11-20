package stellarburgers.test;

import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stellarburgers.client.UserClient;
import stellarburgers.model.User;
import stellarburgers.model.UserCredentials;
import stellarburgers.util.UserGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(AllureJunit5.class)
@DisplayName("Логин пользователя (POST /api/auth/login)")
public class UserLoginTest {
    private UserClient userClient;
    private User user;
    private String accessToken;

    @BeforeEach
    public void setUp() {
        userClient = new UserClient();
        user = UserGenerator.getUniqueUser();
        Response createResponse = userClient.createUser(user);
        accessToken = createResponse.path("accessToken");
    }

    @AfterEach
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userClient.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Успешный логин под существующим пользователем")
    public void loginExistingUserReturnsOk() {
        Response response = userClient.loginUser(UserCredentials.from(user));

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Ошибка при логине с неверным email")
    public void loginWithInvalidEmailReturnsUnauthorized() {
        UserCredentials invalidCredentials = new UserCredentials("wrong_" + user.getEmail(), user.getPassword());
        Response response = userClient.loginUser(invalidCredentials);

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Ошибка при логине с неверным паролем")
    public void loginWithInvalidPasswordReturnsUnauthorized() {
        UserCredentials invalidCredentials = new UserCredentials(user.getEmail(), "wrong_" + user.getPassword());
        Response response = userClient.loginUser(invalidCredentials);

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}