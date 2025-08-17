package accounttransaction.api.controllers.abstracts;

import accounttransaction.entities.Product;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
public interface ProductController {

    @GetMapping
    List<Product> getAll();

    @GetMapping("/{id}")
    Product getById(@PathVariable UUID id);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Product create(@RequestBody Product product);

    @PutMapping("/{id}")
    Product update(@PathVariable UUID id, @RequestBody Product product);

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id);
}
