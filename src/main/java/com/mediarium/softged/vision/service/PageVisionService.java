package com.mediarium.softged.vision.service;

import com.mediarium.softged.shared.exception.TechnicalException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class PageVisionService {

    private final ChatClient chatClient;

    public PageVisionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String summarizeArchitecturePage(Path imagePath, String extractedText) {
        try {
            FileSystemResource imageResource = new FileSystemResource(imagePath);

            String prompt = """
                    Tu analyses une page de PDF d'architecture rendue en image.

                    Objectif :
                    - décrire ce que montre la page ;
                    - identifier si possible le type de document : plan, coupe, façade, détail, notice, cartouche ;
                    - repérer les éléments visibles : pièces, portes, escaliers, circulations, cotes, annotations, légendes ;
                    - ne pas inventer d'information absente ;
                    - rester utile pour une future recherche RAG.

                    Texte extrait automatiquement de la page :
                    %s

                    Réponds en français, de manière structurée mais concise.
                    """.formatted(safeText(extractedText));

            return chatClient
                    .prompt()
                    .user(user -> user
                            .text(prompt)
                            .media(Media.Format.IMAGE_PNG, imageResource)
                    )
                    .call()
                    .content();

        } catch (Exception exception) {
            throw new TechnicalException("Unable to generate visual summary", exception);
        }
    }

    private String safeText(String text) {
        if (text == null || text.isBlank()) {
            return "Aucun texte extractible.";
        }

        return text.length() > 4000
                ? text.substring(0, 4000)
                : text;
    }
}