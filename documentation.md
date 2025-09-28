# IMDB Clone Application Documentation

## Table of Contents
1. [Introduction](#introduction)
2. [Architecture Overview](#architecture-overview)
3. [Package Structure](#package-structure)
4. [Core Components](#core-components)
   - [Authentication System](#authentication-system)
   - [Models](#models)
   - [Controllers](#controllers)
   - [Services](#services)
   - [Repositories](#repositories)
   - [GUI Components](#gui-components)
5. [Configuration](#configuration)
6. [Data Management](#data-management)
7. [Getting Started](#getting-started)
8. [Troubleshooting](#troubleshooting)

## Introduction

The IMDB Clone is a comprehensive JavaFX-based application that replicates core IMDB functionality, enabling users to browse, search, and manage information about movies, TV series, actors, and directors. 
The application features a modern, user-friendly interface with a simple authentication system, search functionality, and content management capabilities.

## Architecture Overview

The application follows a clean architecture with clear separation of concerns:

- **Presentation Layer (GUI)**: JavaFX-based UI components and FXML views
- **Application Layer**: Controllers that handle user input and navigation
- **Domain Layer**: Business logic and service implementations
- **Data Access Layer**: Repositories and data models
- **Infrastructure**: Configuration, utilities, and helper classes

The application uses dependency injection through a custom `ServiceLocator` pattern for better testability and maintainability.

## Package Structure

```
src/main/java/com/papel/imdb_clone/
├── config/                  # Application configuration
│   └── ApplicationConfig.java  # Main configuration class
├── controllers/             # Application controllers
│   └── authentication/      # Authentication controllers
│       ├── AuthController.java  # Handles login/register logic
│       └── ...
├── data/                    # Data transfer objects and management
│   └── DataManager.java     # Central data management
├── enums/                   # Enumerations
├── exceptions/              # Custom exceptions
│   └── AuthException.java   # Authentication-specific exceptions
├── gui/                     # JavaFX UI components
│   └── MovieAppGui.java     # Main application class
├── model/                   # Data models
│   ├── content/             # Content-related models
│   ├── people/              # People-related models
│   └── rating/              # Rating and review models
├── repository/              # Data access layer
├── service/                 # Business logic
│   ├── validation/          # Validation services
│   │   └── AuthService.java # Authentication service
│   └── search/              # Search functionality
│       └── ServiceLocator.java  # Service locator pattern
└── util/                    # Utility classes
```

## Core Components

### Authentication System

The authentication system provides secure user management with the following components:

- **AuthController**: Manages user authentication flows (login/register)
- **AuthService**: Handles validation and business logic for authentication
- **AuthException**: Custom exception for authentication-related errors
- **Login/Register Views**: FXML-based UI for user authentication

Key Features:
- Secure password handling
- Input validation
- Session management
- Error handling and user feedback

### Models

The application uses a rich domain model to represent its core entities:

#### Content Models
- **Content**: Base class for all content types with common properties
- **Movie**: Represents a movie with properties like title, release year, and genre
- **Series**: Manages TV series with seasons and episodes
- **Season**: Represents a season within a TV series
- **Episode**: Individual episode details including runtime and plot

#### People Models
- **Person**: Base class for people in the system
- **Actor**: Represents actors with filmography and biography
- **Director**: Tracks directors and their works
- **User**: Represents application users with authentication details

#### Rating & Review Models
- **Rating**: Numeric rating with timestamp and user reference
- **Review**: Detailed review with text content and associated rating

### Controllers

Controllers handle user input and manage the flow of data between the UI and services:

#### Authentication Controllers
- **AuthController**: 
  - `handleLogin()`: Authenticates users
  - `handleRegister()`: Creates new user accounts
  - `validateInput()`: Validates user input fields

#### Content Controllers
- **ContentController**: Base controller for content operations
- **MovieController**: Handles movie-specific functionality
- **SeriesController**: Manages TV series operations
- **PeopleController**: Handles actor and director management
- **RatingController**: Manages ratings and reviews with methods for:
  - Adding/updating ratings
  - Submitting reviews
  - Calculating average ratings

### Services

Services implement the core business logic:

#### Authentication Services
- **AuthService**:
  - `authenticateUser()`: Validates user credentials
  - `registerUser()`: Creates new user accounts
  - `validatePassword()`: Ensures password meets requirements

#### Content Services
- **ContentService**: Core content management
- **MovieService**: Movie-specific operations
- **SeriesService**: Manages TV series and episodes
- **PeopleService**: Handles actor/director data
- **RatingService**: Manages user ratings and reviews
- **SearchService**: Implements search across all content types

#### Utility Services
- **ServiceLocator**: Centralized service management
- **DataManager**: Handles data persistence and retrieval

### Repositories

Data access layer components that interact with the data store:

- **ContentRepository**: Manages content data access
- **UserRepository**: Handles user data persistence
- **RatingRepository**: Manages rating and review data
- **PeopleRepository**: Handles actor/director data access

### GUI Components

#### Main Application
- **MovieAppGui**: Main application class extending JavaFX Application
  - `start()`: Initializes the application and shows login screen
  - `showLoginView()`: Displays login form
  - `showRegisterView()`: Displays registration form
  - `showMainApplication()`: Shows main application after login

#### FXML Views
- **login-view.fxml**: Login form UI
- **register-view.fxml**: Registration form UI
- **main-view.fxml**: Main application interface
- **MovieRepository**: Movie data operations
- **SeriesRepository**: Series data operations
- **PeopleRepository**: People data access
- **RatingRepository**: Rating and review data access

### Utilities
   - Check JavaFX version compatibility
   - Verify that all required CSS files are in the correct location

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
