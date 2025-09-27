package com.example.productmanagement.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Product entity representing a product in the inventory management system.
 * Extends PanacheEntity to get reactive CRUD operations out of the box.
 */
@Entity
@Table(name = "products")
public class Product extends PanacheEntity {

    @NotBlank(message = "Product name cannot be blank")
    @Column(name = "name", nullable = false)
    public String name;

    @NotBlank(message = "Product description cannot be blank")
    @Column(name = "description", nullable = false, length = 1000)
    public String description;

    @NotNull(message = "Product price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Product price must be greater than 0")
    @Column(name = "price", nullable = false)
    public Double price;

    @NotNull(message = "Product quantity cannot be null")
    @Min(value = 0, message = "Product quantity cannot be negative")
    @Column(name = "quantity", nullable = false)
    public Integer quantity;

    // Default constructor required by JPA
    public Product() {
    }

    // Constructor for creating new products
    public Product(String name, String description, Double price, Integer quantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
