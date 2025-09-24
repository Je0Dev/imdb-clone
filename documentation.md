# IMDB Clone Application Documentation

## Table of Contents
1. [Introduction](#introduction)
2. [Architecture Overview](#architecture-overview)
3. [Package Structure](#package-structure)
4. [Core Components](#core-components)
   - [Models](#models)
   - [Controllers](#controllers)
   - [Services](#services)
   - [Repositories](#repositories)
   - [Utilities](#utilities)
5. [Data Flow](#data-flow)
6. [Configuration](#configuration)
7. [Dependencies](#dependencies)
8. [Getting Started](#getting-started)
9. [Troubleshooting](#troubleshooting)

## Introduction

The IMDB Clone is a JavaFX-based application that provides functionality similar to IMDB, allowing users to browse and manage information about movies, TV series, actors, and directors. The application features a user-friendly interface for searching, viewing, and managing entertainment content.

## Architecture Overview

The application follows a layered architecture with clear separation of concerns:

- **Presentation Layer**: JavaFX-based GUI components
- **Business Logic Layer**: Controllers and Services
- **Data Access Layer**: Repositories and Data Models
- **Utility Layer**: Helper classes and utilities

## Package Structure

```
src/main/java/com/papel/imdb_clone/
├── config/          # Configuration classes
├── controllers/     # Application controllers
├── data/            # Data transfer objects
├── enums/           # Enumerations
├── exceptions/      # Custom exceptions
├── gui/             # JavaFX UI components
├── model/           # Data models
│   ├── content/     # Content-related models
│   ├── people/      # People-related models
│   └── rating/      # Rating and review models
├── repository/      # Data access layer
├── service/         # Business logic
└── util/            # Utility classes
```

## Core Components

### Models

The application uses a rich domain model to represent its core entities:

#### Content Models
- **Content**: Base class for all content types
- **Movie**: Represents a movie with properties like title, release year, etc.
- **Series**: Represents a TV series with seasons and episodes
- **Season**: Represents a season within a TV series
- **Episode**: Represents an individual episode within a season

#### People Models
- **Person**: Base class for people in the system
- **Actor**: Represents an actor with roles in movies/series
- **Director**: Represents a director of movies/series
- **CrewMember**: Represents other crew members

#### Rating Models
- **Rating**: Represents a user's rating of content
- **Review**: Represents a written review with text and rating

### Controllers

Controllers handle user input and manage the flow of data between the UI and services:

- **ContentController**: Manages content-related operations
- **MovieController**: Handles movie-specific functionality
- **SeriesController**: Manages TV series operations
- **PeopleController**: Handles actor and director management
- **RatingController**: Manages ratings and reviews

### Services

Services contain the business logic:

- **ContentService**: Core content management logic
- **MovieService**: Movie-specific business rules
- **SeriesService**: Series-specific operations
- **PeopleService**: People management logic
- **RatingService**: Rating and review management
- **SearchService**: Implements search functionality
- **ImportExportService**: Handles data import/export

### Repositories

Data access layer components:

- **ContentRepository**: Data access for content
- **MovieRepository**: Movie data operations
- **SeriesRepository**: Series data operations
- **PeopleRepository**: People data access
- **RatingRepository**: Rating and review data access

### Utilities

- **DataValidator**: Validates input data
- **FileHandler**: Handles file operations
- **DateUtils**: Date and time utilities
- **StringUtils**: String manipulation helpers

## Data Flow

1. User interacts with the JavaFX UI
2. Controllers receive user input
3. Controllers call appropriate services
4. Services implement business logic and use repositories
5. Repositories interact with the data store
6. Data flows back through the layers to update the UI

## Configuration

The application uses several configuration files:

- `application.properties`: Main configuration file
- `log4j2.xml`: Logging configuration
- Data files in `src/main/resources/data/`

## Dependencies

- JavaFX: For the graphical user interface
- Hibernate: ORM for database operations
- Log4j2: Logging framework
- JUnit: For unit testing
- Maven: Build and dependency management

## Getting Started

1. **Prerequisites**
   - Java JDK 11 or later
   - Maven 3.6 or later

2. **Building the Application**
   ```bash
   mvn clean install
   ```

3. **Running the Application**
   ```bash
   mvn javafx:run
   ```

## Troubleshooting

### Common Issues

1. **JavaFX Not Found**
   - Ensure JavaFX is properly installed and configured
   - Check that the JavaFX SDK is in your project's module path

2. **Database Connection Issues**
   - Verify database credentials in `application.properties`
   - Ensure the database server is running

3. **UI Rendering Problems**
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
