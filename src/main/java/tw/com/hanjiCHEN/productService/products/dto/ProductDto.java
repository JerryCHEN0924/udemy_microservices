package tw.com.hanjiCHEN.productService.products.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import tw.com.hanjiCHEN.productService.products.models.Product;

public record ProductDto(
        String id, String name, String code, float price, String model,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String url
) {
    public ProductDto(Product product) {
        this(product.getId(), product.getProductName(), product.getCode(),
                product.getPrice(), product.getModel(), product.getProductUrl());
    }

    static public Product toProduct(ProductDto productDto) {
        Product product = new Product();
        product.setId(product.getId());
        product.setProductName(productDto.name);
        product.setCode(productDto.code());
        product.setPrice(productDto.price());
        product.setModel(productDto.model());
        product.setProductUrl(productDto.url());
        return product;
    }
}
