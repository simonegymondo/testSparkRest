package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import model.Transaction;
import model.TransactionType;

import org.eclipse.jetty.server.Response;
import service.TransactionService;
import service.exceptions.InvalidTransactionException;
import service.exceptions.TransactionNotFoundException;
import service.exceptions.TransactionTypeNotFoundException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static spark.Spark.*;

/**
 * Rest API endpoints
 * Created by simone on 13/02/16.
 */

public class Api {

    private static final String TRANSACTION_SERVICE_ENDPOINT = "/transactionservice";
    private static final String TRANSACTION_ENDPOINT = "/transaction";
    private static final String SUM_ENDPOINT = "/sum";
    private static final String TYPE_ENDPOINT = "/type";

    TransactionService transactionService;

    public Api(TransactionService transactionService) {
        this.transactionService = transactionService;
        configRoutes();
    }

    /**
     * Jackson serializer
     * @param data
     * @return
     */
    private static String dataToJson(Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, data);
            return sw.toString();
        } catch (IOException e){
            throw new RuntimeException("IOException from a StringWriter?");
        }
    }


    private void configRoutes() {
        /**
         * Returns a {@link Transaction} Object of the desired Id in JSON format
         */
        get(TRANSACTION_SERVICE_ENDPOINT + TRANSACTION_ENDPOINT + "/:id/",
                (req, res) -> dataToJson(transactionService.get(Long.valueOf(req.params(":id")))));
        /**
         * Stores a new {@link Transaction} Object
         */
        put(TRANSACTION_SERVICE_ENDPOINT + TRANSACTION_ENDPOINT + "/:id/",
                (req, res) -> {
                    ObjectMapper mapper = new ObjectMapper();
                    final Transaction creation = mapper.readValue(req.body(), Transaction.class);

                    if (creation.getAmount().isInfinite()) {
                        throw new InvalidTransactionException("Amount is too big");
                    }
                    final Long transactionId = Long.valueOf(req.params(":id"));
                    creation.setId(transactionId);
                    transactionService.insert(creation);
                    return dataToJson(Collections.unmodifiableMap(Stream.of(
                            new AbstractMap.SimpleEntry<>("status", "ok"))
                            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()))));
                });
        /**
         * Returns the sum of {@link Transaction} with same parentId
         */
        get(TRANSACTION_SERVICE_ENDPOINT + SUM_ENDPOINT + "/:id/",
                (req, res) -> {
                    final Long transactionId = Long.valueOf(req.params(":id"));
                    return dataToJson(Collections.unmodifiableMap(Stream.of(
                            new AbstractMap.SimpleEntry<>("amount",
                                    transactionService.getChildrenSum(transactionId)))
                            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()))));
                });
        /**
         * Returns a list of  {@link Transaction} with same {@link TransactionType}
         */
        get(TRANSACTION_SERVICE_ENDPOINT + TYPE_ENDPOINT + "/:typeId/",
                (req, res) -> {
                    try {
                        return dataToJson(transactionService.getByType(TransactionType.valueOf(req.params(":typeId").toUpperCase())));
                    } catch (IllegalArgumentException e) {
                        throw new TransactionTypeNotFoundException("Cannot find transaction type: " + req.params(":typeId"));
                    }
                });

        /**
         * Exception mapping
         */
        exception(InvalidFormatException.class, (e, request, response) -> {
            response.status(Response.SC_BAD_REQUEST);
            response.body(handleErrorData(Arrays.asList(e.getMessage())));
        });

        /**
         * Exception mapping
         */
        exception(TransactionNotFoundException.class, (e, request, response) -> {
            response.status(Response.SC_NOT_FOUND);
            response.body(handleErrorData(Arrays.asList(e.getMessage())));
        });

        /**
         * Exception mapping
         */
        exception(NumberFormatException.class, (e, request, response) -> {
            response.status(Response.SC_BAD_REQUEST);
            response.body(handleErrorData(Arrays.asList(e.getMessage())));
        });

        /**
         * Exception mapping
         */
        exception(TransactionTypeNotFoundException.class, (e, request, response) -> {
            response.status(Response.SC_NOT_FOUND);
            response.body(handleErrorData(Arrays.asList(e.getMessage())));
        });

        /**
         * Exception mapping. Here we need to extract
         */
        exception(InvalidTransactionException.class, (e, request, response) -> {
            response.status(Response.SC_BAD_REQUEST);
            List<String> errors = new ArrayList<>();
            errors = ((InvalidTransactionException)e)
                    .getErrors()
                    .stream()
                    .map(c -> c.getPropertyPath() + " " + c.getMessage())
                    .collect(Collectors.toList());
            if (e.getMessage() != null) {
                errors.add(e.getMessage());
            }
            response.body(handleErrorData(errors));
        });

        /**
         * Configure the response type.
         */
        before((req, res) -> {
            res.type("application/json");
        });
    }

    private String handleErrorData(List<String> errors) {
        return dataToJson(Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>("status", "nok"),
                new AbstractMap.SimpleEntry<>("errors", errors))
                .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()))));
    }
}
