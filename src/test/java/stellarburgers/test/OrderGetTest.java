package stellarburgers.test;

import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stellarburgers.client.OrderClient;
import stellarburgers.client.UserClient;
import stellarburgers.model.OrderRequest;
import stellarburgers.model.User;
import stellarburgers.util.UserGenerator;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(AllureJunit5.class)
@DisplayName("Получение заказов конкретного пользователя (GET /api/orders)")
public class OrderGetTest {
    private UserClient userClient;
    private OrderClient orderClient;
    private String accessToken;

    @BeforeEach
    public void setUp() {
        userClient = new UserClient();
        orderClient = new OrderClient();

        User user = UserGenerator.getUniqueUser();
        Response createResponse = userClient.createUser(user);
        accessToken = createResponse.path("accessToken");

        Response ingredientsResponse = orderClient.getIngredients();
        List<String> allIds = ingredientsResponse.path("data._id");
        OrderRequest orderRequest = new OrderRequest(List.of(allIds.get(0), allIds.get(1)));
        orderClient.createOrder(accessToken, orderRequest);
    }

    @AfterEach
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userClient.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Получение списка заказов авторизованным пользователем")
    public void getUserOrdersWithAuthReturnsOk() {
        Response response = orderClient.getUserOrders(accessToken);

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", hasSize(1))
                .body("total", notNullValue())
                .body("totalToday", notNullValue());
    }

    @Test
    @DisplayName("Ошибка при получении заказов без авторизации")
    public void getUserOrdersWithoutAuthReturnsUnauthorized() {
        Response response = orderClient.getUserOrdersWithoutAuth();

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}