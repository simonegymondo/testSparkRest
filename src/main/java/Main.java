import api.Api;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import service.TransactionService;

import static spark.SparkBase.threadPool;

@Configuration
@ComponentScan({ "service", "api" })
public class Main {

    public static void main(String[] args) {
        int maxThreads = 8;
        int minThreads = 2;
        int timeOutMillis = 30000;

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Main.class);
        threadPool(maxThreads, minThreads, timeOutMillis);
        new Api(ctx.getBean(TransactionService.class));
        ctx.registerShutdownHook();
    }
}