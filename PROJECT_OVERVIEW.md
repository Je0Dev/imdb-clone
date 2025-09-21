# IMDB Clone - Project Overview

## 📖 Introduction
IMDB Clone is a feature-rich desktop application that replicates the core functionality of the Internet Movie Database (IMDB). Built with JavaFX, it provides users with a modern, responsive interface to explore movies, TV shows, and celebrity information.

## 🎯 Core Features

### 🎬 Content Exploration
- Browse an extensive collection of movies and TV shows
- View detailed information including plot summaries, cast, ratings, and more
- Discover trending and top-rated content
- Filter and sort content by various criteria (genre, year, rating, etc.)

### 🎭 Celebrity Profiles
- Comprehensive profiles for actors, directors, and other industry professionals
- Filmography with role details
- Biography and career highlights

### 🔍 Advanced Search
- Full-text search across all content types
- Advanced filtering options
- Search history and suggestions

### 👤 User Experience
- User registration and authentication
- Personalized watchlists
- Rating and review system
- Responsive UI with smooth navigation

## 🏗️ Technical Implementation

### Architecture
- **MVC Architecture**: Clean separation of concerns with Models, Views, and Controllers
- **Layered Architecture**: Clear separation between presentation, business logic, and data access layers
- **Dependency Injection**: Custom service locator pattern for managing dependencies

### Key Components

#### 1. Data Models
- `Content`: Base class for all content types (movies, TV shows)
- `Movie`: Represents movie content with specific attributes
- `Series`: Represents TV series with seasons and episodes
- `Celebrity`: Base class for people in the entertainment industry
- `User`: Manages user accounts and preferences

#### 2. Services
- `ContentService`: Handles content-related business logic
- `SearchService`: Implements search functionality
- `UserService`: Manages user accounts and authentication
- `RatingService`: Handles content ratings and reviews

#### 3. User Interface
- Built with JavaFX and FXML for modern, responsive design
- Custom CSS theming
- Responsive layouts that adapt to different screen sizes

## 🔄 Data Flow
1. **Data Loading**: Application loads initial data from serialized storage
2. **User Interaction**: User interacts with the UI components
3. **Controller Layer**: User actions are handled by appropriate controllers
4. **Service Layer**: Business logic is processed
5. **Data Access**: Data is retrieved or updated through repositories
6. **Response**: UI is updated to reflect changes

## 🚀 Getting Started

### Prerequisites
- Java 21 or later
- Maven 3.8.0 or later

### Running the Application
1. Clone the repository
2. Build with Maven: `mvn clean install`
3. Run: `mvn javafx:run`

## 📊 Data Management
- In-memory data storage with serialization for persistence
- Data validation and sanitization
- Efficient data retrieval and caching

## 🔒 Security
- Secure password hashing with BCrypt
- Session management
- Input validation and sanitization

## 📈 Future Enhancements
- Integration with external movie/TV APIs
- User recommendations based on viewing history
- Social features (following users, activity feeds)
- Offline mode with data synchronization

## 📚 Additional Resources
- [API Documentation](docs/API.md)
- [Developer Guide](docs/DEVELOPER_GUIDE.md)
- [Contribution Guidelines](CONTRIBUTING.md)
