package com.mediarium.softged.rag.service;

import com.mediarium.softged.project.service.ProjectService;
import com.mediarium.softged.rag.businessmodel.RagAnswer;
import com.mediarium.softged.rag.businessmodel.RagSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private final ProjectService projectService;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagService(
            ProjectService projectService,
            VectorStore vectorStore,
            ChatClient.Builder chatClientBuilder
    ) {
        this.projectService = projectService;
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    public RagAnswer ask(Long projectId, String ownerUid, String question) {
        projectService.findById(projectId, ownerUid);

        List<Document> documents = searchRelevantDocuments(
                projectId,
                ownerUid,
                question
        );

        if (documents.isEmpty()) {
            return new RagAnswer(
                    "Je n’ai trouvé aucune source pertinente dans les documents indexés de ce projet.",
                    List.of()
            );
        }

        String context = buildContext(documents);

        String answer = chatClient
                .prompt()
                .system("""
                        Tu es un assistant GED spécialisé dans l’analyse de documents d’architecture.

                        Règles :
                        - Réponds uniquement à partir du contexte fourni.
                        - Si le contexte ne permet pas de répondre, dis-le clairement.
                        - Ne fais pas d’hypothèse non justifiée.
                        - Réponds en français.
                        - Mentionne les pages utilisées quand c’est pertinent.
                        """)
                .user("""
                        Question utilisateur :
                        %s

                        Contexte documentaire :
                        %s
                        """.formatted(question, context))
                .call()
                .content();

        return new RagAnswer(
                answer,
                extractSources(documents)
        );
    }

    private List<Document> searchRelevantDocuments(
            Long projectId,
            String ownerUid,
            String question
    ) {
        String filterExpression = "ownerUid == '%s' && projectId == '%s'"
                .formatted(ownerUid, projectId);

        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(5)
                .filterExpression(filterExpression)
                .build();

        return vectorStore.similaritySearch(request);
    }

    private String buildContext(List<Document> documents) {
        StringBuilder context = new StringBuilder();

        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            Map<String, Object> metadata = document.getMetadata();

            context.append("Source ")
                    .append(i + 1)
                    .append("\n");

            context.append("Document : ")
                    .append(value(metadata, "originalFilename"))
                    .append("\n");

            context.append("Page : ")
                    .append(value(metadata, "pageNumber"))
                    .append("\n");

            context.append("Contenu :\n")
                    .append(document.getText())
                    .append("\n\n");
        }

        return context.toString();
    }

    private List<RagSource> extractSources(List<Document> documents) {
        Map<String, RagSource> uniqueSources = new LinkedHashMap<>();

        for (Document document : documents) {
            Map<String, Object> metadata = document.getMetadata();

            RagSource source = new RagSource(
                    value(metadata, "documentId"),
                    value(metadata, "originalFilename"),
                    value(metadata, "pageId"),
                    value(metadata, "pageNumber"),
                    value(metadata, "imagePath")
            );

            String key = source.documentId() + "-" + source.pageNumber();
            uniqueSources.putIfAbsent(key, source);
        }

        return uniqueSources.values()
                .stream()
                .toList();
    }

    private String value(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        return value == null ? null : String.valueOf(value);
    }
}