# IMDB Clone

A comprehensive JavaFX-based clone of the Internet Movie Database (IMDB) that provides a modern, responsive UI for browsing and managing movies, TV series, and celebrity information. Built with Java 21 and JavaFX 21.0.3, this application demonstrates clean architecture, SOLID principles, and modern Java development practices.

## 🚀 Key Features

### 🎬 Content Management
- **Movies & TV Shows**: Browse, search, and filter movies and TV series with detailed information
- **Celebrity Profiles**: View actor and director profiles with filmography
- **User Authentication**: Secure registration and login system with session management
- **Rating System**: Rate and review content with a 1-10 rating scale
- **Responsive UI**: Modern JavaFX interface with FXML and CSS styling
- **Data Management**: In-memory data storage with serialization support
- **Search Functionality**: Advanced search across all content types
- **Navigation**: Intuitive UI with smooth transitions between views

## 🛠️ Technical Stack

- **Language**: Java 21
- **UI Framework**: JavaFX 21.0.3
- **Build Tool**: Maven
- **Dependency Injection**: Custom Service Locator pattern
- **Logging**: SLF4J with Simple Binding
- **JSON Processing**: Jackson Databind 2.16.1
- **Testing**: JUnit 5, TestFX

## 🏗️ Project Structure

```
src/main/java/com/papel/imdb_clone/
├── config/               # Application configuration
│   └── ApplicationConfig.java
├── controllers/          # JavaFX controllers
│   ├── authentication/   # Authentication controllers
│   ├── base/             # Base controllers
│   ├── content/          # Content management controllers
│   ├── coordinator/      # UI coordination
│   └── people/           # People/celebrities controllers
├── data/                 # Data management
│   └── DataManager.java
├── enums/                # Enumerations
│   ├── ContentType.java
│   ├── UserRole.java
│   └── ViewType.java
├── exceptions/           # Custom exceptions
│   ├── AuthenticationException.java
│   └── DataAccessException.java
├── gui/                  # Main application GUI
│   └── MovieAppGui.java
├── model/                # Domain models
│   ├── content/          # Content-related models
│   │   ├── Content.java
│   │   ├── Movie.java
│   │   ├── Series.java
│   │   ├── Season.java
│   │   └── Episode.java
│   ├── people/           # People-related models
│   └── rating/           # Rating-related models
├── repository/           # Data access
│   └── impl/             # Repository implementations
├── service/              # Business logic services
│   ├── content/          # Content services
│   ├── data/             # Data loading services
│   ├── navigation/       # Navigation services
│   ├── people/           # People services
│   ├── search/           # Search functionality
│   └── validation/       # Input validation
└── util/                 # Utility classes
```

## 🚀 Getting Started

### Prerequisites
- Java 21 or later
- Maven 3.6.0 or later

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Je0Dev/imdb-clone.git
   cd imdb-clone
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn javafx:run
   ```

## 📚 Documentation

## 🤝 Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Project Structure

```
src/main/java/com/papel/imdb_clone/
├── config/               # Application configuration
├── controllers/          # JavaFX controllers
│   ├── coordinator/      # UI coordination
│   ├── AuthController.java
│   ├── ContentController.java
│   └── RefactoredMainController.java
├── data/                 # Data management
│   └── RefactoredDataManager.java
├── enums/                # Enumerations
│   ├── ContentType.java
│   ├── UserRole.java
│   └── ViewType.java
├── exceptions/           # Custom exceptions
├── gui/                  # Main application GUI
│   └── ImprovedMovieApp.java
├── model/                # Domain models
│   ├── Actor.java
│   ├── Celebrity.java
│   ├── Content.java
│   ├── Director.java
│   ├── Episode.java
│   ├── Movie.java
│   ├── Rating.java
│   ├── Season.java
│   ├── Series.java
│   ├── User.java
│   └── UserRating.java
├── repository/           # Data access
│   └── impl/             # Repository implementations
├── service/              # Business logic services
│   ├── data/             # Data loading services
│   ├── validation/       # Input validation
│   ├── AuthService.java
│   ├── CelebrityService.java
│   ├── ContentService.java
│   ├── EncryptionService.java
│   ├── SearchService.java
│   ├── ServiceLocator.java
│   ├── UserService.java
│   └── UserStorageService.java
├── tools/                # Utility tools
└── util/                 # Utility classes
   └── AppStateManager.java
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8.0 or higher

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/imdb-clone.git
   cd imdb-clone
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn javafx:run
   ```

#### User

```java
public class User {
    private String id;
    private String username;
    private String passwordHash; // BCrypt hashed
    private LocalDateTime createdAt;
    // Getters and setters
}
```

#### Celebrity (Base Class)

```java
public abstract class Celebrity {
    private String id;
    private String name;
    private LocalDate birthDate;
    private String biography;
    // Common fields and methods
}
```

## 🛠️ Development

### Setup Development Environment

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd imdb-clone
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn javafx:run
   ```

### Code Style

- Follow standard Java naming conventions
- Use meaningful variable and method names
- Add Javadoc for public APIs
- Keep methods focused and concise

### Testing

Run the complete test suite:

```bash
mvn test
```

Run specific test class:

```bash
mvn test -Dtest=ForumServiceTest
```

Generate test coverage report:

```bash
mvn jacoco:report
```

## 🚨 Troubleshooting

### Common Issues

#### Application Won't Start

- Ensure Java 17+ is installed
- Check if required ports are available
- Verify data files exist in `src/main/resources/data/`

#### Data Loading Issues

- Check console for error messages
- Ensure data files are properly formatted
- Verify file permissions

#### UI Problems

- Try resizing the window
- Check for error dialogs
- Restart the application

### Getting Help

1. Check the [Wiki](../../wiki) for known issues
2. Search existing issues
3. Open a new issue with:
    - Steps to reproduce
    - Expected vs actual behavior
    - Screenshots if applicable
    - Log files (remove sensitive data)

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- JavaFX for the UI framework
- Maven for build automation

## 📞 Support

For support, please open an issue in the repository.

---

*Last updated: August 2025*
