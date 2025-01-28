# Rental Car API

## Description
Rental Car API is a backend application built with **Spring Boot** to manage car rental services. It uses a RESTful architecture, integrating with client applications like mobile or web. The application includes user authentication, car management, and rental operations.

---

## Features
- **Authentication & Authorization**: Secure access using **JWT (JSON Web Token)** and **Spring Security**.
- **User Management**: CRUD operations for customers and admins.
- **Car Management**: Add, update, delete, and view available cars for rent.
- **Rental System**: Rent and return cars, and check their availability.
- **Database Integration**: Uses **PostgreSQL** for persistent storage.
- **Data Validation**: Ensures valid input through Spring's validation annotations.

---

## Technologies Used
- **Spring Boot**
- **Spring Security with JWT**
- **PostgreSQL**
- **Hibernate/JPA**
- **Maven/Gradle**
- **Lombok**

---

## Installation

### Prerequisites
- Java 17+
- Maven or Gradle
- PostgreSQL

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/rafliandreansyah/rental-car-server
   cd rental-car-server
   ```

2. Configure the database:
   - Create a new database, e.g., `rental_car_db`.
   - Update `application.properties` or `application.yml`:
     ```properties
     spring.datasource.url=jdbc:postgresql://localhost:5432/rental_car_db
     spring.datasource.username=<your_username>
     spring.datasource.password=<your_password>
     spring.jpa.hibernate.ddl-auto=update
     ```

3. Run the application:
   - With Maven:
     ```bash
     mvn spring-boot:run
     ```
   - With Gradle:
     ```bash
     ./gradlew bootRun
     ```

4. Access the API:
   - The application will run at: `http://localhost:8080`

---

## Security
- **JWT** is used for authentication, sent in the `Authorization` header:
  ```
  Authorization: Bearer <token>
  ```
---

## Contribution
Contributions are welcome! Please create a **pull request** or submit an **issue** for discussion.

---

## License
This project is licensed under the MIT License. See the `LICENSE` file for details.

---

## Contact
For inquiries or support, please contact [rafliandreansyah957@gmail.com].
