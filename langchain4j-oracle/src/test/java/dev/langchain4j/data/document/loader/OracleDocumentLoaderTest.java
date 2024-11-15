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

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

public class OracleDocumentLoaderTest {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OracleDocumentLoaderTest.class);

    Dotenv dotenv;
    OracleDocumentLoader loader;

    @BeforeEach
    void setUp() {
        dotenv = Dotenv.configure().load();

        try {
            Connection conn = DriverManager.getConnection(
                    dotenv.get("ORACLE_JDBC_URL"), dotenv.get("ORACLE_JDBC_USER"), dotenv.get("ORACLE_JDBC_PASSWORD"));
            loader = new OracleDocumentLoader(conn);
        } catch (SQLException ex) {
            String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            log.error(message);
        }
    }

    @Test
    @DisplayName("load from file")
    void testFile() {
        String pref = "{\"file\": \"" + dotenv.get("DEMO_DS_PDF_FILE") + "\"}";
        List<Document> docs = loader.loadDocuments(pref);
        assertThat(docs.size()).isEqualTo(1);
        for (Document doc : docs) {
            assertThat(doc.text().length()).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("load from dir")
    void testDir() {
        String pref = "{\"dir\": \"" + dotenv.get("DEMO_DS_DIR") + "\"}";
        List<Document> docs = loader.loadDocuments(pref);
        assertThat(docs.size()).isGreaterThan(1);
        for (Document doc : docs) {
            assertThat(doc.text().length()).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("load from table")
    void testTable() {
        String pref = "{\"owner\": \"" + dotenv.get("DEMO_DS_OWNER") + "\", \"tablename\": \"" + dotenv.get("DEMO_DS_TABLE") + "\", \"colname\": \"" + dotenv.get("DEMO_DS_COLUMN") + "\"}";
        List<Document> docs = loader.loadDocuments(pref);
        assertThat(docs.size()).isGreaterThan(1);
        for (Document doc : docs) {
            assertThat(doc.text().length()).isGreaterThan(0);
        }
    }

}
