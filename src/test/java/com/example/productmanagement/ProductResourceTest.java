package com.example.productmanagement;

import com.example.productmanagement.entity.Product;
import com.example.productmanagement.repository.ProductRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ProductResourceTest {

    @Inject
    ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        productRepository.deleteAll().await().indefinitely();
        
        // Create a test product
        testProduct = new Product();
        testProduct.name = "Test Product";
        testProduct.description = "A test product for unit testing";
        testProduct.price = 99.99;
        testProduct.quantity = 50;
        
        productRepository.persist(testProduct).await().indefinitely();
    }

    @Test
    void testCreateProduct() {
        Product newProduct = new Product();
        newProduct.name = "New Product";
        newProduct.description = "A new product";
        newProduct.price = 149.99;
        newProduct.quantity = 25;

        Response response = given()
                .contentType(ContentType.JSON)
                .body(newProduct)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", equalTo("New Product"))
                .body("description", equalTo("A new product"))
                .body("price", equalTo(149.99f))
                .body("quantity", equalTo(25))
                .body("id", notNullValue())
                .extract().response();

        assertNotNull(response.jsonPath().getLong("id"));
    }

    @Test
    void testCreateProductWithInvalidData() {
        Product invalidProduct = new Product();
        invalidProduct.name = ""; // Invalid: empty name
        invalidProduct.description = "Invalid product";
        invalidProduct.price = -10.0; // Invalid: negative price
        invalidProduct.quantity = -5; // Invalid: negative quantity

        given()
                .contentType(ContentType.JSON)
                .body(invalidProduct)
                .when()
                .post("/api/products")
                .then()
                .statusCode(400);
    }

    @Test
    void testGetAllProducts() {
        // Create another product
        Product anotherProduct = new Product();
        anotherProduct.name = "Another Product";
        anotherProduct.description = "Another test product";
        anotherProduct.price = 199.99;
        anotherProduct.quantity = 30;
        productRepository.persist(anotherProduct).await().indefinitely();

        given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(2))
                .body("name", hasItems("Test Product", "Another Product"));
    }

    @Test
    void testGetProductById() {
        Long productId = testProduct.id;

        given()
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(productId.intValue()))
                .body("name", equalTo("Test Product"))
                .body("description", equalTo("A test product for unit testing"))
                .body("price", equalTo(99.99f))
                .body("quantity", equalTo(50));
    }

    @Test
    void testGetProductByIdNotFound() {
        given()
                .when()
                .get("/api/products/99999")
                .then()
                .statusCode(404)
                .body("error", equalTo("Product not found"));
    }

    @Test
    void testUpdateProduct() {
        Long productId = testProduct.id;
        
        Product updatedProduct = new Product();
        updatedProduct.name = "Updated Product";
        updatedProduct.description = "Updated description";
        updatedProduct.price = 199.99;
        updatedProduct.quantity = 75;

        given()
                .contentType(ContentType.JSON)
                .body(updatedProduct)
                .when()
                .put("/api/products/" + productId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(productId.intValue()))
                .body("name", equalTo("Updated Product"))
                .body("description", equalTo("Updated description"))
                .body("price", equalTo(199.99f))
                .body("quantity", equalTo(75));
    }

    @Test
    void testUpdateProductNotFound() {
        Product updatedProduct = new Product();
        updatedProduct.name = "Updated Product";
        updatedProduct.description = "Updated description";
        updatedProduct.price = 199.99;
        updatedProduct.quantity = 75;

        given()
                .contentType(ContentType.JSON)
                .body(updatedProduct)
                .when()
                .put("/api/products/99999")
                .then()
                .statusCode(404)
                .body("error", equalTo("Product not found"));
    }

    @Test
    void testDeleteProduct() {
        Long productId = testProduct.id;

        given()
                .when()
                .delete("/api/products/" + productId)
                .then()
                .statusCode(204);

        // Verify product is deleted
        given()
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(404);
    }

    @Test
    void testDeleteProductNotFound() {
        given()
                .when()
                .delete("/api/products/99999")
                .then()
                .statusCode(404)
                .body("error", equalTo("Product not found"));
    }

    @Test
    void testCheckStockAvailability() {
        Long productId = testProduct.id;

        // Test with available stock
        given()
                .when()
                .get("/api/products/" + productId + "/stock?count=30")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("productId", equalTo(productId.intValue()))
                .body("requestedCount", equalTo(30))
                .body("available", equalTo(true));

        // Test with insufficient stock
        given()
                .when()
                .get("/api/products/" + productId + "/stock?count=100")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("productId", equalTo(productId.intValue()))
                .body("requestedCount", equalTo(100))
                .body("available", equalTo(false));
    }

    @Test
    void testCheckStockAvailabilityDefaultCount() {
        Long productId = testProduct.id;

        given()
                .when()
                .get("/api/products/" + productId + "/stock")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("productId", equalTo(productId.intValue()))
                .body("requestedCount", equalTo(1))
                .body("available", equalTo(true));
    }

    @Test
    void testCheckStockAvailabilityProductNotFound() {
        given()
                .when()
                .get("/api/products/99999/stock?count=10")
                .then()
                .statusCode(404)
                .body("error", equalTo("Product not found"));
    }

    @Test
    void testGetProductsOrderedByPrice() {
        // Create products with different prices
        Product cheapProduct = new Product();
        cheapProduct.name = "Cheap Product";
        cheapProduct.description = "A cheap product";
        cheapProduct.price = 29.99;
        cheapProduct.quantity = 100;
        productRepository.persist(cheapProduct).await().indefinitely();

        Product expensiveProduct = new Product();
        expensiveProduct.name = "Expensive Product";
        expensiveProduct.description = "An expensive product";
        expensiveProduct.price = 299.99;
        expensiveProduct.quantity = 10;
        productRepository.persist(expensiveProduct).await().indefinitely();

        given()
                .when()
                .get("/api/products/sorted-by-price")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(3))
                .body("price[0]", equalTo(29.99f))
                .body("price[1]", equalTo(99.99f))
                .body("price[2]", equalTo(299.99f));
    }

    @Test
    void testSearchProductsByName() {
        // Create products with similar names
        Product laptopProduct = new Product();
        laptopProduct.name = "Gaming Laptop";
        laptopProduct.description = "High-performance gaming laptop";
        laptopProduct.price = 1299.99;
        laptopProduct.quantity = 5;
        productRepository.persist(laptopProduct).await().indefinitely();

        Product desktopProduct = new Product();
        desktopProduct.name = "Gaming Desktop";
        desktopProduct.description = "High-performance gaming desktop";
        desktopProduct.price = 1999.99;
        desktopProduct.quantity = 3;
        productRepository.persist(desktopProduct).await().indefinitely();

        // Search for "Gaming"
        given()
                .when()
                .get("/api/products/search?name=Gaming")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(2))
                .body("name", hasItems("Gaming Laptop", "Gaming Desktop"));

        // Search for "Laptop" (case-insensitive)
        given()
                .when()
                .get("/api/products/search?name=laptop")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(1))
                .body("name[0]", equalTo("Gaming Laptop"));
    }

    @Test
    void testGetLowStockProducts() {
        // Create products with different stock levels
        Product lowStockProduct = new Product();
        lowStockProduct.name = "Low Stock Product";
        lowStockProduct.description = "Product with low stock";
        lowStockProduct.price = 49.99;
        lowStockProduct.quantity = 5; // Below threshold of 10
        productRepository.persist(lowStockProduct).await().indefinitely();

        Product highStockProduct = new Product();
        highStockProduct.name = "High Stock Product";
        highStockProduct.description = "Product with high stock";
        highStockProduct.price = 79.99;
        highStockProduct.quantity = 50; // Above threshold of 10
        productRepository.persist(highStockProduct).await().indefinitely();

        given()
                .when()
                .get("/api/products/low-stock?threshold=10")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(1))
                .body("name[0]", equalTo("Low Stock Product"));
    }

    @Test
    void testGetLowStockProductsDefaultThreshold() {
        // Test with default threshold of 10
        given()
                .when()
                .get("/api/products/low-stock")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(0)); // testProduct has quantity 50, which is above threshold
    }
}
