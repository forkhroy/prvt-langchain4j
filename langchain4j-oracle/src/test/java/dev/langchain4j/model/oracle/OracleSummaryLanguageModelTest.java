package dev.langchain4j.model.oracle;

import dev.langchain4j.model.output.Response;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import static org.assertj.core.api.Assertions.assertThat;

public class OracleSummaryLanguageModelTest {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OracleSummaryLanguageModelTest.class);

    Connection conn;

    @BeforeEach
    void setUp() {
        try {
            conn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@phoenix91729.dev3sub2phx.databasede3phx.oraclevcn.com:1521:rachain", "scott", "tiger");
        } catch (SQLException ex) {
            String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            log.error(message);
        }
    }

    @Test
    @DisplayName("summary with provider=database")
    void testSummaryDatabase() {
        try {
            String pref = "{\"provider\": \"database\", \"gLevel\": \"S\"}";

            OracleSummaryLanguageModel model = new OracleSummaryLanguageModel(conn, pref);

            String filename = "D:\\work\\ddjiang\\GitHub\\langchain_demo\\langchainjs\\data\\sample-3.txt";
            String content = readFile(filename, Charset.forName("UTF-8"));
            Response<String> resp = model.generate(content);
            assertThat(resp.content().length()).isGreaterThan(0);
        } catch (IOException ex) {
            String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            log.error(message);
        }
    }

    @Test
    @DisplayName("summary with provider=OCIGenAI")
    void testSummaryOcigenai() {
        try {
            String pref = "{\n"
                    + "  \"provider\": \"ocigenai\",\n"
                    + "  \"credential_name\": \"OCI_CRED\",\n"
                    + "  \"url\": \"https://inference.generativeai.us-chicago-1.oci.oraclecloud.com/20231130/actions/summarizeText\",\n"
                    + "  \"model\": \"cohere.command\",\n"
                    + "}";
            String proxy = "www-proxy-ash7.us.oracle.com:80";

            OracleSummaryLanguageModel model = new OracleSummaryLanguageModel(conn, pref, proxy);

            String filename = "D:\\work\\ddjiang\\GitHub\\langchain_demo\\langchainjs\\data\\sample-3.txt";
            String content = readFile(filename, Charset.forName("UTF-8"));
            Response<String> resp = model.generate(content);
            assertThat(resp.content().length()).isGreaterThan(0);
        } catch (IOException ex) {
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
