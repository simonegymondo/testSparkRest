Spark API Testing
===========

This is a simple API server in Java Spark. It includes a simple Spring integrated service. Which stores
data in memory. It also includes unit and API testing.

    ...
    mvn clean install // to compile and test
    mvn exec:java     // to run the server on localhost:4567


API
-------

The API's base URL is /transactionservice and it contains the following endpoints:

**"/transactionservice/transaction/{:id}"**: it returns a transaction of the given ID.
If the ID does not exists it returns 404.
