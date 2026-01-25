package com.antdevrealm.housechaosmain;

import com.antdevrealm.housechaosmain.cart.model.CartEntity;
import com.antdevrealm.housechaosmain.cart.repository.CartRepository;
import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.repository.CategoryRepository;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.cloudinary.CloudinaryService;
import com.antdevrealm.housechaosmain.role.model.entity.RoleEntity;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.role.repository.RoleRepository;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    private final CloudinaryService cloudinaryService;

    private final List<String> categoryNames = List.of("chair", "table", "couch", "lamp");

    @Autowired
    public DatabaseSeeder(ProductRepository productRepository,
                          CategoryRepository categoryRepository, RoleRepository roleRepository,
                          UserRepository userRepository, CartRepository cartRepository,
                          PasswordEncoder passwordEncoder, CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public void run(String... args) {

        if (this.roleRepository.count() == 0) {
            List<RoleEntity> roles = List.of(
                    RoleEntity.builder()
                            .role(UserRole.USER)
                            .build(),
                    RoleEntity.builder()
                            .role(UserRole.ADMIN)
                            .build()
            );

            this.roleRepository.saveAll(roles);
        }


        if (this.userRepository.count() == 0) {
            UserEntity defaultAdminEntity = UserEntity.builder()
                    .email("admin@email.com")
                    .password(this.passwordEncoder.encode("adminpassword"))
                    .createdOn(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            RoleEntity userRole = this.roleRepository.findByRole(UserRole.USER)
                    .orElseThrow(() -> new ResourceNotFoundException("User role not found"));

            RoleEntity adminRole = this.roleRepository.findByRole(UserRole.ADMIN)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin role not found"));

            defaultAdminEntity.getRoles().add(userRole);
            defaultAdminEntity.getRoles().add(adminRole);

            UserEntity savedEntity = this.userRepository.save(defaultAdminEntity);

            CartEntity adminCart = CartEntity.builder()
                    .owner(savedEntity)
                    .build();

            this.cartRepository.save(adminCart);

        }

        if (this.categoryRepository.count() == 0) {
            categoryNames.forEach(name -> this.categoryRepository.save(CategoryEntity.builder().name(name).build()));
        }

        if (this.productRepository.count() == 0) {
            seedChairs();
            seedTables();
            seedCouches();
            seedLamps();
        }
    }

    private void seedChairs() {
        CategoryEntity chairCategory = categoryRepository.findByName("chair")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category entity with name: \"chair\" not found!"));

        Instant now = Instant.now();

        List<ProductEntity> chairs = List.of(
                ProductEntity.builder()
                        .name("Ashwood Counter Stool")
                        .description("Handcrafted ashwood counter stool featuring clean mid-century lines and a subtle patina that recalls 1950s workshop furniture. A timeless addition to any vintage-inspired interior.")
                        .price(new BigDecimal("129.99"))
                        .quantity(6)
                        .category(chairCategory)
                        .createdOn(now.minus(30, ChronoUnit.DAYS))
                        .updatedAt(now.minus(30, ChronoUnit.DAYS))
                        .newArrival(false)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Ebonwood Modern Dining Chair")
                        .description("Sleek mid-century silhouette in black molded resin with solid oak legs. A minimalist piece that channels vintage café charm and timeless Scandinavian craftsmanship.")
                        .price(new BigDecimal("149.99"))
                        .quantity(8)
                        .category(chairCategory)
                        .createdOn(now.minus(20, ChronoUnit.DAYS))
                        .updatedAt(now.minus(20, ChronoUnit.DAYS))
                        .newArrival(false)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Walnut Crest Lounge Chair")
                        .description("A refined lounge chair with a textured linen seat and sculpted walnut base, evoking the quiet elegance of mid-century reading rooms and boutique salons of the 1960s.")
                        .price(new BigDecimal("189.00"))
                        .quantity(4)
                        .category(chairCategory)
                        .createdOn(now.minus(10, ChronoUnit.DAYS))
                        .updatedAt(now.minus(10, ChronoUnit.DAYS))
                        .newArrival(false)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Vintage Cognac Office Chair")
                        .description("Supple cognac-brown leatherette upholstery with vertical channel stitching on a smooth swivel base. Inspired by 1970s study chairs that balanced elegance with everyday comfort.")
                        .price(new BigDecimal("169.00"))
                        .quantity(5)
                        .category(chairCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Ivory Regency Accent Chair")
                        .description("Elegant tufted velvet upholstery with sculpted spindle legs, echoing the sophistication of early Regency parlors while offering modern comfort and timeless refinement.")
                        .price(new BigDecimal("209.00"))
                        .quantity(3)
                        .category(chairCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Pearl Modernist Side Chair")
                        .description("Sleek matte-white side chair molded in minimalist form, reminiscent of late 1960s Scandinavian design with a clean silhouette suited for both retro and modern interiors.")
                        .price(new BigDecimal("119.00"))
                        .quantity(9)
                        .category(chairCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Golden Oak Heritage Chair")
                        .description("Crafted from solid oak with ornate carved details and a honeyed finish, this classic side chair captures the rustic charm of early farmhouse craftsmanship and Victorian-era elegance.")
                        .price(new BigDecimal("179.00"))
                        .quantity(5)
                        .category(chairCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Arctic Shell Lounge Chair")
                        .description("Curved birch frame with smooth white upholstery, inspired by 1960s Scandinavian shell chairs that blend organic lines with minimalist craftsmanship for serene living spaces.")
                        .price(new BigDecimal("199.00"))
                        .quantity(7)
                        .category(chairCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Graywood Contour Dining Chair")
                        .description("Gracefully curved backrest wrapped in soft gray tweed over a natural oak frame, blending vintage European refinement with enduring mid-century design sensibility.")
                        .price(new BigDecimal("159.00"))
                        .quantity(6)
                        .category(chairCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Onyx Nordic Side Chair")
                        .description("Matte black molded seat paired with angled beechwood legs. A tribute to minimalist Nordic design, evoking the elegance and restraint of 1950s modernist interiors.")
                        .price(new BigDecimal("139.00"))
                        .quantity(10)
                        .category(chairCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build()
        );

        List<String> chairImagePaths = List.of(
                "static/images/chairs/chair-1.jpg",
                "static/images/chairs/chair-2.jpg",
                "static/images/chairs/chair-3.jpg",
                "static/images/chairs/chair-4.jpg",
                "static/images/chairs/chair-5.jpg",
                "static/images/chairs/chair-6.jpg",
                "static/images/chairs/chair-7.jpg",
                "static/images/chairs/chair-8.jpg",
                "static/images/chairs/chair-9.jpg",
                "static/images/chairs/chair-10.jpg"
        );

        for (int i = 0; i < chairs.size(); i++) {
            ProductEntity p = chairs.get(i);

            String publicId = uploadSeedImageFromClasspath(
                    chairImagePaths.get(i),
                    "house-of-chaos/chairs",
                    slugify(p.getName())
            );

            p.setImagePublicId(publicId);
        }

        this.productRepository.saveAll(chairs);
    }


    private void seedTables() {
        CategoryEntity tableCategory = categoryRepository.findByName("table")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category entity with name: \"table\" not found!"));

        Instant now = Instant.now();

        List<ProductEntity> tables = List.of(
                ProductEntity.builder()
                        .name("Workshop Trestle Dining Table")
                        .description("Sun-washed plank top on saw-horse trestle legs, echoing 1930s studio workbenches with a gentle patina that brings vintage atelier charm to modern dining spaces.")
                        .price(new BigDecimal("329.00"))
                        .quantity(5)
                        .category(tableCategory)
                        .createdOn(now.minus(30, ChronoUnit.DAYS))
                        .updatedAt(now.minus(30, ChronoUnit.DAYS))
                        .newArrival(false)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Walnut Grove Round Table")
                        .description("Compact round table in warm walnut tones, recalling 1940s café charm with a modern twist — perfect for intimate dining nooks or vintage-inspired reading corners.")
                        .price(new BigDecimal("249.00"))
                        .quantity(7)
                        .category(tableCategory)
                        .createdOn(now.minus(20, ChronoUnit.DAYS))
                        .updatedAt(now.minus(20, ChronoUnit.DAYS))
                        .newArrival(false)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Nordic Dawn Bedside Table")
                        .description("Compact white side table with dipped oak legs, reflecting Scandinavian minimalism and a soft vintage aesthetic ideal for cozy bedrooms and serene reading spaces.")
                        .price(new BigDecimal("189.00"))
                        .quantity(10)
                        .category(tableCategory)
                        .createdOn(now.minus(10, ChronoUnit.DAYS))
                        .updatedAt(now.minus(10, ChronoUnit.DAYS))
                        .newArrival(false)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Elmwood Harmony Side Table")
                        .description("Polished elmwood side table with soft rounded edges, exuding the quiet simplicity of 1950s Nordic homes and the warm craftsmanship of timeless vintage decor.")
                        .price(new BigDecimal("179.00"))
                        .quantity(8)
                        .category(tableCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Oakline Industrial Dining Table")
                        .description("A modern industrial dining table with a natural oak top and brushed steel base, merging minimalist geometry with the enduring character of mid-century workshop design.")
                        .price(new BigDecimal("299.00"))
                        .quantity(6)
                        .category(tableCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Parisian Bistro Mosaic Table")
                        .description("Charming café table with a tiled top and ornate cast-iron base, evoking the timeless allure of vintage Paris terraces where style and simplicity meet effortlessly.")
                        .price(new BigDecimal("269.00"))
                        .quantity(4)
                        .category(tableCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Rustic Foldaway Patio Table")
                        .description("Handcrafted wooden folding table with weathered charm, reminiscent of vintage garden furniture from sunlit European courtyards and seaside verandas.")
                        .price(new BigDecimal("159.00"))
                        .quantity(9)
                        .category(tableCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Midnight River Epoxy Table")
                        .description("Stunning walnut and black resin table, hand-poured to resemble flowing midnight waters — a modern heirloom blending natural artistry with timeless craftsmanship.")
                        .price(new BigDecimal("449.00"))
                        .quantity(5)
                        .category(tableCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Industrial Loft Dining Table")
                        .description("Solid pine tabletop with matte black steel frame — a striking mix of warmth and strength, recalling the handcrafted furniture of mid-century urban lofts.")
                        .price(new BigDecimal("389.00"))
                        .quantity(6)
                        .category(tableCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Modern Glass-Top Coffee Table")
                        .description("Elegant glass-top coffee table with natural oak base and matte black frame — merging modern design with a hint of vintage charm and quiet sophistication.")
                        .price(new BigDecimal("329.00"))
                        .quantity(8)
                        .category(tableCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build()
        );

        List<String> tableImagePaths = List.of(
                "static/images/tables/table-1.jpg",
                "static/images/tables/table-2.jpg",
                "static/images/tables/table-3.jpg",
                "static/images/tables/table-4.jpg",
                "static/images/tables/table-5.jpg",
                "static/images/tables/table-6.jpg",
                "static/images/tables/table-7.jpg",
                "static/images/tables/table-8.jpg",
                "static/images/tables/table-9.jpg",
                "static/images/tables/table-10.jpg"
        );

        for (int i = 0; i < tables.size(); i++) {
            ProductEntity p = tables.get(i);

            String publicId = uploadSeedImageFromClasspath(
                    tableImagePaths.get(i),
                    "house-of-chaos/tables",
                    slugify(p.getName())
            );

            p.setImagePublicId(publicId);
        }

        this.productRepository.saveAll(tables);
    }

    private void seedCouches() {
        CategoryEntity couchCategory = categoryRepository.findByName("couch")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category entity with name: \"couch\" not found!"));

        Instant now = Instant.now();

        List<ProductEntity> couches = List.of(
                ProductEntity.builder()
                        .name("Saddle Leather Mid-Century Sofa")
                        .description("Supple saddle-tan leather on a streamlined frame with tapered legs—echoing 1960s lounge elegance, aged warmth, and everyday, sink-in comfort.")
                        .price(new BigDecimal("899.00"))
                        .quantity(3)
                        .category(couchCategory)
                        .createdOn(now.minus(30, ChronoUnit.DAYS))
                        .updatedAt(now.minus(30, ChronoUnit.DAYS))
                        .newArrival(false)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Emerald Velvet Retro Sofa")
                        .description("Lush emerald velvet upholstery paired with tapered wooden legs gives this retro-inspired sofa a timeless yet refined vintage charm.")
                        .price(new BigDecimal("949.00"))
                        .quantity(4)
                        .category(couchCategory)
                        .createdOn(now.minus(20, ChronoUnit.DAYS))
                        .updatedAt(now.minus(20, ChronoUnit.DAYS))
                        .newArrival(false)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Modern Gray Comfort Sofa")
                        .description("A soft gray upholstered sofa blending minimalist lines with a cozy vintage tone, perfect for relaxed antique-inspired interiors.")
                        .price(new BigDecimal("899.00"))
                        .quantity(6)
                        .category(couchCategory)
                        .createdOn(now.minus(10, ChronoUnit.DAYS))
                        .updatedAt(now.minus(10, ChronoUnit.DAYS))
                        .newArrival(false)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Cream Leather Retro Sofa")
                        .description("A smooth cream leather sofa with gentle curves and a vintage touch, offering timeless comfort and understated elegance.")
                        .price(new BigDecimal("849.00"))
                        .quantity(5)
                        .category(couchCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Vintage Chesterfield Leather Sofa")
                        .description("A classic dark brown Chesterfield sofa with deep button tufting, exuding timeless elegance and antique craftsmanship.")
                        .price(new BigDecimal("1199.00"))
                        .quantity(3)
                        .category(couchCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Modern Amber Leather Loveseat")
                        .description("A sleek amber leather loveseat with minimalist metal legs, blending mid-century warmth and modern sophistication.")
                        .price(new BigDecimal("999.00"))
                        .quantity(5)
                        .category(couchCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Nordic Gray Fabric Sofa")
                        .description("A refined gray fabric sofa with a Nordic design touch, evoking understated vintage charm and cozy simplicity.")
                        .price(new BigDecimal("850.00"))
                        .quantity(7)
                        .category(couchCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Amber Velvet Loveseat")
                        .description("A rich amber velvet loveseat with a tufted back and classic silhouette. Perfect for elegant spaces that blend vintage charm with modern sophistication.")
                        .price(new BigDecimal("980.00"))
                        .quantity(4)
                        .category(couchCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Gray Mid-Century Sofa")
                        .description("A cozy gray mid-century modern sofa with button tufting and teal accent pillows. Ideal for bright, inviting living spaces.")
                        .price(new BigDecimal("890.00"))
                        .quantity(6)
                        .category(couchCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Yellow Cozy Sofa")
                        .description("A bright yellow two-seater sofa with soft cushions and minimalistic design. Adds a cheerful pop of color to any living space.")
                        .price(new BigDecimal("670.00"))
                        .quantity(7)
                        .category(couchCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build()
        );

        List<String> couchImagePaths = List.of(
                "static/images/couches/couch-1.jpg",
                "static/images/couches/couch-2.jpg",
                "static/images/couches/couch-3.jpg",
                "static/images/couches/couch-4.jpg",
                "static/images/couches/couch-5.jpg",
                "static/images/couches/couch-6.jpg",
                "static/images/couches/couch-7.jpg",
                "static/images/couches/couch-8.jpg",
                "static/images/couches/couch-9.jpg",
                "static/images/couches/couch-10.jpg"
        );

        for (int i = 0; i < couches.size(); i++) {
            ProductEntity p = couches.get(i);

            String publicId = uploadSeedImageFromClasspath(
                    couchImagePaths.get(i),
                    "house-of-chaos/couches",
                    slugify(p.getName())
            );

            p.setImagePublicId(publicId);
        }

        this.productRepository.saveAll(couches);
    }

    private void seedLamps() {
        CategoryEntity lampCategory = categoryRepository.findByName("lamp")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category entity with name: \"lamp\" not found!"));

        Instant now = Instant.now();

        List<ProductEntity> lamps = List.of(
                ProductEntity.builder()
                        .name("Factory Pendant Lamp")
                        .description("Matte enamel shade with exposed hardware, echoing 1940s workshop pendants—soft, focused light with timeless industrial charm.")
                        .price(new BigDecimal("129.00"))
                        .quantity(10)
                        .category(lampCategory)
                        .createdOn(now.minus(30, ChronoUnit.DAYS))
                        .updatedAt(now.minus(30, ChronoUnit.DAYS))
                        .newArrival(false)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Vintage Oil Lantern")
                        .description("Classic metal oil lantern with glass enclosure, offering a warm, flickering glow ideal for outdoor evenings or rustic interiors.")
                        .price(new BigDecimal("89.00"))
                        .quantity(10)
                        .category(lampCategory)
                        .createdOn(now.minus(20, ChronoUnit.DAYS))
                        .updatedAt(now.minus(20, ChronoUnit.DAYS))
                        .newArrival(false)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Matte Black Floor Lamp")
                        .description("Minimalist matte black floor lamp with adjustable head, perfect for reading corners or modern interiors.")
                        .price(new BigDecimal("129.00"))
                        .quantity(10)
                        .category(lampCategory)
                        .createdOn(now.minus(10, ChronoUnit.DAYS))
                        .updatedAt(now.minus(10, ChronoUnit.DAYS))
                        .newArrival(false)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Retro Orange Table Lamp")
                        .description("Mid-century modern table lamp with a bold orange dome shade and warm ambient glow — a stylish accent for any room.")
                        .price(new BigDecimal("149.00"))
                        .quantity(8)
                        .category(lampCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Matte Dome Pendant Lamp")
                        .description("A sleek matte-finish dome pendant lamp with minimalist design and soft downward lighting — ideal for modern interiors or dining areas.")
                        .price(new BigDecimal("169.00"))
                        .quantity(12)
                        .category(lampCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Nordic Wooden Tripod Table Lamp")
                        .description("A cozy Scandinavian-style table lamp with a wooden tripod base and a soft fabric shade. Perfect for bedside tables or reading corners.")
                        .price(new BigDecimal("109.00"))
                        .quantity(20)
                        .category(lampCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Industrial Black Pendant Lamp")
                        .description("A minimalist industrial pendant lamp with a matte black shade and exposed bulb. Perfect for modern kitchens, cafes, or loft-style spaces.")
                        .price(new BigDecimal("95.00"))
                        .quantity(15)
                        .category(lampCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Modern Sculptural Table Lamp")
                        .description("A contemporary sculptural lamp featuring a U-shaped glass base and a frosted globe light. This artistic piece blends elegance and creativity, ideal for minimalist or designer interiors.")
                        .price(new BigDecimal("210.00"))
                        .quantity(10)
                        .category(lampCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Vintage Brass Wall Lamp")
                        .description("An adjustable brass wall-mounted lamp with a black metal shade. Its industrial yet elegant design makes it perfect for reading corners, bedrooms, or refined office interiors.")
                        .price(new BigDecimal("155.00"))
                        .quantity(10)
                        .category(lampCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build(),
                ProductEntity.builder()
                        .name("Modern Glass Floor Lamp")
                        .description("A sleek floor lamp with a cylindrical glass shade and exposed Edison bulb. Its minimalist black frame and warm light make it ideal for cozy modern interiors.")
                        .price(new BigDecimal("185.00"))
                        .quantity(10)
                        .category(lampCategory)
                        .createdOn(now)
                        .updatedAt(now)
                        .newArrival(true)
                        .isActive(true)
                        .build()
        );

        List<String> lampImagePaths = List.of(
                "static/images/lamps/lamp-1.jpg",
                "static/images/lamps/lamp-2.jpg",
                "static/images/lamps/lamp-3.jpg",
                "static/images/lamps/lamp-4.jpg",
                "static/images/lamps/lamp-5.jpg",
                "static/images/lamps/lamp-6.jpg",
                "static/images/lamps/lamp-7.jpg",
                "static/images/lamps/lamp-8.jpg",
                "static/images/lamps/lamp-9.jpg",
                "static/images/lamps/lamp-10.jpg"
        );

        for (int i = 0; i < lamps.size(); i++) {
            ProductEntity p = lamps.get(i);

            String publicId = uploadSeedImageFromClasspath(
                    lampImagePaths.get(i),
                    "house-of-chaos/lamps",
                    slugify(p.getName())
            );

            p.setImagePublicId(publicId);
        }

        this.productRepository.saveAll(lamps);
    }

    private String uploadSeedImageFromClasspath(String classpathLocation,
                                                String folder,
                                                String publicId) {
        try (InputStream inputStream = new ClassPathResource(classpathLocation).getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            return cloudinaryService.uploadSeedImage(bytes, folder, publicId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload seed image: " + classpathLocation, e);
        }
    }

    private String slugify(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

}
