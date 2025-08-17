package accounttransaction.api.controllers.concretes;

import accounttransaction.entities.Product;
import accounttransaction.business.abstracts.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController implements accounttransaction.api.controllers.abstracts.ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public List<Product> getAll() {
        return productService.getAll();
    }

    @Override
    public Product getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @Override
    public Product create(@RequestBody Product product) {
        return productService.create(product);
    }

    @Override
    public Product update(@PathVariable UUID id, @RequestBody Product product) {
        return productService.update(id, product);
    }

    @Override
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }
}
