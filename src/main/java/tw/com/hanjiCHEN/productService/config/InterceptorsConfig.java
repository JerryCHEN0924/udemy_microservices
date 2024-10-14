package tw.com.hanjiCHEN.productService.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tw.com.hanjiCHEN.productService.products.interceptors.ProductInterceptor;

@Configuration
public class InterceptorsConfig implements WebMvcConfigurer {
    private final ProductInterceptor productInterceptor;

    @Autowired
    public InterceptorsConfig(ProductInterceptor productInterceptor) {
        this.productInterceptor = productInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(productInterceptor)
                .addPathPatterns("/api/products/**");
    }
}
