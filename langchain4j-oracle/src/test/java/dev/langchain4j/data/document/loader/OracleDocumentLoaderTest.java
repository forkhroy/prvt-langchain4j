package dev.langchain4j.data.document.loader;

import dev.langchain4j.data.document.loader.oracle.OracleDocumentLoader;
import dev.langchain4j.data.document.Document;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.slf4j.LoggerFactory;

public class OracleDocumentLoaderTest {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OracleDocumentLoaderTest.class);

    OracleDocumentLoader loader;

    @BeforeEach
    void setUp() {
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@phoenix91729.dev3sub2phx.databasede3phx.oraclevcn.com:1521:rachain", "scott", "tiger");
            loader = new OracleDocumentLoader(conn);
        } catch (SQLException ex) {
            String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            log.error(message);
        }
    }

    @Test
    @DisplayName("load from file")
    void testFile() {
        String pref = "{\"file\": \"D:/work/ddjiang/GitHub/langchain_demo/langchainjs/data/sample-1.pdf\"}";
        List<Document> docs = loader.loadDocuments(pref);
        assertThat(docs.size()).isEqualTo(1);
        for (Document doc : docs) {
            assertThat(doc.text().length()).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("load from dir")
    void testDir() {
        String pref = "{\"dir\": \"D:/work/ddjiang/GitHub/langchain_demo/langchainjs/data\"}";
        List<Document> docs = loader.loadDocuments(pref);
        assertThat(docs.size()).isGreaterThan(1);
        for (Document doc : docs) {
            assertThat(doc.text().length()).isGreaterThan(0);
        }
    }
    
    @Test
    @DisplayName("load from table")
    void testTable() {
        String pref = "{\"owner\": \"scott\", \"tablename\": \"docs\", \"colname\": \"text\"}";
        List<Document> docs = loader.loadDocuments(pref);
        assertThat(docs.size()).isGreaterThan(1);
        for (Document doc : docs) {
            assertThat(doc.text().length()).isGreaterThan(0);
        }
    }
    
}
