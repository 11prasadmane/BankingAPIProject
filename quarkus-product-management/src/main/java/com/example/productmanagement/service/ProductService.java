package com.example.productmanagement.service;

import com.example.productmanagement.entity.Product;
import com.example.productmanagement.repository.ProductRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Service class for Product business logic and operations.
 * Handles all business rules and coordinates with the repository layer.
 */
@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;

    /**
     * Create a new product.
     * 
     * @param product - Product to create
     * @return Uni<Product> - Created product with generated ID
     */
    @WithTransaction
    public Uni<Product> createProduct(@Valid Product product) {
        return productRepository.persist(product);
    }

    /**
     * Get all products.
     * 
     * @return Uni<List<Product>> - List of all products
     */
    public Uni<List<Product>> getAllProducts() {
        return productRepository.listAll();
    }

    /**
     * Get a product by ID.
     * 
     * @param id - Product ID
     * @return Uni<Product> - Product if found, null otherwise
     */
    public Uni<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Update an existing product.
     * 
     * @param id - Product ID to update
     * @param product - Updated product data
     * @return Uni<Product> - Updated product
     */
    @WithTransaction
    public Uni<Product> updateProduct(Long id, @Valid Product product) {
        return productRepository.findById(id)
                .onItem().ifNotNull().transformToUni(existingProduct -> {
                    existingProduct.name = product.name;
                    existingProduct.description = product.description;
                    existingProduct.price = product.price;
                    existingProduct.quantity = product.quantity;
                    return productRepository.persist(existingProduct);
                });
    }

    /**
     * Delete a product by ID.
     * 
     * @param id - Product ID to delete
     * @return Uni<Boolean> - True if deleted, false if not found
     */
    @WithTransaction
    public Uni<Boolean> deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }

    /**
     * Check stock availability for a specific product.
     * 
     * @param id - Product ID
     * @param requestedQuantity - Quantity to check availability for
     * @return Uni<Boolean> - True if sufficient stock available, false otherwise
     */
    public Uni<Boolean> checkStockAvailability(Long id, Integer requestedQuantity) {
        return productRepository.findById(id)
                .onItem().ifNotNull().transform(product -> 
                    product.quantity >= requestedQuantity
                )
                .onItem().ifNull().continueWith(false);
    }

    /**
     * Get all products ordered by price in ascending order.
     * 
     * @return Uni<List<Product>> - List of products sorted by price
     */
    public Uni<List<Product>> getProductsOrderedByPrice() {
        return productRepository.findAllOrderedByPrice();
    }

    /**
     * Search products by name (case-insensitive).
     * 
     * @param name - Name to search for
     * @return Uni<List<Product>> - List of matching products
     */
    public Uni<List<Product>> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Get products with low stock (quantity below threshold).
     * 
     * @param threshold - Stock threshold
     * @return Uni<List<Product>> - List of products with low stock
     */
    public Uni<List<Product>> getLowStockProducts(Integer threshold) {
        return productRepository.findByQuantityGreaterThanEqual(threshold)
                .onItem().transform(products -> 
                    products.stream()
                            .filter(product -> product.quantity < threshold)
                            .toList()
                );
    }

    /**
     * Get products within a price range.
     * 
     * @param minPrice - Minimum price
     * @param maxPrice - Maximum price
     * @return Uni<List<Product>> - List of products within price range
     */
    public Uni<List<Product>> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /**
     * Update product stock quantity.
     * 
     * @param id - Product ID
     * @param newQuantity - New quantity
     * @return Uni<Product> - Updated product
     */
    @WithTransaction
    public Uni<Product> updateProductStock(Long id, Integer newQuantity) {
        return productRepository.findById(id)
                .onItem().ifNotNull().transformToUni(product -> {
                    product.quantity = newQuantity;
                    return productRepository.persist(product);
                });
    }
}
