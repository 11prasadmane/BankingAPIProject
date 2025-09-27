package com.example.productmanagement.repository;

import com.example.productmanagement.entity.Product;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repository interface for Product entity providing reactive database operations.
 * Extends PanacheRepository to get reactive CRUD operations automatically.
 */
@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    /**
     * Find all products ordered by price in ascending order.
     * 
     * @return Uni<List<Product>> - List of products sorted by price
     */
    public Uni<List<Product>> findAllOrderedByPrice() {
        return findAll().order("price").list();
    }

    /**
     * Find products by name (case-insensitive search).
     * 
     * @param name - Product name to search for
     * @return Uni<List<Product>> - List of products matching the name
     */
    public Uni<List<Product>> findByNameContainingIgnoreCase(String name) {
        return find("LOWER(name) LIKE LOWER(?1)", "%" + name + "%").list();
    }

    /**
     * Find products with quantity greater than or equal to specified amount.
     * 
     * @param minQuantity - Minimum quantity threshold
     * @return Uni<List<Product>> - List of products with sufficient stock
     */
    public Uni<List<Product>> findByQuantityGreaterThanEqual(Integer minQuantity) {
        return find("quantity >= ?1", minQuantity).list();
    }

    /**
     * Find products with price between min and max values.
     * 
     * @param minPrice - Minimum price
     * @param maxPrice - Maximum price
     * @return Uni<List<Product>> - List of products within price range
     */
    public Uni<List<Product>> findByPriceBetween(Double minPrice, Double maxPrice) {
        return find("price BETWEEN ?1 AND ?2", minPrice, maxPrice).list();
    }

    /**
     * Count products with quantity less than specified threshold (low stock).
     * 
     * @param threshold - Quantity threshold
     * @return Uni<Long> - Count of products with low stock
     */
    public Uni<Long> countLowStockProducts(Integer threshold) {
        return count("quantity < ?1", threshold);
    }

    /**
     * Find products by exact name match.
     * 
     * @param name - Exact product name
     * @return Uni<Product> - Product with exact name match
     */
    public Uni<Product> findByName(String name) {
        return find("name", name).firstResult();
    }
}
