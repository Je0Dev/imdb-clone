# IMDB Clone

A JavaFX-based clone of the Internet Movie Database (IMDB) that provides a modern UI for browsing and managing movies,
TV series, and celebrity information.

## Key Features

### Content Management

- **Movie & TV Show Browsing**: View detailed information about movies and TV series
- **Advanced Search**: Search across movies, TV shows, actors, and directors
- **User Authentication**: Secure login with session management
- **Content Rating**: Rate and review movies and shows
- **Responsive UI**: Modern JavaFX interface with FXML

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
