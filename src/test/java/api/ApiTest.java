package api;

import model.Transaction;
import model.TransactionType;
import net.javacrumbs.jsonunit.JsonAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import service.TransactionService;
import spark.SparkBase;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import static junit.framework.TestCase.assertEquals;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

/**
 * Api testing using Mock Spark Jetty.
 * 
 * Created by simone on 13/02/16.
 */
public class ApiTest {

    MockServer mockServer;
    TransactionService transactionService;

    @BeforeClass
    public static void setUpServer() throws Exception {
        SparkBase.setPort(4568);
    }

    @Before
    public void setUp() throws Exception {
        this.transactionService = new TransactionService();
        new Api(this.transactionService);
        mockServer = new MockServer();
    }

    @After
    public void tearDown() {
        mockServer.clear();
    }

    @Test
    public void invalidTransactionPut() throws IOException, ServletException {
        HttpServletResponse response = mockServer.mockRequest("PUT", "/transactionservice/transaction/1/"
                , "{\"amount\": 10.0}");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void invalidJSONTransactionPut() throws IOException, ServletException {
        HttpServletResponse response = mockServer.mockRequest("PUT", "/transactionservice/transaction/1/"
                , "<hi>\"amount\": 10.0</hi>");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void invalidJSONFieldTransactionPut() throws IOException, ServletException {
        HttpServletResponse response = mockServer.mockRequest("PUT", "/transactionservice/transaction/1/"
                , "{\"yo\": 1, \"transactionType\" : \"CARS\", \"amount\": 10.0}");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void invalidTransactionOverflowPut() throws IOException, ServletException {
        HttpServletResponse response = mockServer.mockRequest("PUT", "/transactionservice/transaction/1/"
                , "{\"transactionType\" : \"CARS\"" +
                        ", \"amount\": 1" + Double.MAX_VALUE + "}");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void validTransactionPut() throws IOException, ServletException {
        HttpServletResponse response = mockServer.mockRequest("PUT", "/transactionservice/transaction/1/"
                , "{\"transactionType\" : \"CARS\", \"amount\": 10.0}");
        assertEquals(200, response.getStatus());
        JsonAssert.assertJsonEquals("{\"status\": \"ok\"}", response.getOutputStream().toString());
    }

    @Test
    public void invalidTransactionGet() throws IOException, ServletException {
        HttpServletResponse response = mockServer.mockRequest("GET", "/transactionservice/transaction/1/", "");
        assertEquals(404, response.getStatus());
        assertThatJson(response.getOutputStream().toString())
                .node("status").isEqualTo("nok")
                .node("errors").isArray().ofLength(1);
    }

    @Test
    public void invalidTransactionIdGet() throws IOException, ServletException {
        transactionService.insert(new Transaction(1L, 10.0, null, TransactionType.CARS));
        HttpServletResponse response = mockServer.mockRequest("GET", "/transactionservice/transaction/null/", "");
        assertEquals(400, response.getStatus());
    }


    @Test
    public void validTransactionGet() throws IOException, ServletException {
        transactionService.insert(new Transaction(1L, 10.0, null, TransactionType.CARS));
        HttpServletResponse response = mockServer.mockRequest("GET", "/transactionservice/transaction/1/", "");
        assertEquals(200, response.getStatus());
        JsonAssert.assertJsonEquals("{\"id\": 1, \"parentId\": null, \"amount\": 10.0, \"transactionType\": \"CARS\"}"
                , response.getOutputStream().toString());
    }

    @Test
    public void validSumGet() throws IOException, ServletException {
        transactionService.insert(new Transaction(1L, 10.0, null, TransactionType.CARS));
        transactionService.insert(new Transaction(2L, 10.0, 1L, TransactionType.CARS));
        transactionService.insert(new Transaction(3L, 10.0, 1L, TransactionType.CARS));
        HttpServletResponse response = mockServer.mockRequest("GET", "/transactionservice/sum/1/", "");
        assertEquals(200, response.getStatus());

        assertThatJson(response.getOutputStream().toString())
                .node("amount").isEqualTo(new Double(20.0));
    }

    @Test
    public void validTypeGet() throws IOException, ServletException {
        transactionService.insert(new Transaction(1L, 10.0, null, TransactionType.CARS));
        transactionService.insert(new Transaction(2L, 10.0, null, TransactionType.CARS));
        HttpServletResponse response = mockServer.mockRequest("GET", "/transactionservice/type/cars/", "");
        assertEquals(200, response.getStatus());
        assertThatJson(response.getOutputStream().toString()).isArray().ofLength(2);
    }

    @Test
    public void invalidTransactionSumGet() throws IOException, ServletException {
        HttpServletResponse response = mockServer.mockRequest("GET", "/transactionservice/sum/1/", "");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void invalidTransactionTypeGet() throws IOException, ServletException {
        HttpServletResponse response = mockServer.mockRequest("GET", "/transactionservice/type/boats/", "");
        assertEquals(404, response.getStatus());
    }
}
