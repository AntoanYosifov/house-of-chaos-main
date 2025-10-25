//package com.antdevrealm.housechaosmain;
//
//import com.antdevrealm.housechaosmain.features.product.model.ProductEntity;
//import com.antdevrealm.housechaosmain.features.product.service.ProductService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//
//@Component
//public class Runner implements CommandLineRunner {
//
//    private final ProductService productService;
//
//    @Autowired
//    public Runner(ProductService productService) {
//        this.productService = productService;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        ProductEntity product = ProductEntity.builder()
//                .name("Product one(name)")
//                .description("This is the description")
//                .price(new BigDecimal(20))
//                .imageUrl("URL")
//                .build();
//
//        this.productService.create(product);
//    }
//}
