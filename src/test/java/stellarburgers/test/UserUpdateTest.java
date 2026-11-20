package stellarburgers.test;

import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stellarburgers.client.UserClient;
import stellarburgers.model.User;
import stellarburgers.model.UserCredentials;
import stellarburgers.util.UserGenerator;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;

@ExtendWith(AllureJunit5.class)
@DisplayName("Изменение данных пользователя (PATCH /api/auth/user)")
public class UserUpdateTest {
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
    @DisplayName("Изменение email авторизованного пользователя")
    public void updateEmailWithAuthReturnsOk() {
        String newEmail = "updated_" + user.getEmail();
        User updatedData = new User(newEmail, null, null);

        Response response = userClient.updateUser(accessToken, updatedData);
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail))
                .body("user.name", equalTo(user.getName()));
    }

    @Test
    @DisplayName("Изменение имени авторизованного пользователя")
    public void updateNameWithAuthReturnsOk() {
        String newName = "Updated_" + user.getName();
        User updatedData = new User(null, null, newName);

        Response response = userClient.updateUser(accessToken, updatedData);
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(newName));
    }

    @Test
    @DisplayName("Изменение пароля авторизованного пользователя")
    public void updatePasswordWithAuthReturnsOk() {
        String newPassword = "updated_" + user.getPassword();
        User updatedData = new User(null, newPassword, null);

        Response response = userClient.updateUser(accessToken, updatedData);
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()));

        userClient.loginUser(new UserCredentials(user.getEmail(), newPassword))
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @ParameterizedTest(name = "Изменение поля {0} без авторизации")
    @MethodSource("userFieldsToUpdate")
    @DisplayName("Ошибка при изменении любого поля без авторизации")
    public void updateFieldWithoutAuthReturnsUnauthorized(String fieldName, User updatedData) {
        Response response = userClient.updateUserWithoutAuth(updatedData);
        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    private static Stream<Arguments> userFieldsToUpdate() {
        User randomData = UserGenerator.getUniqueUser();
        return Stream.of(
                Arguments.of("email", new User(randomData.getEmail(), null, null)),
                Arguments.of("password", new User(null, randomData.getPassword(), null)),
                Arguments.of("name", new User(null, null, randomData.getName()))
        );
    }
}