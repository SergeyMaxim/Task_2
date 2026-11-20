package stellarburgers.test;

import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import stellarburgers.client.UserClient;
import stellarburgers.model.User;
import stellarburgers.util.UserGenerator;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(AllureJunit5.class)
@DisplayName("Создание пользователя (POST /api/auth/register)")
public class UserCreateTest {
    private UserClient userClient;
    private User user;
    private String accessToken;

    @BeforeEach
    public void setUp() {
        userClient = new UserClient();
        user = UserGenerator.getUniqueUser();
    }

    @AfterEach
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userClient.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Успешное создание уникального пользователя")
    public void createUniqueUserReturnsOk() {
        Response response = userClient.createUser(user);
        accessToken = response.path("accessToken");

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Ошибка при создании уже зарегистрированного пользователя")
    public void createExistingUserReturnsForbidden() {
        Response firstResponse = userClient.createUser(user);
        accessToken = firstResponse.path("accessToken");

        Response duplicateResponse = userClient.createUser(user);
        duplicateResponse.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @ParameterizedTest(name = "Создание пользователя без обязательного поля: {0}")
    @MethodSource("usersWithMissingFields")
    @DisplayName("Ошибка при создании пользователя без одного из обязательных полей")
    public void createUserWithoutRequiredFieldReturnsForbidden(String missingField, User invalidUser) {
        Response response = userClient.createUser(invalidUser);
        accessToken = response.path("accessToken");

        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> usersWithMissingFields() {
        User baseUser = UserGenerator.getUniqueUser();
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("email", new User(null, baseUser.getPassword(), baseUser.getName())),
                org.junit.jupiter.params.provider.Arguments.of("password", new User(baseUser.getEmail(), null, baseUser.getName())),
                org.junit.jupiter.params.provider.Arguments.of("name", new User(baseUser.getEmail(), baseUser.getPassword(), null))
        );
    }
}