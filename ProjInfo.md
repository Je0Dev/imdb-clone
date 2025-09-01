IMDB Clone - Technical Documentation
Project Overview
The IMDB Clone is a Java-based desktop application that replicates core functionality of the Internet Movie Database (
IMDB). It provides features for browsing, searching, and managing information about movies, TV shows, actors, and
directors.

Project Structure

1. Core Packages
   com.papel.imdb_clone.config
   ApplicationConfig: Handles application-wide configuration settings and properties.
   com.papel.imdb_clone.controllers
   Contains JavaFX controllers for the application's UI:

AuthController: Manages user authentication (login/register) flows.
CelebritiesController: Handles display and management of celebrity/actor information.
ContentController: Base controller for content-related views.
ContentDetailsController: Displays detailed information about specific content.
RefactoredMainController: Main application controller after refactoring.
UICoordinator: Coordinates UI components and navigation.
com.papel.imdb_clone.data
RefactoredDataManager: Central data management class that coordinates between services and repositories.
SearchCriteria: Defines search parameters for content queries.
com.papel.imdb_clone.enums
Contains enumerations used throughout the application:

ContentType: Defines types of content (MOVIE, TV_SHOW, etc.)
Ethnicity: Represents different ethnic groups
Genre: Defines various media genres (Action, Comedy, Drama, etc.)
com.papel.imdb_clone.exceptions
Custom exceptions for error handling:

AuthException: Authentication-related errors
DuplicateEntryException: Duplicate data entries
EntityNotFoundException: Requested entity not found
FileParsingException: File parsing errors
InvalidEntityException: Invalid entity state
UserAlreadyExistsException: Duplicate user registration
ValidationException: Data validation failures
com.papel.imdb_clone.gui
ImprovedMovieApp: Main application class extending JavaFX Application.
com.papel.imdb_clone.model
Core domain models:

Actor: Represents an actor/actress, extending Celebrity
Celebrity: Abstract base class for people in the entertainment industry
Content: Base class for all media content
Director: Represents a film/TV director
Movie: Represents a film, extends Content
Rating: User ratings and reviews
Season: Represents a season of a TV series
Series: Represents a TV series, extends Content
User: Application user accounts
com.papel.imdb_clone.repository
Data access interfaces and implementations:

InMemoryMovieRepository: In-memory storage for movies
InMemorySeriesRepository: In-memory storage for TV series
InMemoryUserRepository: In-memory storage for users
MovieRepository: Interface for movie data operations
SeriesRepository: Interface for series data operations
UserRepository: Interface for user data operations
com.papel.imdb_clone.service
Business logic and service layer:

AuthService: Authentication and user management
CelebrityService: Business logic for celebrity operations
ContentDataLoader: Loads content data from files
ContentService: Generic service for content operations
EncryptionService: Handles password hashing and verification
NavigationService: Manages view navigation
SearchService: Handles search functionality
ServiceLocator: Service locator pattern implementation
UserService: User-related business logic
UserStorageService: Manages user data persistence
com.papel.imdb_clone.tools
UserDataRegenerator: Utility for user data regeneration
com.papel.imdb_clone.util
Utility classes:

AppStateManager: Manages application state and settings
DataFileLoader: Loads data from files
UIUtils: Common UI helper methods
Key Features
Authentication System
User registration and login
Session management
Password encryption
Guest access
Content Management
Movie and TV show listings
Detailed view for each content type
Search and filtering
Content categorization by genre, year, etc.
Celebrity Information
Actor/Actress profiles
Director information
Filmography
Search by name, ethnicity, notable works
User Experience
Responsive UI with JavaFX
Theming support
Navigation between views
Form validation
Technical Stack
Language: Java 11+
UI Framework: JavaFX
Build Tool: Maven
Logging: SLF4J with Logback
Concurrency: Java Util Concurrent
Design Patterns
Repository Pattern: For data access
Service Layer: For business logic
Singleton: For services that need single instances
Observer: For UI updates
Factory: For object creation
Data Flow
UI Layer: JavaFX views and controllers
Service Layer: Business logic and validation
Repository Layer: Data access and storage
Model Layer: Domain objects and business entities
Configuration
Application settings are managed in
AppStateManager
and persisted to a properties file. Configuration includes:

UI theme
Font size
Window dimensions
Last active tab
Error Handling
Custom exceptions for different error scenarios
Centralized error handling in controllers
User-friendly error messages
Logging of errors for debugging
Security
Password hashing with salt
Input validation
Session management
Secure storage of sensitive data
Future Improvements
Database integration
REST API for remote data access
Enhanced search with filters
User profiles and watchlists
Rating and review system
Image and media support