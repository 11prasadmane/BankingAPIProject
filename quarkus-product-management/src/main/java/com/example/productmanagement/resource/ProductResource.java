package com.example.productmanagement.resource;

import com.example.productmanagement.entity.Product;
import com.example.productmanagement.service.ProductService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * REST API resource for Product management operations.
 * Provides CRUD endpoints and additional business operations.
 */
@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Product Management", description = "Operations for managing products")
public class ProductResource {

    @Inject
    ProductService productService;

    /**
     * Create a new product.
     */
    @POST
    @Operation(summary = "Create a new product", description = "Creates a new product in the system")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @APIResponse(responseCode = "400", description = "Invalid product data")
    })
    public Uni<Response> createProduct(@Valid Product product) {
        return productService.createProduct(product)
                .map(createdProduct -> Response.status(Response.Status.CREATED)
                        .entity(createdProduct)
                        .build());
    }

    /**
     * Get all products.
     */
    @GET
    @Operation(summary = "Get all products", description = "Retrieves all products from the system")
    @APIResponse(responseCode = "200", description = "List of all products",
            content = @Content(schema = @Schema(implementation = Product.class)))
    public Uni<Response> getAllProducts() {
        return productService.getAllProducts()
                .map(products -> Response.ok(products).build());
    }

    /**
     * Get a specific product by ID.
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @APIResponse(responseCode = "404", description = "Product not found")
    })
    public Uni<Response> getProductById(@PathParam("id") Long id) {
        return productService.getProductById(id)
                .map(product -> {
                    if (product != null) {
                        return Response.ok(product).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("{\"error\": \"Product not found\"}")
                                .build();
                    }
                });
    }

    /**
     * Update an existing product.
     */
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @APIResponse(responseCode = "404", description = "Product not found"),
            @APIResponse(responseCode = "400", description = "Invalid product data")
    })
    public Uni<Response> updateProduct(@PathParam("id") Long id, @Valid Product product) {
        return productService.updateProduct(id, product)
                .map(updatedProduct -> {
                    if (updatedProduct != null) {
                        return Response.ok(updatedProduct).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("{\"error\": \"Product not found\"}")
                                .build();
                    }
                });
    }

    /**
     * Delete a product by ID.
     */
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete product", description = "Deletes a product from the system")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Product deleted successfully"),
            @APIResponse(responseCode = "404", description = "Product not found")
    })
    public Uni<Response> deleteProduct(@PathParam("id") Long id) {
        return productService.deleteProduct(id)
                .map(deleted -> {
                    if (deleted) {
                        return Response.noContent().build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("{\"error\": \"Product not found\"}")
                                .build();
                    }
                });
    }

    /**
     * Check stock availability for a specific product.
     */
    @GET
    @Path("/{id}/stock")
    @Operation(summary = "Check stock availability", description = "Checks if sufficient stock is available for a product")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Stock availability checked",
                    content = @Content(schema = @Schema(implementation = StockAvailabilityResponse.class))),
            @APIResponse(responseCode = "404", description = "Product not found")
    })
    public Uni<Response> checkStockAvailability(@PathParam("id") Long id, 
                                               @QueryParam("count") @DefaultValue("1") Integer count) {
        return productService.checkStockAvailability(id, count)
                .map(available -> {
                    if (available != null) {
                        StockAvailabilityResponse response = new StockAvailabilityResponse(id, count, available);
                        return Response.ok(response).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("{\"error\": \"Product not found\"}")
                                .build();
                    }
                });
    }

    /**
     * Get all products ordered by price in ascending order.
     */
    @GET
    @Path("/sorted-by-price")
    @Operation(summary = "Get products sorted by price", description = "Retrieves all products ordered by price in ascending order")
    @APIResponse(responseCode = "200", description = "List of products sorted by price",
            content = @Content(schema = @Schema(implementation = Product.class)))
    public Uni<Response> getProductsOrderedByPrice() {
        return productService.getProductsOrderedByPrice()
                .map(products -> Response.ok(products).build());
    }

    /**
     * Search products by name.
     */
    @GET
    @Path("/search")
    @Operation(summary = "Search products by name", description = "Searches for products by name (case-insensitive)")
    @APIResponse(responseCode = "200", description = "List of matching products",
            content = @Content(schema = @Schema(implementation = Product.class)))
    public Uni<Response> searchProductsByName(@QueryParam("name") String name) {
        return productService.searchProductsByName(name)
                .map(products -> Response.ok(products).build());
    }

    /**
     * Get products with low stock.
     */
    @GET
    @Path("/low-stock")
    @Operation(summary = "Get low stock products", description = "Retrieves products with stock below the specified threshold")
    @APIResponse(responseCode = "200", description = "List of products with low stock",
            content = @Content(schema = @Schema(implementation = Product.class)))
    public Uni<Response> getLowStockProducts(@QueryParam("threshold") @DefaultValue("10") Integer threshold) {
        return productService.getLowStockProducts(threshold)
                .map(products -> Response.ok(products).build());
    }

    /**
     * Response class for stock availability check.
     */
    public static class StockAvailabilityResponse {
        public Long productId;
        public Integer requestedCount;
        public Boolean available;

        public StockAvailabilityResponse(Long productId, Integer requestedCount, Boolean available) {
            this.productId = productId;
            this.requestedCount = requestedCount;
            this.available = available;
        }
    }
}
