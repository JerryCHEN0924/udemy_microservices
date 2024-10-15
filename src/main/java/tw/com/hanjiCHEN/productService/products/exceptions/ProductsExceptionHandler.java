package tw.com.hanjiCHEN.productService.products.exceptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tw.com.hanjiCHEN.productService.products.dto.ProductErrorResponse;

/*
@RestControllerAdvice集中處理 RESTful Web 應用中的全局異常處理（exception handling）以及跨控制器的建議（advice）
1.全局異常處理
集中處理所有控制器中的異常(Exception)
通常返回 JSON 格式的異常響應，以保持 RESTful API 的一致性。
2.跨控制器的建議
3.返回統一格式的錯誤響應
 */
@RestControllerAdvice
public class ProductsExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LogManager.getLogger(ProductsExceptionHandler.class);

    @ExceptionHandler(value = {ProductException.class})
    protected ResponseEntity<Object> handleProductError(ProductException productException, WebRequest webRequest) {
        ProductErrorResponse productErrorResponse = new ProductErrorResponse(
                productException.getProductErrors().getMessage(),
                productException.getProductErrors().getHttpStatus().value(),
                ThreadContext.get("requestId"),
                productException.getProductId()
        );
        LOG.error(productException.getProductErrors().getMessage());

        return handleExceptionInternal(
                productException,
                productErrorResponse,
                new HttpHeaders(),
                productException.getProductErrors().getHttpStatus(),
                webRequest
        );
    }
}
