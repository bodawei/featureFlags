## Setup
- You must install `direnv` if you want to make use of the `.envrc` file.
- You must install the claude code app ( https://github.com/apps/claude ) in github if you wish to use the github actions
- - you must add a secret, ANTHROPIOC_API_KEY, to the github repo configuration.
  
### Build

```
mvn package
```

Compiles and produces a fat JAR at target/feature-flags-0.1.0-SNAPSHOT.jar.

### Run

Option A — Maven plugin (easier during development):

```
mvn spring-boot:run
```
Option B — run the JAR directly:

```
java -jar target/feature-flags-0.1.0-SNAPSHOT.jar
```
The server starts on port 8080 by default.



### OR
```
mvn clean package
java -jar target/feature-flags-0.1.0-SNAPSHOT.jar
```
