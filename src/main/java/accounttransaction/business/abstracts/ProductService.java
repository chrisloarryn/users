package accounttransaction.business.abstracts;

import accounttransaction.entities.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<Product> getAll();
    Product getById(UUID id);
    Product create(Product product);
    Product update(UUID id, Product product);
    void delete(UUID id);
}
