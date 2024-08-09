import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc.
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private static long id = 0;

    private static String createId(){
        return String.valueOf(id++);
    }
    private final Map<String, Document> storage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(createId());
            document.setCreated(Instant.now());
        } else {
            Document alreadyCreated = storage.get(document.getId());
            if (alreadyCreated != null) {
                document.setCreated(alreadyCreated.getCreated());
            }
        }
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
        List<Document> documents = new ArrayList<>();
        for (Document document : storage.values()) {
            if (matchesSearchRequest(document, request)) {
                documents.add(document);
            }
        }
        return documents;
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

    private boolean matchesSearchRequest(Document document, SearchRequest request) {
        // Check title prefixes
        if (request.getTitlePrefixes() != null && !request.getTitlePrefixes().isEmpty()) {
            boolean matches = false;
            for (String prefix : request.getTitlePrefixes()) {
                if (document.getTitle().startsWith(prefix)) {
                    matches = true;
                    break;
                }
            }
            if (!matches) return false;
        }

        // Check content contains
        if (request.getContainsContents() != null && !request.getContainsContents().isEmpty()) {
            boolean matches = false;
            for (String content : request.getContainsContents()) {
                if (document.getContent().contains(content)) {
                    matches = true;
                    break;
                }
            }
            if (!matches) return false;
        }

        // Check author IDs
        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            if (!request.getAuthorIds().contains(document.getAuthor().getId())) {
                return false;
            }
        }

        // Check created date range
        if (request.getCreatedFrom() != null) {
            if (document.getCreated().isBefore(request.getCreatedFrom())) {
                return false;
            }
        }

        if (request.getCreatedTo() != null) {
            if (document.getCreated().isAfter(request.getCreatedTo())) {
                return false;
            }
        }

        return true;
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
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