package com.example.productmanagement.service;

import com.example.productmanagement.entity.Product;
import com.example.productmanagement.repository.ProductRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ProductServiceTest {

    @Inject
    ProductService productService;

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
        testProduct.description = "A test product for service testing";
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

        Product createdProduct = productService.createProduct(newProduct).await().indefinitely();
        
        assertNotNull(createdProduct);
        assertNotNull(createdProduct.id);
        assertEquals("New Product", createdProduct.name);
        assertEquals("A new product", createdProduct.description);
        assertEquals(149.99, createdProduct.price);
        assertEquals(25, createdProduct.quantity);
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

        List<Product> products = productService.getAllProducts().await().indefinitely();
        
        assertEquals(2, products.size());
        assertTrue(products.stream().anyMatch(p -> p.name.equals("Test Product")));
        assertTrue(products.stream().anyMatch(p -> p.name.equals("Another Product")));
    }

    @Test
    void testGetProductById() {
        Long productId = testProduct.id;
        
        Product foundProduct = productService.getProductById(productId).await().indefinitely();
        
        assertNotNull(foundProduct);
        assertEquals(productId, foundProduct.id);
        assertEquals("Test Product", foundProduct.name);
        assertEquals("A test product for service testing", foundProduct.description);
        assertEquals(99.99, foundProduct.price);
        assertEquals(50, foundProduct.quantity);
    }

    @Test
    void testGetProductByIdNotFound() {
        Product foundProduct = productService.getProductById(99999L).await().indefinitely();
        
        assertNull(foundProduct);
    }

    @Test
    void testUpdateProduct() {
        Long productId = testProduct.id;
        
        Product updatedProduct = new Product();
        updatedProduct.name = "Updated Product";
        updatedProduct.description = "Updated description";
        updatedProduct.price = 199.99;
        updatedProduct.quantity = 75;

        Product result = productService.updateProduct(productId, updatedProduct).await().indefinitely();
        
        assertNotNull(result);
        assertEquals(productId, result.id);
        assertEquals("Updated Product", result.name);
        assertEquals("Updated description", result.description);
        assertEquals(199.99, result.price);
        assertEquals(75, result.quantity);
    }

    @Test
    void testUpdateProductNotFound() {
        Product updatedProduct = new Product();
        updatedProduct.name = "Updated Product";
        updatedProduct.description = "Updated description";
        updatedProduct.price = 199.99;
        updatedProduct.quantity = 75;

        Product result = productService.updateProduct(99999L, updatedProduct).await().indefinitely();
        
        assertNull(result);
    }

    @Test
    void testDeleteProduct() {
        Long productId = testProduct.id;
        
        Boolean deleted = productService.deleteProduct(productId).await().indefinitely();
        
        assertTrue(deleted);
        
        // Verify product is deleted
        Product foundProduct = productService.getProductById(productId).await().indefinitely();
        assertNull(foundProduct);
    }

    @Test
    void testDeleteProductNotFound() {
        Boolean deleted = productService.deleteProduct(99999L).await().indefinitely();
        
        assertFalse(deleted);
    }

    @Test
    void testCheckStockAvailability() {
        Long productId = testProduct.id;
        
        // Test with available stock
        Boolean available = productService.checkStockAvailability(productId, 30).await().indefinitely();
        assertTrue(available);
        
        // Test with insufficient stock
        Boolean notAvailable = productService.checkStockAvailability(productId, 100).await().indefinitely();
        assertFalse(notAvailable);
    }

    @Test
    void testCheckStockAvailabilityProductNotFound() {
        Boolean available = productService.checkStockAvailability(99999L, 10).await().indefinitely();
        assertFalse(available);
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

        List<Product> products = productService.getProductsOrderedByPrice().await().indefinitely();
        
        assertEquals(3, products.size());
        assertEquals(29.99, products.get(0).price);
        assertEquals(99.99, products.get(1).price);
        assertEquals(299.99, products.get(2).price);
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

        List<Product> gamingProducts = productService.searchProductsByName("Gaming").await().indefinitely();
        
        assertEquals(2, gamingProducts.size());
        assertTrue(gamingProducts.stream().anyMatch(p -> p.name.equals("Gaming Laptop")));
        assertTrue(gamingProducts.stream().anyMatch(p -> p.name.equals("Gaming Desktop")));
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

        List<Product> lowStockProducts = productService.getLowStockProducts(10).await().indefinitely();
        
        assertEquals(1, lowStockProducts.size());
        assertEquals("Low Stock Product", lowStockProducts.get(0).name);
    }

    @Test
    void testUpdateProductStock() {
        Long productId = testProduct.id;
        
        Product updatedProduct = productService.updateProductStock(productId, 100).await().indefinitely();
        
        assertNotNull(updatedProduct);
        assertEquals(productId, updatedProduct.id);
        assertEquals(100, updatedProduct.quantity);
    }

    @Test
    void testUpdateProductStockNotFound() {
        Product updatedProduct = productService.updateProductStock(99999L, 100).await().indefinitely();
        
        assertNull(updatedProduct);
    }
}
