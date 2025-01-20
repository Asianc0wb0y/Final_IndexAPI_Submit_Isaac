# Index Management API

## Overview

This project implements a RESTful API for creating and managing financial indices, and simulating real-time operations like adding shares, removing shares, and applying dividends. The application is built using Java with Spring Boot and is designed to handle concurrent requests while ensuring data consistency.

## How to Run the Project

### Prerequisites
•	Java 17+
•	Maven 3.8+
•	IDE (optional): IntelliJ IDEA or Eclipse

### Steps to Run

1.	Clone the repository:

` git clone  https://github.com/Asianc0wb0y/Final_IndexAPI_Submit_Isaac.git`

` cd <repository_directory> `


2.	Build the project using Maven:
   `Mvn clean install`

3.	Run the application

   `mvn spring-boot:run`

4.	The application will start on `http://localhost:8080.`
   
5.	Endpoints

| API | Method | Description |
| -------- | -------- | -------- |
| /api/create	  | POST| Create a new index. |
| /api/indexAdjustment | POST | Adjust an index (add/delete shares, apply dividend). |
| /api/indexState	 | GET | Retrieve the state of all indices. |
| /api/indexState/{indexName}	 | GET | Retrieve the state of a specific index. |

    
6.	Example cURL commands for testing are provided in the appendix below

### Alternatively, one can also run it using the .jar file

  ` Mvn clean package`

Run the Jar with Java 17 or above
`java -jar with the name of the jar file`

There is a jar file in the release just in case you want to test run it directly.

** Potential bug with IntellJ

   Sometimes, IntellJ can have problem with Lombok, make sure you use the Annotation path with the setting “Obtain processors from project classpath”, otherwise there would be issue in building the project.

# Architect

   + The engineering principle is to separate API, business logic, and data layers. DTOs (e.g. IndexDTO, ShareDTO) handle API requests and responses, ensuring input validation. 

   + Entities (IndexEntity, ShareEntity) represent in-memory data used in the business logic layer. IndexMapper bridges DTOs and entities, handling data transformations. 

   + Core operations like adding shares, applying dividends, or retrieving index states are implemented in the IndexService, which ensures thread safety using ReentrantLock. 

   + The controller layer exposes RESTful endpoints for seamless interaction with the system.


# Design Highlights

This project was developed with the following key design principles:

### 1.	Use of Data Transfer Object (DTO) pattern

+ Clear separation between API layer and business logic layer
+ Future changes of JSON fields are easier to handle
+ Use of the Mapper Interface (MapStruct)
+ The IndexMapper transforms API requests (eg. IndexDTO, ShareDTO) into internal models (IndexEntity, ShareEntity) and vice versa.
+ using a mapper ensures that the logic for data transformation is centralized, making the codebase easier to maintain and extend.
+ Use of MapStruct as seen in the interface IndexMapper for converting between DTOs and Entities with transformation logic at compile time for high performance and simplicity.

### 2. Leverages Spring’s Dependency Injection to promote loose coupling and maintainability. 

+ Dependencies like IndexService and IndexMapper are injected into classes like IndexController via constructor-based injection. Such modular design is used to reduce boilerplate code and improve scalability.

### 3.	Multi-Layer Validation Approach

 + Annotations (e.g. @NotBlank, @Positive) for the consumer layer to validate API request inputs directly in the DTO layer as a declarative and centralized way to enforce rules to detect invalid input early on
+ Custom Exception Handling for service layer (business logic) validation to accommodate for more complex, domain-specific validations (e.g., ensuring indices have at least two members or dividends don’t exceed share prices).
+ Following industry standard for multi-layer validation

### 4.	Thread Safety and Concurrency

+ Fine-Grained Locking: Used ReentrantLock to ensure that operations are thread-safe and to reduce race occurrence.
+ The use of per-index lock for all (except Dividen operations), instead of locking the whole memory structure with all indices is to increase the performance with multiple requests at the same time.
+ Deadlock Prevention: For the dividend operations that need access to the whole Index Entity storage in memory, deadlocks could happen if multiple dividend requests are made. Therefore the locks are acquired in a consistent order (e.g., alphabetically by index name) to avoid deadlock

  + Why ReentrantLock?
     - Offers greater flexibility than synchronized, and more fine-grained control.
     - Allows per-index locking, enabling parallel operations on different indices, and improving scalability.

### 5.	Testing
  + UnitTest, integration test, and basic concurrency test have been implemented for testing.
  + Use of Mockmvc to simulate HTTP requests and responses

# Assumptions

1.	Everything that is specified in the code challenge PDF including validations

2.	Input assumption
    + Only one operation per each API request
    + For POST /indexAdjustment
        - Only one operation at a time per API request
        - For each operation, only one index is allowed at a time for Adding and deleting share (as seen in your Integration Test example requirement pdf)
        - For Dividend operation, it will apply to all indices that are related to the share
        - or the Share Deletion operation, it is not clear in the requirement if a share can be deleted from an index if there are only 2 members left after deletion, so it is assumed to be prohibited in my implementation to be on the safe side.
    + For POST /create
        - Only one index is to be created in each request
  	
3.	In-memory Data (Entity)
    + Data is stored in ConcurrentHashMap for simplicity and thread Safety
    + No persistent storage is used as per the coding challenge requirement

4.	Project was developed and tested with Azul Zulu 17.0.13 JDK on  aarch64 architecture on Apple Silicon

# What would I improve if I had more time?

  + Write better ReadMe with format and layout. I am absolutely confident in my presentation skills, however, with the given time limit, I made the choice to focus on the coding and show more about my technical ability rather than on documentation.
  + Use of custom validators so that validation can be separated from the of business logic implementation.
  + Some of the concurrency implementation should also focus on the financial / business aspect to find the optimal solution. For instance, S&P 500 index is rebalanced quarterly unless some special incidents such as companies being merged or having bankruptces, therefore the concurrency implementation should not just focus on the technical aspect, but the real world financial situation.
  + Deeper analysis on each operation to see if one can use an atomic method such as computerIfAbsent() to further improve the performance due to multiple real-time requests.
  + More comprehensive test with boundary and edge cases to include extreme values as input, and more versatile performance test on concurrency
  + JavaDoc to be written for better documentation
  + Overall, given the relatively substantial workload on an assignment for a job application, I tried to find a balance between code quality and showing you my various skills in development such as implementing the optional part and the use of Enterprise application development practise whenever possible

# Feedback

  + The challenge is quite interesting and I like that it’s related to real-world scenario, not just some pure abstract technical challenge
  + This challenge also gave me a better insight into the type of projects I could work in your company. 
  + I can see the challenge was created by someone with lots of experience and knowledge in enterprise applications, and a passion in coding.

Thank you for taking your time in considering my application.

# Appendix – CURL examples for the API

1. Create an Index

```
curl -i -X POST http://localhost:8080/api/create \
-H "Content-Type: application/json" \
-d '{
  "indexName": "INDEX_1",
  "indexMembers": [
    { "shareName": "AAPL.OQ", "sharePrice": 150.0, "numberOfShares": 10.0 },
    { "shareName": "META.OQ", "sharePrice": 200.0, "numberOfShares": 5.0 }
  ]
}'
```

2. Index Adjustment

2a Add a Share
```
curl -i -X POST http://localhost:8080/api/indexAdjustment \
-H "Content-Type: application/json" \
-d '{
  "additionOperation": {
    "shareName": "TSLA.OQ",
    "sharePrice": 300.0,
    "numberOfShares": 8.0,
    "indexName": "INDEX_1"
  }
}'
```
2b Delete a Share
```
curl -i -X POST http://localhost:8080/api/indexAdjustment \
-H "Content-Type: application/json" \
-d '{
  "deletionOperation": {
    "shareName": "META.OQ",
    "indexName": "INDEX_1"
  }
}'
```

2c Apply a Dividend
```
curl -i -X POST http://localhost:8080/api/indexAdjustment \
-H "Content-Type: application/json" \
-d '{
  "dividendOperation": {
    "shareName": "AAPL.OQ",
    "dividend": 10.0
  }
}'
```
3. Get the State of All Indices
```
curl -i -X GET http://localhost:8080/api/indexState \
-H "Content-Type: application/json"
```

4. Get the State of one particular index
```
curl -i -X GET http://localhost:8080/api/indexState/INDEX_1 \
-H "Content-Type: application/json"
```






