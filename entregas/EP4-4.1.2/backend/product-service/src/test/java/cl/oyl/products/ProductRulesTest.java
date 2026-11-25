package cl.oyl.products;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProductRulesTest {
    @Test
    void acceptsValidProduct() {
        Product product = new Product();
        product.name = "Nueces";
        product.price = 3490;
        product.stock = 18;
        product.minStock = 5;
        assertDoesNotThrow(() -> ProductRules.validate(product));
    }

    @Test
    void rejectsNegativeValues() {
        Product product = new Product();
        product.name = "Nueces";
        product.price = -1;
        assertThrows(IllegalArgumentException.class, () -> ProductRules.validate(product));
    }

    @Test
    void stockChangeIsCalculatedSafely() {
        assertEquals(17, ProductRules.nextStock(18, -1));
        assertThrows(IllegalArgumentException.class, () -> ProductRules.nextStock(0, -1));
        assertThrows(IllegalArgumentException.class, () -> ProductRules.nextStock(5, 0));
    }
}
