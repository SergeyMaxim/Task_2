package stellarburgers.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import stellarburgers.model.OrderRequest;

import static io.restassured.RestAssured.given;

public class OrderClient extends BaseApiClient {
    private static final String INGREDIENTS_PATH = "/api/ingredients";
    private static final String ORDERS_PATH = "/api/orders";

    @Step("Получение списка доступных ингредиентов")
    public Response getIngredients() {
        return given()
                .spec(getBaseSpec())
                .when()
                .get(INGREDIENTS_PATH);
    }

    @Step("Создание заказа авторизованным пользователем")
    public Response createOrder(String accessToken, OrderRequest orderRequest) {
        return given()
                .spec(getBaseSpec())
                .header("Authorization", accessToken)
                .body(orderRequest)
                .when()
                .post(ORDERS_PATH);
    }

    @Step("Создание заказа без авторизации")
    public Response createOrderWithoutAuth(OrderRequest orderRequest) {
        return given()
                .spec(getBaseSpec())
                .body(orderRequest)
                .when()
                .post(ORDERS_PATH);
    }

    @Step("Получение заказов авторизованного пользователя")
    public Response getUserOrders(String accessToken) {
        return given()
                .spec(getBaseSpec())
                .header("Authorization", accessToken)
                .when()
                .get(ORDERS_PATH);
    }

    @Step("Получение заказов без авторизации")
    public Response getUserOrdersWithoutAuth() {
        return given()
                .spec(getBaseSpec())
                .when()
                .get(ORDERS_PATH);
    }
}