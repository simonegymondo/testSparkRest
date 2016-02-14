package api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import model.Transaction;
import model.TransactionType;
import net.javacrumbs.jsonunit.JsonAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.TransactionService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

/**
 * Created by simone on 13/02/16.
 */
public class ApiTest {

    MockServer mockServer;
    TransactionService transactionService;

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
        HttpServletResponse response = mockServer.mockRequest("PUT", "/transactionservice/transaction/1/", "{\"amount\": 10.0}");
        assertEquals(400, response.getStatus());

    }

    @Test
    public void validTransactionPut() throws IOException, ServletException {
        HttpServletResponse response = mockServer.mockRequest("PUT", "/transactionservice/transaction/1/", "{\"transactionType\" : \"CARS\", \"amount\": 10.0}");
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
        HttpServletResponse response = mockServer.mockRequest("GET", "/transactionservice/sum/2/", "");
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