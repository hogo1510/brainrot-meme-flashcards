package org.lucas.brainrotmemeflashcards.controller;

import org.lucas.brainrotmemeflashcards.model.Category;
import org.lucas.brainrotmemeflashcards.model.Flashcard;
import org.lucas.brainrotmemeflashcards.service.FlashcardService;
import org.lucas.brainrotmemeflashcards.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/")
public class FlashcardController {
    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private CategoryService categoryService;

    @Value("${app.upload.dir:${user.dir}/src/main/resources/static/images}")
    private String uploadDirectory;

    // Homepage - show all flashcards
    @GetMapping
    public String showHomePage(Model model) {
        model.addAttribute("flashcards", flashcardService.getAllFlashcards());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "index";
    }

    // Show form to add new flashcard
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("flashcard", new Flashcard());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("availableImages", flashcardService.getAllAvailableImages());
        return "add-card";
    }

    // Process adding new flashcard
    @PostMapping("/add")
    public String addFlashcard(@Valid @ModelAttribute Flashcard flashcard,
                               BindingResult result,
                               @RequestParam(value = "imageFile", required = false) MultipartFile file,
                               @RequestParam(value = "selectedImage", required = false) String selectedImage,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("availableImages", flashcardService.getAllAvailableImages());
            return "add-card";
        }

        // Handle image upload
        if (file != null && !file.isEmpty()) {
            try {
                // Create upload directory if it doesn't exist
                Path uploadPath = Paths.get(uploadDirectory);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generate unique filename
                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFilename = System.currentTimeMillis() + fileExtension;

                // Save file
                Path filePath = uploadPath.resolve(uniqueFilename);
                Files.write(filePath, file.getBytes());

                // Set image URL
                flashcard.setImageUrl("/images/" + uniqueFilename);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("message", "Fout bij uploaden afbeelding: " + e.getMessage());
                return "redirect:/add";
            }
        } else if (selectedImage != null && !selectedImage.isEmpty()) {
            // Use selected existing image
            flashcard.setImageUrl(selectedImage);
        } else {
            // Assign random image if no image is selected
            String randomImage = flashcardService.getRandomImage();
            if (randomImage != null) {
                flashcard.setImageUrl(randomImage);
            }
        }

        flashcardService.saveFlashcard(flashcard);
        redirectAttributes.addFlashAttribute("message", "Flashcard toegevoegd!");
        return "redirect:/";
    }

    // Show study mode
    @GetMapping("/study")
    public String showStudyMode(Model model) {
        List<Flashcard> flashcards = flashcardService.getAllFlashcards();
        if (flashcards.isEmpty()) {
            return "redirect:/add";
        }
        model.addAttribute("flashcards", flashcards);
        return "study";
    }

    // Study by category
    @GetMapping("/study/category/{id}")
    public String studyByCategory(@PathVariable Long id, Model model) {
        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            List<Flashcard> flashcards = flashcardService.getFlashcardsByCategory(category.get());
            model.addAttribute("flashcards", flashcards);
            model.addAttribute("categoryName", category.get().getName());
            return "study";
        }
        return "redirect:/study";
    }

    // Show categories page
    @GetMapping("/categories")
    public String showCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", new Category());
        return "categories";
    }

    // Add new category
    @PostMapping("/categories/add")
    public String addCategory(@Valid @ModelAttribute Category category,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "categories";
        }

        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("message", "Categorie toegevoegd!");
        return "redirect:/categories";
    }

    // Delete flashcard
    @GetMapping("/delete/{id}")
    public String deleteFlashcard(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        flashcardService.deleteFlashcard(id);
        redirectAttributes.addFlashAttribute("message", "Flashcard verwijderd!");
        return "redirect:/";
    }

    // Randomize images for all flashcards without images
    @GetMapping("/randomize-images")
    public String randomizeImages(RedirectAttributes redirectAttributes) {
        List<Flashcard> allFlashcards = flashcardService.getAllFlashcards();
        int updatedCount = 0;

        for (Flashcard flashcard : allFlashcards) {
            if (flashcard.getImageUrl() == null || flashcard.getImageUrl().isEmpty()) {
                String randomImage = flashcardService.getRandomImage();
                if (randomImage != null) {
                    flashcard.setImageUrl(randomImage);
                    flashcardService.saveFlashcard(flashcard);
                    updatedCount++;
                }
            }
        }

        redirectAttributes.addFlashAttribute("message",
                updatedCount + " flashcards hebben willekeurige afbeeldingen gekregen!");
        return "redirect:/";
    }
}
