Spark API Testing
===========

This is a simple API server in Java Spark and it currently runs
on OpenJDK8. It is not compatible with OracleJDK8. It includes a simple Spring integrated service which stores
data in memory. The service is thread safe and is limited in space by the amount of memory allocated for 
the running process. It also includes unit and API testing.

    mvn clean install // to compile and test
    mvn exec:java     // to run the server on localhost:4567

Service
-------

The service provide a basic interface for transactions. A transaction is defined as

    {
      id: Long,
      parentId: Long or null,
      transctionType: any of the following [SHOPPING, CARS, AUDIO, VIDEO, GENERIC],
      amount: Double
    }

API
-------

The API's base URL is /transactionservice and it contains the following endpoints:

##### GET "/transactionservice/transaction/{:id}"

It returns a transaction of the given ID.
If the ID does not exists it returns 404.

##### PUT "/transactionservice/transaction/{:id}"
It adds a new transaction with id {:id} as described from the body. 
The body accepts JSON format only. If the transaction already exists it is replaced with the new transaction.

Example of body:

    {
      amount: 10.0, 
      parentId: 1,
      transactionType: CARS
    }

The result will be in case everything is ok:

    { status: ok }

##### GET "/transactionservice/sum/{:id}"
It returns the sum of the transaction which have {:id} as parent id.
If the transaction of given id does not exists it returns 404.
If no transaction links to the {:id} it returns 0.
The result will be in JSON format as for example:

    { amount: 10.0 }

##### GET "/transactionservice/type/{:typeId}"
It returns a list of ids of the given type. It returns 404 if the type 
does not exists. An example of return value is:

    [1, 2]

Error Handling
-------

In case of errors, along with the return type you get a list of error messages:

Example of insertion with bad data:

    {
      amount: 10.0, 
      parentId: 1,
    }

You will receive:

    {
      status: "nok", 
      errors: ["transactionType cannot be null"]
    }

Complexity
-------

All endpoints return the result synchronously, in constant time O(1) if we do not take into cosideration the time needed for serialization.

Notes
-------
This project uses Java Spark framework for creating a simple REST API. It is quite minimalistic. It also integrates a simple Spring context. Testing is performed with JUnit and Mockito. API testing is done also in JUnit using a mocked Jetty server. 
