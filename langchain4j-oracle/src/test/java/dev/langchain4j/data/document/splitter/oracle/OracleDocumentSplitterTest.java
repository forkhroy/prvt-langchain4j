package dev.langchain4j.data.document.splitter.oracle;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import static org.assertj.core.api.Assertions.assertThat;

public class OracleDocumentSplitterTest {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OracleDocumentSplitterTest.class);

    OracleDocumentSplitter splitter;

    @Test
    @DisplayName("chunk by chars")
    void testByChars() {
        String pref = "{\"by\": \"chars\", \"max\": 50}";
        String filename = "D:\\work\\ddjiang\\GitHub\\langchain_demo\\langchainjs\\data\\sample-3.txt";

        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@phoenix91729.dev3sub2phx.databasede3phx.oraclevcn.com:1521:rachain", "scott", "tiger");
            splitter = new OracleDocumentSplitter(conn, pref);

            String content = readFile(filename, Charset.forName("UTF-8"));
            String[] chunks = splitter.split(content);
            assertThat(chunks.length).isGreaterThan(1);
        } catch (IOException | SQLException ex) {
            String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            log.error(message);
        }
    }

    @Test
    @DisplayName("chunk by words")
    void testByWords() {
        String pref = "{\"by\": \"words\", \"max\": 50}";
        String filename = "D:\\work\\ddjiang\\GitHub\\langchain_demo\\langchainjs\\data\\sample-3.txt";

        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@phoenix91729.dev3sub2phx.databasede3phx.oraclevcn.com:1521:rachain", "scott", "tiger");
            splitter = new OracleDocumentSplitter(conn, pref);

            String content = readFile(filename, Charset.forName("UTF-8"));
            String[] chunks = splitter.split(content);
            assertThat(chunks.length).isGreaterThan(1);
        } catch (IOException | SQLException ex) {
            String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            log.error(message);
        }
    }

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes, encoding);
    }
}
