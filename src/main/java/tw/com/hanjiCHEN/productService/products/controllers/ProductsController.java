package tw.com.hanjiCHEN.productService.products.controllers;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.juli.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.dynamodb.endpoints.internal.Value;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import tw.com.hanjiCHEN.productService.events.dto.EventType;
import tw.com.hanjiCHEN.productService.events.services.EventsPublisher;
import tw.com.hanjiCHEN.productService.products.dto.ProductDto;
import tw.com.hanjiCHEN.productService.products.enums.ProductErrors;
import tw.com.hanjiCHEN.productService.products.exceptions.ProductException;
import tw.com.hanjiCHEN.productService.products.models.Product;
import tw.com.hanjiCHEN.productService.products.repositories.ProductsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/products")
@XRayEnabled
public class ProductsController {
    private static final Logger LOG = LogManager.getLogger(ProductsController.class);
    private final ProductsRepository productsRepository;
    private final EventsPublisher eventsPublisher;

    @Autowired
    public ProductsController(ProductsRepository productsRepository, EventsPublisher eventsPublisher) {
        this.productsRepository = productsRepository;
        this.eventsPublisher = eventsPublisher;
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts(@RequestParam(required = false) String code)
            throws ProductException {
        if (code != null) {
            LOG.info("Get product by code:{}", code);
            Product productByCode = productsRepository.getByCode(code).join();
            if (productByCode != null) {
                return new ResponseEntity<>(new ProductDto(productByCode), HttpStatus.OK);
            } else {
                throw new ProductException(ProductErrors.PRODUCT_NOT_FOUND, null);
            }
        } else {
            LOG.info("Get all products");
            List<ProductDto> productDtoList = new ArrayList<>();

            productsRepository.getAll().items().subscribe(product -> {
                productDtoList.add(new ProductDto(product));
            }).join();

            return new ResponseEntity<>(productDtoList, HttpStatus.OK);
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable("id") String id) throws ProductException {
        Product product = productsRepository.getById(id).join();
        if (product != null) {
            LOG.info("Get products by its id: {}", id);
            return new ResponseEntity<>(new ProductDto(product), HttpStatus.OK);
        } else {
            throw new ProductException(ProductErrors.PRODUCT_NOT_FOUND, id);
        }
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto)
            throws ProductException, JsonProcessingException, ExecutionException, InterruptedException {
        Product productCreated = ProductDto.toProduct(productDto);
        productCreated.setId(UUID.randomUUID().toString()); //Dynamo DB不會產生ID，在此自己設定。

        /*
        productCompletableFeature
        publishResponseCompletableFuture
        兩個將會並行處理(parallel)
         */
        CompletableFuture<Void> productCompletableFeature = productsRepository.create(productCreated);

        CompletableFuture<PublishResponse> publishResponseCompletableFuture =
                eventsPublisher.sendProductEvent(productCreated,
                        EventType.PRODUCT_CREATED, "jk2455892@gmail.com");

        //等候兩個CompletableFuture完成
        CompletableFuture.allOf(productCompletableFeature,publishResponseCompletableFuture).join();

        PublishResponse publishResponse = publishResponseCompletableFuture.get();
        //ThreadContext.put("messageId", publishResponse.messageId());

        LOG.info("Product created - ID:{}", productCreated.getId());
        return new ResponseEntity<>(new ProductDto(productCreated), HttpStatus.CREATED);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ProductDto> deleteProduct(@PathVariable("id") String id)
            throws ProductException, JsonProcessingException {
        Product productDeleted = productsRepository.deleteById(id).join();
        if (productDeleted != null) {
            PublishResponse publishResponse = eventsPublisher.sendProductEvent(productDeleted,
                    EventType.PRODUCT_DELETED, "jk2455892@gmail.com").join();
            ThreadContext.put("messageId", publishResponse.messageId());

            LOG.info("Product deleted - ID: {}", productDeleted.getId());
            return new ResponseEntity<>(new ProductDto(productDeleted), HttpStatus.OK);
        } else {
            throw new ProductException(ProductErrors.PRODUCT_NOT_FOUND, id);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<ProductDto> updateProduct(@RequestBody ProductDto productDto,
                                                    @PathVariable("id") String id)
            throws ProductException {
        try {
            Product productUpdated = productsRepository.update(ProductDto.toProduct(productDto), id).join();

            PublishResponse publishResponse = eventsPublisher.sendProductEvent(productUpdated, EventType.PRODUCT_UPDATED,
                    "jk2455892@gmail.com").join();
            ThreadContext.put("messageId", publishResponse.messageId());

            LOG.info("Product updated - ID:{}", productUpdated.getId());
            return new ResponseEntity<>(new ProductDto(productUpdated), HttpStatus.OK);
        } catch (CompletionException | JsonProcessingException completionException) {
            throw new ProductException(ProductErrors.PRODUCT_NOT_FOUND, id);
        }
    }
}
