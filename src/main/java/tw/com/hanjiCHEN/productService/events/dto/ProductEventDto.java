package tw.com.hanjiCHEN.productService.events.dto;

public record ProductEventDto(
        String id,
        String code,
        String email,
        float price
){ }
