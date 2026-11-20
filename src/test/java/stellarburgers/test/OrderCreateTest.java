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

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(AllureJunit5.class)
@DisplayName("Создание заказа (POST /api/orders)")
public class OrderCreateTest {
    private UserClient userClient;
    private OrderClient orderClient;
    private String accessToken;
    private List<String> validIngredients;

    @BeforeEach
    public void setUp() {
        userClient = new UserClient();
        orderClient = new OrderClient();

        User user = UserGenerator.getUniqueUser();
        Response createResponse = userClient.createUser(user);
        accessToken = createResponse.path("accessToken");

        Response ingredientsResponse = orderClient.getIngredients();
        List<String> allIds = ingredientsResponse.path("data._id");
        validIngredients = List.of(allIds.get(0), allIds.get(1));
    }

    @AfterEach
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userClient.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и валидными ингредиентами")
    public void createOrderWithAuthAndIngredientsReturnsOk() {
        OrderRequest orderRequest = new OrderRequest(validIngredients);

        Response response = orderClient.createOrder(accessToken, orderRequest);
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации с валидными ингредиентами")
    public void createOrderWithoutAuthWithIngredientsReturnsOk() {
        OrderRequest orderRequest = new OrderRequest(validIngredients);

        Response response = orderClient.createOrderWithoutAuth(orderRequest);
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Ошибка при создании заказа с авторизацией без ингредиентов")
    public void createOrderWithAuthWithoutIngredientsReturnsBadRequest() {
        OrderRequest emptyOrderRequest = new OrderRequest(Collections.emptyList());

        Response response = orderClient.createOrder(accessToken, emptyOrderRequest);
        response.then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Ошибка при создании заказа без авторизации и без ингредиентов")
    public void createOrderWithoutAuthWithoutIngredientsReturnsBadRequest() {
        OrderRequest emptyOrderRequest = new OrderRequest(Collections.emptyList());

        Response response = orderClient.createOrderWithoutAuth(emptyOrderRequest);
        response.then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Ошибка 500 при создании заказа с неверным хешем ингредиентов")
    public void createOrderWithInvalidIngredientHashReturnsInternalServerError() {
        OrderRequest invalidOrderRequest = new OrderRequest(List.of("invalid_ingredient_hash_123"));

        Response response = orderClient.createOrder(accessToken, invalidOrderRequest);
        response.then()
                .statusCode(500);
    }
}