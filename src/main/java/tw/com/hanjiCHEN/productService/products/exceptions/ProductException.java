package tw.com.hanjiCHEN.productService.products.exceptions;

import org.springframework.lang.Nullable;
import tw.com.hanjiCHEN.productService.products.enums.ProductErrors;

public class ProductException extends Exception {
    private final ProductErrors productErrors;

    @Nullable
    private final String productId;

    public ProductException(ProductErrors productErrors, @Nullable String productId) {
        this.productErrors = productErrors;
        this.productId = productId;
    }

    public ProductErrors getProductErrors() {
        return productErrors;
    }

    @Nullable
    public String getProductId() {
        return productId;
    }
}
