
# Rule Engine with Abstract Syntax Tree

A rule engine application that uses Abstract Syntax Tree (AST) to create, evaluate, and manage complex business rules. The system supports dynamic rule creation, combination, and modification with a focus on user attribute evaluation.

---

### Deployment

The application has been deployed with the following technologies and services:

1. **Backend (Spring Boot):**
   - Deployed to **Docker Hub** as a containerized application.
   - [Docker Hub Repository URL](sandesh030/rule-engine-deployment)

2. **Backend Docker Image:**
   - Docker image deployed to **Render.com** for hosting the backend services.
   - [Render Deployment URL](https://rule-engine-deployment-latest.onrender.com)

3. **Database:**
   - PostgreSQL database deployed on **Neon Postgres Cloud Database** for efficient and scalable data management.
   - [Neon Postgres Cloud URL](https://console.neon.tech/app/projects/solitary-forest-16973334/branches/br-spring-butterfly-a8e2xdam/tables)

4. **Frontend:**
   - React application deployed to **Netlify** for seamless frontend hosting.
   - [Frontend Deployment URL](https://rule-engine-bd7555.netlify.app/)

---

## 🚀 Features

- Create complex rules using logical operators (AND/OR)
- Evaluate user data against defined rules
- Combine multiple rules into a single rule
- Modify existing rules
- Visualize rules as Abstract Syntax Trees
- RESTful API for rule management
- React-based UI for rule visualization and management

## 🛠 Technology Stack

### Backend
- Java 17
- Spring Boot 3.1.0
- PostgreSQL 15
- JUnit 5
- Mockito
- Spring Data JPA

### Frontend
- React 18
- TypeScript
- Tailwind CSS
- Axios
- React Query
- React Flow (for AST visualization)

## 📋 Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- PostgreSQL 15
- Maven 3.8+

## 🏗 Project Structure

```
rule-engine/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ruleengine/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── RuleController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── RuleServices.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── Node.java
│   │   │   │   │   ├── Rule.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── RuleRepository.java
│   │   │   │   └── exception/
│   │   │   │       ├── GlobalHandlerException.java
│   │   │   │       ├── CustomException.java
│   │   │   └── resources/
│   │   │        ├── application.properties
│   │   └── test/
│   │       ├── RuleServiceTest.java
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │         ├── CombineRule.js
│   │         ├── CreateRule.js
│   │         ├── EvaluateRule.js
│   │         ├── Home.js
│   │         ├── ModifyRule.js
│   │         ├── Navbar.js
│   │  
│   ├── package.json
│   └── tsconfig.json
└── README.md
```

## 🔧 Installation & Setup

### Backend Setup

1. Clone the repository:
```bash
git clone https://github.com/sandesh300/Rule-Engine-Application.git
cd rule-engine
```

2. Configure PostgreSQL:
```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/rule_engine
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database=postgresql
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.show-sql=false
```

3. Build and run the application:
```bash
mvn clean install
mvn spring-boot:run
```

### Frontend Setup

1. Navigate to the frontend directory:
```bash
cd rule-engine-ui
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm start
```

## 🎯 API Endpoints

### Rule Management

```
POST   /api/rules/create          - Create a new rule
GET    /api/rules/getRules        - Get all rules
PUT    /api/rules/modify          - Modify existing rule
DELETE /api/rules/delete          - Delete a rule
POST   /api/rules/evaluate        - Evaluate data against a rule
POST   /api/rules/combine         - Combine multiple rules
```

### Backend REST APIs Documentation Link
[Backend REST APIs Documentation](https://walnut-wrist-9da.notion.site/Rule-Engine-with-AST-129a272bc3e580c0ac0cfc7f01e5c221)

## 🎨 Design Choices

### Abstract Syntax Tree (AST)
- Nodes represent operators (AND/OR) and operands (conditions)
- Binary tree structure for efficient rule evaluation
- Node structure:
  ```java
  class Node {
      String type;        // "operator" or "operand"
      Node left;          // Left child
      Node right;         // Right child
      String value;       // Operator or condition value
  }
  ```

### Database Schema
```sql
CREATE TABLE rules (
    id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(255) NOT NULL,
    root_node JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Rule Combination Strategy
- AND operator for combining multiple rules
- Preserves individual rule integrity
- Optimizes for evaluation performance

## 🧪 Testing

### Test Coverage
- Unit tests for services and controllers
- Integration tests for API endpoints
- Frontend component tests
- End-to-end testing for critical flows

### Test Cases Documentation Link
[Test Cases Documentation](https://walnut-wrist-9da.notion.site/Rule-Engine-using-AST-Test-Cases-129a272bc3e580a082e9c45cc99ccc51)

### Running Tests

Backend:
```bash
cd backend
mvn test
```

Frontend:
```bash
cd frontend
npm test
```

## ⚠️ Validation & Error Handling

### Rule String Validation
- Syntax validation for rule strings
- Parentheses balance checking
- Operator validation
- Data type compatibility checks

### User Data Validation
- Required field validation
- Data type validation
- Value range validation
- Department catalog validation

### Error Response Format
```json
{
    "error": "Error message",
    "details": "Additional error details",
    "timestamp": "2024-10-24T10:00:00Z"
}
```

### Validation Error Handling Link
[Validation Error Handling](https://walnut-wrist-9da.notion.site/Rule-Engine-using-AST-Validation-Error-Handling-129a272bc3e580bb9a1acd849dee8eb7)

## 📦 Dependencies

### Backend Dependencies
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.11.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.10.3</version> <!-- Or any latest stable version -->
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Frontend Dependencies
```json
{
    "dependencies": {
        "react": "^18.2.0",
        "react-dom": "^18.2.0",
        "axios": "^1.6.0",
        "react-query": "^3.39.3",
        "reactflow": "^11.10.1",
        "tailwindcss": "^3.3.5"
    }
}
```

---

