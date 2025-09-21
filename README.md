# 🎬 IMDB Clone

A comprehensive JavaFX-based clone of the Internet Movie Database (IMDB) that provides a modern, responsive UI for browsing and managing movies, TV series, and celebrity information. Built with Java 21 and JavaFX 21.0.3, this application demonstrates clean architecture, SOLID principles, and modern Java development practices.

## ✨ Features

### 🎥 Content Management
- **Movies & TV Shows**: Browse, search, and filter movies and TV series with detailed information
- **Celebrity Profiles**: View actor and director profiles with filmography
- **User Authentication**: Secure registration and login system with session management
- **Rating System**: Rate and review content with a 1-10 rating scale
- **Advanced Search**: Powerful search functionality across all content types
- **Responsive UI**: Modern JavaFX interface with FXML and CSS styling
- **Data Management**: In-memory data storage with serialization support

## 🛠️ Technical Stack

- **Language**: Java 21
- **UI Framework**: JavaFX 21.0.3
- **Build Tool**: Maven
- **Dependency Injection**: Custom Service Locator pattern
- **Logging**: SLF4J with Simple Binding
- **JSON Processing**: Jackson Databind 2.16.1
- **Testing**: JUnit 5, TestFX

## 🚀 Getting Started

### Prerequisites
- Java 21 or later
- Maven 3.8.0 or later

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

## 🏗️ Project Structure

```
src/main/java/com/papel/imdb_clone/
├── config/               # Application configuration
│   └── ApplicationConfig.java
├── controllers/          # JavaFX controllers
│   ├── authentication/   # Authentication controllers
│   ├── base/             # Base controllers
│   ├── content/          # Content management
│   ├── coordinator/      # UI coordination
│   └── people/           # People/celebrities
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
│   ├── content/          # Content models (Movie, Series, etc.)
│   ├── people/           # People models (Actor, Director)
│   └── rating/           # Rating models
├── repository/           # Data access layer
│   └── impl/             # Repository implementations
├── service/              # Business logic
│   ├── content/          # Content services
│   ├── data/             # Data services
│   ├── navigation/       # Navigation services
│   ├── people/           # People services
│   ├── search/           # Search functionality
│   └── validation/       # Input validation
└── util/                 # Utility classes
```

## 🔍 Key Components

### User Model
```java
public class User {
    private String id;
    private String username;
    private String passwordHash; // BCrypt hashed
    private LocalDateTime createdAt;
    // Getters and setters
}
```

### Celebrity Base Class
```java
public abstract class Celebrity {
    private String id;
    // Common celebrity properties and methods
}
```

## 🤝 Contributing

We welcome contributions! Please follow these steps:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📚 Documentation

For more detailed documentation, please refer to the [Wiki](https://github.com/Je0Dev/imdb-clone/wiki).

## 👥 Support

If you encounter any issues or have questions, please [open an issue](https://github.com/Je0Dev/imdb-clone/issues).
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
