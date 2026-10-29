
package com.feedback.portal.config;

import com.feedback.portal.entity.Category;
import com.feedback.portal.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CategoryInitializer {

    @Bean
    CommandLineRunner createCategories(CategoryRepository repository) {
        return args -> {
            addCategory(repository, "Product", "Product-related feedback");
            addCategory(repository, "Service", "Service-related feedback");
            addCategory(repository, "Support", "Customer support feedback");
            addCategory(repository, "Other", "Other feedback");
        };
    }

    private void addCategory(
            CategoryRepository repository,
            String name,
            String description) {

        boolean exists = repository.findByActiveTrueOrderByNameAsc()
                .stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name));

        if (!exists) {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            category.setActive(true);
            repository.save(category);
        }
    }
}
