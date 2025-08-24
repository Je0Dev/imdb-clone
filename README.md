# 🎬 IMDB Clone

A JavaFX-based clone of the Internet Movie Database (IMDB) that allows users to browse, search, and manage movies, TV series, actors, and directors with a modern UI.

## ✨ Key Features

### 🎥 Content Management
- **Movie & TV Show Browsing**: View detailed information about movies and TV series
- **Advanced Search**: Search across movies, TV shows, actors, and directors
- **Content Filtering**: Filter by genre, year, rating, and more
- **Data Import**: Load initial data from text files

### 🔐 Authentication & User Management
- **Secure Login/Registration**: BCrypt password hashing
- **User Profiles**: View and manage your profile
- **Session Management**: Token-based authentication

### 🏗️ Technical Architecture
- **MVC Pattern**: Clear separation of concerns
- **Service Layer**: Business logic encapsulation
- **Repository Pattern**: Data access abstraction
- **Dependency Injection**: Using custom ServiceLocator

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8.0 or higher
- Git (for version control)

### System Requirements
- Minimum 2GB RAM (4GB recommended)
- 1GB free disk space
- 1366x768 display resolution or higher

## ⚙️ Installation

### Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/imdb-clone.git
   cd imdb-clone
   ```

2. Build the project with Maven:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn javafx:run
   ```

### Production Deployment

1. Create an executable JAR:
   ```bash
   mvn clean package
   ```

2. Run the application:
   ```bash
   java -jar target/imdb-clone-1.0.0-jar-with-dependencies.jar
   ```

### Initial Setup

1. On first run, the application will create necessary data files in the `data/` directory
2. Default admin credentials:
   - Username: admin
   - Password: admin123 (change immediately after first login)
3. Sample data will be loaded automatically from `src/main/resources/data/`

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/papel/imdb_clone/
│   │       ├── config/           # Application configuration
│   │       ├── controllers/      # UI controllers
│   │       ├── data/             # Data management
│   │       ├── enums/            # Enumerations
│   │       ├── exceptions/       # Custom exceptions
│   │       ├── gui/              # Main application class
│   │       ├── model/            # Data models
│   │       ├── repository/       # Data access
│   │       ├── service/          # Business logic
│   │       ├── services/         # Additional services
│   │       ├── tools/            # Utility tools
│   │       └── util/             # Utility classes
│   └── resources/
│       ├── data/                # Data files
│       └── fxml/                # FXML view definitions
```

### Core Components

1. **Controllers**
   - `RefactoredMainController`: Main application controller
   - `AuthController`: Handles user authentication
   - `ContentController`: Manages content display and search
   - `UICoordinator`: Coordinates UI components

2. **Services**
   - `AuthService`: User authentication and session management
   - `ContentService`: Manages movie and TV show data
   - `DataManager`: Handles data loading and persistence
   - `ServiceLocator`: Central service locator

3. **Models**
   - `User`: User information and authentication
   - `Movie`: Movie details and metadata
   - `Series`: TV series information
   - `Celebrity`: Base class for actors/directors
   - `Actor`/`Director`: Specific celebrity types

## 📊 Data Management

The application loads initial data from text files in `src/main/resources/data/`:

- `movies_updated.txt`: Movie information
- `series_updated.txt`: TV series information
- `actors_updated.txt`: Actor profiles
- `directors_updated.txt`: Director information
- `awards_boxoffice_updated.txt`: Awards data
- `users_updated.txt`: User accounts

### Key Classes

#### Movie
```java
public class Movie {
    private String id;
    private String title;
    private int year;
    private String genre;
    private String director;
    private List<String> actors;
    private String plot;
    private double rating;
    // Getters and setters
}
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
- All contributors

## 📞 Support

For support, please open an issue in the repository.

---

*Last updated: August 2023*
