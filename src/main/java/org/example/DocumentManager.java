package org.example;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    private final Map<String, Document> storage = new HashMap<>();

    public Document save(Document document) {
        prepareDocumentForSave(document);
        storage.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage.values().stream()
                .filter(request.toPredicate())
                .toList();
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    private void prepareDocumentForSave(Document document) {
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
            document.setCreated(Instant.now());
        }
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;

        public Predicate<Document> toPredicate() {
            return createTitleFilter()
                    .and(createContentFilter())
                    .and(createAuthorFilter())
                    .and(createdFromFilter())
                    .and(createdToFilter());
        }

        private Predicate<Document> createTitleFilter() {
            return doc -> titlePrefixes == null || titlePrefixes.stream()
                    .anyMatch(doc.getTitle()::startsWith);
        }

        private Predicate<Document> createContentFilter() {
            return doc -> containsContents == null || containsContents.stream()
                    .anyMatch(doc.getContent()::contains);
        }

        private Predicate<Document> createAuthorFilter() {
            return doc -> authorIds == null || authorIds.stream()
                    .anyMatch(doc.getAuthor().getId()::equals);
        }

        private Predicate<Document> createdFromFilter() {
            return doc -> createdFrom == null || doc.getCreated().isAfter(createdFrom);
        }

        private Predicate<Document> createdToFilter() {
            return doc -> createdTo == null || doc.getCreated().isBefore(createdTo);
        }
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
