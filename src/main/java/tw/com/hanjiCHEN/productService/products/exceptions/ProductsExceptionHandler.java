package tw.com.hanjiCHEN.productService.products.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import tw.com.hanjiCHEN.productService.events.dto.ProductFailureEventDto;
import tw.com.hanjiCHEN.productService.events.services.EventsPublisher;
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
    private final EventsPublisher eventsPublisher;

    @Autowired
    public ProductsExceptionHandler(EventsPublisher eventsPublisher) {
        this.eventsPublisher = eventsPublisher;
    }

    @ExceptionHandler(value = {ProductException.class})
    protected ResponseEntity<Object> handleProductError(ProductException productException, WebRequest webRequest)
            throws JsonProcessingException {
        ProductErrorResponse productErrorResponse = new ProductErrorResponse(
                productException.getProductErrors().getMessage(),
                productException.getProductErrors().getHttpStatus().value(),
                ThreadContext.get("requestId"),
                productException.getProductId()
        );

        ProductFailureEventDto productFailureEventDto = new ProductFailureEventDto(
                "jk2455892@gmail.com",
                productException.getProductErrors().getHttpStatus().value(),
                productException.getProductErrors().getMessage(),
                productException.getProductId()
        );

        //發布ProductFailure事件
        PublishResponse publishResponse = eventsPublisher.sendProductFailureEvent(productFailureEventDto).join();
        ThreadContext.put("messageId", publishResponse.messageId());

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
