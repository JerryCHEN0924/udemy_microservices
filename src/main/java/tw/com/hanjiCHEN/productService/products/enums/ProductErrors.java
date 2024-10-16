package tw.com.hanjiCHEN.productService.products.enums;

import org.springframework.http.HttpStatus;

public enum ProductErrors {
    PRODUCT_NOT_FOUND("Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_CODE_ALREADY_EXISTS("",HttpStatus.CONFLICT)

    ;

    private final String message;
    private final HttpStatus httpStatus;

    ProductErrors(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
