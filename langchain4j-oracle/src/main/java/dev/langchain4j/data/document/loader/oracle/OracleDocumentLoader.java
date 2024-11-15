package dev.langchain4j.data.document.loader.oracle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleDocumentLoader {

    private static final Logger log = LoggerFactory.getLogger(OracleDocumentLoader.class);

    private final Connection conn;

    public OracleDocumentLoader(Connection conn) {
        this.conn = conn;
    }

    public List<Document> loadDocuments(String pref) {
        List<Document> documents = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(pref);
            JsonNode fileNode = rootNode.path("file");
            JsonNode dirNode = rootNode.path("dir");
            JsonNode ownerNode = rootNode.path("owner");
            JsonNode tableNode = rootNode.path("tablename");
            JsonNode colNode = rootNode.path("colname");

            if (fileNode.textValue() != null) {
                String filename = fileNode.textValue();
                Document doc = loadDocument(filename, pref);
                if (doc != null) {
                    documents.add(doc);
                }
            } else if (dirNode.textValue() != null) {
                String dir = dirNode.textValue();
                Path root = Paths.get(dir);
                Files.walk(root).forEach(path -> {
                    if (path.toFile().isFile()) {
                        Document doc = loadDocument(path.toFile().toString(), pref);
                        if (doc != null) {
                            documents.add(doc);
                        }
                    }
                });
            } else if (colNode.textValue() != null) {
                String column = colNode.textValue();

                String table = tableNode.textValue();
                String owner = ownerNode.textValue();
                if (table == null) {
                    throw new RuntimeException("Missing table in preference");
                }
                if (owner == null) {
                    throw new RuntimeException("Missing owner in preference");
                }

                documents.addAll(loadDocuments(owner, table, column, pref));
            } else {
                throw new RuntimeException("Invalid preference");
            }
        } catch (IOException | RuntimeException ex) {
            String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            log.warn("Failed to load '{}': {}", pref, message);
        }
        return documents;
    }

    private Document loadDocument(String filename, String pref) {
        Document document = null;

        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filename));

            String query = "select dbms_vector_chain.utl_to_text(?, json(?)) text, dbms_vector_chain.utl_to_text(?, json('{\"plaintext\": \"false\"}')) metadata from dual";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                Blob blob = conn.createBlob();
                blob.setBytes(1, bytes);

                stmt.setBlob(1, blob);
                stmt.setObject(2, pref);
                stmt.setBlob(3, blob);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String text = rs.getString("text");
                        String html = rs.getString("metadata");

                        Metadata metadata = getMetadata(html);
                        Path path = Paths.get(filename);
                        metadata.put(Document.FILE_NAME, path.getFileName().toString());
                        metadata.put(Document.ABSOLUTE_DIRECTORY_PATH, path.getParent().toString());
                        document = new Document(text, metadata);
                    }
                }
            }
        } catch (IOException | SQLException e) {
            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            log.warn("Failed to load '{}': {}", filename, message);
        }

        return document;
    }

    private List<Document> loadDocuments(String owner, String table, String column, String pref) {
        List<Document> documents = new ArrayList<>();

        String query = String.format("select dbms_vector_chain.utl_to_text(t.%s, json(?)) text, dbms_vector_chain.utl_to_text(t.%s, json('{\"plaintext\": \"false\"}')) metadata from %s.%s t",
                column, column, owner, table);
        try {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setObject(1, pref);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String text = rs.getString("text");
                        String html = rs.getString("metadata");

                        Metadata metadata = getMetadata(html);
                        Document doc = new Document(text, metadata);
                        documents.add(doc);
                    }
                }
            }
        } catch (SQLException e) {
            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            log.warn("Failed to load '{}': {}", column, message);
        }

        return documents;
    }

    private static Metadata getMetadata(String html) {
        Metadata metadata = new Metadata();

        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        Elements metaTags = doc.getElementsByTag("meta");
        for (Element metaTag : metaTags) {
            String name = metaTag.attr("name");
            if (name.isEmpty()) {
                continue;
            }
            String content = metaTag.attr("content");
            metadata.put(name, content);
        }

        return metadata;
    }
}
