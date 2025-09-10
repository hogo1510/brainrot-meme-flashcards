package org.lucas.brainrotmemeflashcards.service;

import org.lucas.brainrotmemeflashcards.model.Category;
import org.lucas.brainrotmemeflashcards.model.Flashcard;
import org.lucas.brainrotmemeflashcards.repo.FlashcardRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class FlashcardService {
    @Autowired
    private FlashcardRepo flashcardRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("classpath:static/images/")
    private Resource imagesResource;

    private final Random random = new Random();

    public List<Flashcard> getAllFlashcards() {
        List<Flashcard> flashcards = flashcardRepository.findAll();
        // Wijs willekeurige afbeeldingen toe aan flashcards zonder afbeelding
        for (Flashcard flashcard : flashcards) {
            if (flashcard.getImageUrl() == null || flashcard.getImageUrl().isEmpty()) {
                String randomImage = getRandomImage();
                if (randomImage != null) {
                    flashcard.setImageUrl(randomImage);
                }
            }
        }
        return flashcards;
    }

    public Optional<Flashcard> getFlashcardById(Long id) {
        Optional<Flashcard> flashcard = flashcardRepository.findById(id);
        flashcard.ifPresent(f -> {
            if (f.getImageUrl() == null || f.getImageUrl().isEmpty()) {
                String randomImage = getRandomImage();
                if (randomImage != null) {
                    f.setImageUrl(randomImage);
                }
            }
        });
        return flashcard;
    }

    public List<Flashcard> getFlashcardsByCategory(Category category) {
        List<Flashcard> flashcards = flashcardRepository.findByCategory(category);
        for (Flashcard flashcard : flashcards) {
            if (flashcard.getImageUrl() == null || flashcard.getImageUrl().isEmpty()) {
                String randomImage = getRandomImage();
                if (randomImage != null) {
                    flashcard.setImageUrl(randomImage);
                }
            }
        }
        return flashcards;
    }

    public List<Flashcard> getFlashcardsByCategoryId(Long categoryId) {
        List<Flashcard> flashcards = flashcardRepository.findByCategoryId(categoryId);
        for (Flashcard flashcard : flashcards) {
            if (flashcard.getImageUrl() == null || flashcard.getImageUrl().isEmpty()) {
                String randomImage = getRandomImage();
                if (randomImage != null) {
                    flashcard.setImageUrl(randomImage);
                }
            }
        }
        return flashcards;
    }

    public Flashcard saveFlashcard(Flashcard flashcard) {
        return flashcardRepository.save(flashcard);
    }

    public void deleteFlashcard(Long id) {
        flashcardRepository.deleteById(id);
    }

    /**
     * Haalt een willekeurige afbeelding op uit de images folder
     * @return het pad naar een willekeurige afbeelding of null als er geen afbeeldingen zijn
     */
    public String getRandomImage() {
        try {
            // Probeer verschillende manieren om de images folder te vinden
            Path imagesPath = null;

            // Methode 1: Via classpath
            try {
                Resource resource = resourceLoader.getResource("classpath:static/images");
                if (resource.exists()) {
                    imagesPath = Paths.get(resource.getURI());
                }
            } catch (Exception e) {
                System.out.println("Classpath methode faalde: " + e.getMessage());
            }

            // Methode 2: Via relative path
            if (imagesPath == null || !Files.exists(imagesPath)) {
                imagesPath = Paths.get("src/main/resources/static/images");
            }

            // Methode 3: Via absolute path
            if (!Files.exists(imagesPath)) {
                imagesPath = Paths.get(System.getProperty("user.dir"), "src/main/resources/static/images");
            }

            // Controleer of de folder bestaat
            if (Files.exists(imagesPath) && Files.isDirectory(imagesPath)) {
                List<Path> imageFiles = new ArrayList<>();

                // Zoek naar afbeeldingsbestanden
                Files.list(imagesPath)
                        .filter(path -> {
                            String fileName = path.getFileName().toString().toLowerCase();
                            return Files.isRegularFile(path) &&
                                    (fileName.endsWith(".jpg") ||
                                            fileName.endsWith(".jpeg") ||
                                            fileName.endsWith(".png") ||
                                            fileName.endsWith(".gif") ||
                                            fileName.endsWith(".webp"));
                        })
                        .forEach(imageFiles::add);

                if (!imageFiles.isEmpty()) {
                    Path randomImage = imageFiles.get(random.nextInt(imageFiles.size()));
                    return "/images/" + randomImage.getFileName().toString();
                } else {
                    System.out.println("Geen afbeeldingen gevonden in: " + imagesPath);
                }
            } else {
                System.out.println("Images folder niet gevonden: " + imagesPath);
                // Probeer de folder aan te maken
                try {
                    Files.createDirectories(imagesPath);
                    System.out.println("Images folder aangemaakt: " + imagesPath);
                } catch (IOException e) {
                    System.out.println("Kon images folder niet aanmaken: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Fout bij het lezen van images folder: " + e.getMessage());
        }
        return null;
    }

    /**
     * Haalt alle beschikbare afbeeldingen op uit de images folder
     * @return lijst van afbeeldingspaden
     */
    public List<String> getAllAvailableImages() {
        List<String> images = new ArrayList<>();
        try {
            Path imagesPath = Paths.get(System.getProperty("user.dir"), "src/main/resources/static/images");

            if (Files.exists(imagesPath) && Files.isDirectory(imagesPath)) {
                Files.list(imagesPath)
                        .filter(path -> {
                            String fileName = path.getFileName().toString().toLowerCase();
                            return Files.isRegularFile(path) &&
                                    (fileName.endsWith(".jpg") ||
                                            fileName.endsWith(".jpeg") ||
                                            fileName.endsWith(".png") ||
                                            fileName.endsWith(".gif") ||
                                            fileName.endsWith(".webp"));
                        })
                        .map(path -> "/images/" + path.getFileName().toString())
                        .forEach(images::add);
            }
        } catch (IOException e) {
            System.err.println("Fout bij het lezen van images folder: " + e.getMessage());
        }
        return images;
    }
}
