#### How to run:

```
mvn clean install package
java -jar target/server-runnable.jar # start server
java -jar target/client-runnable.jar # start 1st client
java -jar target/client-runnable.jar # start 2nd client
```

Optionally server accepts port number:

```
java -jar target/server-runnable.jar 5000
```

Client accepts server url:

```
java -jar target/client-runnable.jar "ws://localhost:5000"
```
