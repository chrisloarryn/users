package accounttransaction.api.controllers.concretes;

import accounttransaction.entities.Product;
import accounttransaction.business.abstracts.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products")
public class ProductController implements accounttransaction.api.controllers.abstracts.ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    @Operation(summary = "List Products")
    public List<Product> getAll() {
        return productService.getAll();
    }

    @Override
    @Operation(summary = "Get Product By Id")
    public Product getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @Override
    @Operation(summary = "Create Product", security = @SecurityRequirement(name = "bearerAuth"))
    public Product create(@RequestBody Product product) {
        return productService.create(product);
    }

    @Override
    @Operation(summary = "Update Product", security = @SecurityRequirement(name = "bearerAuth"))
    public Product update(@PathVariable UUID id, @RequestBody Product product) {
        return productService.update(id, product);
    }

    @Override
    @Operation(summary = "Delete Product", security = @SecurityRequirement(name = "bearerAuth"))
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }
}
