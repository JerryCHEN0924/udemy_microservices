package tw.com.hanjiCHEN.productService.products.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import tw.com.hanjiCHEN.productService.products.models.Product;

@Repository
public class ProductsRepository {
    private final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    private final DynamoDbAsyncTable<Product> productsTable;

    @Autowired
    public ProductsRepository(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                              @Value("aws.productsddb.name") String productsDdbName) {
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.productsTable = dynamoDbEnhancedAsyncClient.table(productsDdbName, TableSchema.fromBean(Product.class));

    }
}
