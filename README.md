# Account-analyzer

Is  an account statement analyzer of popular, BHD Leon, scotiabank banks. Account-analyzer extract
taxes, commissions, late payments and other deductions.

This project uses [Quarkus](https://quarkus.io/), the Supersonic Subatomic Java Framework.

## Running the application

- Install [java 17+](https://www.oracle.com/java/technologies/downloads/#jdk17-windows)
- Download  https://github.com/franmen05/account-analyzer/blob/master/distribution/account-analyzer-0.3.1.zip
- Uncompressed
- Run startup

### H2 Console
 
For access to h2 in JDBC URL insert:
```
jdbc:h2:./data/aa
```



## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.


-----

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://forthebadge.com)[![forthebadge](https://forthebadge.com/images/badges/built-with-love.svg)](https://forthebadge.com) 

