package com.antdevrealm.housechaosmain;

import com.antdevrealm.housechaosmain.advice.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.features.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.features.category.repository.CategoryRepository;
import com.antdevrealm.housechaosmain.features.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.features.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class productSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private final List<String> categoryNames = List.of("chair", "table", "couch", "lamp");

    @Autowired
    public productSeeder(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (this.categoryRepository.count() == 0) {
            categoryNames.forEach(name -> this.categoryRepository.save(CategoryEntity.builder().name(name).build()));
        }

        if (this.productRepository.count() == 0) {
            seedChairs();
        }

    }

    private void seedChairs() {
        CategoryEntity chairCategory = categoryRepository.findByName("chair").
                orElseThrow(() -> new ResourceNotFoundException("Category entity with name \"chair\" does not found!"));

        List<ProductEntity> chairs;
        chairs = List.of(
                ProductEntity.builder()
                        .name("Ashwood Counter Stool")
                        .description("Handcrafted ashwood counter stool featuring clean mid-century lines and a subtle patina that recalls 1950s workshop furniture. A timeless addition to any vintage-inspired interior.")
                        .price(new BigDecimal("129.99"))
                        .quantity(6)
                        .category(chairCategory)
                        .imageUrl("/images/chairs/chair-1.jpg")
                        .build(),
                ProductEntity.builder()
                        .name("Ebonwood Modern Dining Chair")
                        .description("Sleek mid-century silhouette in black molded resin with solid oak legs. A minimalist piece that channels vintage café charm and timeless Scandinavian craftsmanship.")
                        .price(new BigDecimal("149.99"))
                        .quantity(8)
                        .category(chairCategory)
                        .imageUrl("/images/chairs/chair-2.jpg")
                        .build(),
                ProductEntity.builder()
                        .name("Walnut Crest Lounge Chair")
                        .description("A refined lounge chair with a textured linen seat and sculpted walnut base, evoking the quiet elegance of mid-century reading rooms and boutique salons of the 1960s.")
                        .price(new BigDecimal("189.00"))
                        .quantity(4)
                        .category(chairCategory)
                        .imageUrl("/images/chairs/chair-3.jpg")
                        .build(),
                ProductEntity.builder()
                        .name("Vintage Cognac Office Chair")
                        .description("Supple cognac-brown leatherette upholstery with vertical channel stitching on a smooth swivel base. Inspired by 1970s study chairs that balanced elegance with everyday comfort.")
                        .price(new BigDecimal("169.00"))
                        .quantity(5)
                        .category(chairCategory)
                        .imageUrl("/images/chairs/chair-4.jpg")
                        .build(),
                ProductEntity.builder()
                        .name("Ivory Regency Accent Chair")
                        .description("Elegant tufted velvet upholstery with sculpted spindle legs, echoing the sophistication of early Regency parlors while offering modern comfort and timeless refinement.")
                        .price(new BigDecimal("209.00"))
                        .quantity(3)
                        .category(chairCategory)
                        .imageUrl("/images/chairs/chair-5.jpg")
                        .build(),
                ProductEntity.builder()
                        .name("Pearl Modernist Side Chair")
                        .description("Sleek matte-white side chair molded in minimalist form, reminiscent of late 1960s Scandinavian design with a clean silhouette suited for both retro and modern interiors.")
                        .price(new BigDecimal("119.00"))
                        .quantity(9)
                        .category(chairCategory)
                        .imageUrl("/images/chairs/chair-6.jpg")
                        .build(),
                ProductEntity.builder()
                        .name("Golden Oak Heritage Chair")
                        .description("Crafted from solid oak with ornate carved details and a honeyed finish, this classic side chair captures the rustic charm of early farmhouse craftsmanship and Victorian-era elegance.")
                        .price(new BigDecimal("179.00"))
                        .quantity(5)
                        .category(chairCategory)
                        .imageUrl("/images/chairs/chair-7.jpg")
                        .build(),
                ProductEntity.builder()
                        .name("Arctic Shell Lounge Chair")
                        .description("Curved birch frame with smooth white upholstery, inspired by 1960s Scandinavian shell chairs that blend organic lines with minimalist craftsmanship for serene living spaces.")
                        .price(new BigDecimal("199.00"))
                        .quantity(7)
                        .category(chairCategory)
                        .imageUrl("/images/chairs/chair-8.jpg")
                        .build(),
                ProductEntity.builder()
                        .name("Graywood Contour Dining Chair")
                        .description("Gracefully curved backrest wrapped in soft gray tweed over a natural oak frame, blending vintage European refinement with enduring mid-century design sensibility.")
                        .price(new BigDecimal("159.00"))
                        .quantity(6)
                        .category(chairCategory)
                        .imageUrl("/images/chairs/chair-9.jpg")
                        .build(),
                ProductEntity.builder()
                        .name("Onyx Nordic Side Chair")
                        .description("Matte black molded seat paired with angled beechwood legs. A tribute to minimalist Nordic design, evoking the elegance and restraint of 1950s modernist interiors.")
                        .price(new BigDecimal("139.00"))
                        .quantity(10)
                        .category(chairCategory)
                        .imageUrl("/images/chairs/chair-10.jpg")
                        .build()
                );

        this.productRepository.saveAll(chairs);
    }
}
