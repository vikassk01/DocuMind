package com.documind.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final List<String> chunks = new ArrayList<>();
    private final List<double[]> embeddings = new ArrayList<>();

    public DocumentService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String uploadDocument(MultipartFile file) throws IOException {
       
        Path tempFile = Files.createTempFile("documind-", ".pdf");
        file.transferTo(tempFile.toFile());

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                new FileSystemResource(tempFile.toFile())
        );
        List<Document> documents = pdfReader.get();
        Files.deleteIfExists(tempFile);

        chunks.clear();
        embeddings.clear();
        
        int count = 0;
        for (Document doc : documents) {
            String text = doc.getText();
            if (text == null || text.trim().isEmpty()) continue;
            double[] embedding = getEmbedding(text);
            if (embedding != null) {
                chunks.add(text);
                embeddings.add(embedding);
                count++;
            }
        }

        return "Document uploaded and indexed! Pages indexed: " + count;
    }

    public String askQuestion(String question) throws IOException, InterruptedException {
        if (chunks.isEmpty()) {
            return "No document uploaded yet. Please upload a PDF first.";
        }

        double[] questionEmbedding = getEmbedding(question);
        if (questionEmbedding == null) {
            return "Could not process your question. Please try again.";
        }

        List<String> topChunks = findTopK(questionEmbedding, 3);
        String context = String.join("\n\n", topChunks);

        String prompt = """
                You are a helpful assistant. Answer the question based
                only on the context provided below.
                If the answer is not in the context, say
                "I could not find this in the document."

                Context:
                %s

                Question: %s
                """.formatted(context, question);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    private double[] getEmbedding(String text) {
        try {
            if (text.length() > 2000) {
                text = text.substring(0, 2000);
            }

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "gemini-embedding-001:embedContent?key=" + apiKey;

            String body = objectMapper.writeValueAsString(Map.of(
                    "model", "models/gemini-embedding-001",
                    "content", Map.of(
                            "parts", List.of(Map.of("text", text))
                    )
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("Embedding API response: " + response.body());

            Map<?, ?> responseMap = objectMapper.readValue(
                    response.body(), Map.class
            );

            Object embeddingObj = responseMap.get("embedding");
            if (embeddingObj == null) {
                embeddingObj = responseMap.get("embeddings");
            }

            if (embeddingObj == null) {
            	System.err.println("No embedding in response. Full response: "
            	        + response.body());
                return null;
            }

            Map<?, ?> embedding = (Map<?, ?>) embeddingObj;
            List<?> values = (List<?>) embedding.get("values");

            return values.stream()
                    .mapToDouble(v -> ((Number) v).doubleValue())
                    .toArray();

        } catch (Exception e) {
            System.err.println("Embedding error: " + e.getMessage());
            return null;
        }
    }

    private List<String> findTopK(double[] query, int k) {
        List<double[]> scores = new ArrayList<>();
        for (int i = 0; i < embeddings.size(); i++) {
            double score = cosineSimilarity(query, embeddings.get(i));
            scores.add(new double[]{score, i});
        }
        scores.sort((a, b) -> Double.compare(b[0], a[0]));
        return scores.stream()
                .limit(k)
                .map(s -> chunks.get((int) s[1]))
                .collect(Collectors.toList());
    }

    private double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
