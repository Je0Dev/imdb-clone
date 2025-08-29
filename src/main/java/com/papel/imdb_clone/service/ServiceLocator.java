package com.papel.imdb_clone.service;

import com.papel.imdb_clone.controllers.coordinator.UICoordinator;
import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.model.Actor;
import com.papel.imdb_clone.model.Director;
import com.papel.imdb_clone.model.Movie;
import com.papel.imdb_clone.model.Series;
import com.papel.imdb_clone.repository.impl.InMemoryMovieRepository;
import com.papel.imdb_clone.repository.impl.InMemorySeriesRepository;
import com.papel.imdb_clone.repository.impl.InMemoryUserRepository;
import com.papel.imdb_clone.service.data.FileDataLoaderService;
import com.papel.imdb_clone.service.data.loader.DataLoaderFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service Locator pattern implementation for managing application services.
 * Provides centralized access to services and ensures singleton behavior.
 * Updated to use the refactored service-oriented architecture.
 */
public class ServiceLocator {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLocator.class);
    private static ServiceLocator instance;
    private final ConcurrentMap<Object, Object> services = new ConcurrentHashMap<>();
    private static RefactoredDataManager dataManager;
    private static UICoordinator uiCoordinator;
    private static Stage primaryStage;
    private static FileDataLoaderService fileDataLoaderService;
    private static DataLoaderFactory dataLoaderFactory;
    private EncryptionService encryptionService;
    private static volatile boolean servicesInitialized = false;
    private static final Object lock = new Object();

    private ServiceLocator() {
        // Private constructor to prevent instantiation
    }

    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            synchronized (ServiceLocator.class) {
                if (instance == null) {
                    instance = new ServiceLocator();
                }
            }
        }
        return instance;
    }

    public static void setPrimaryStage(Stage stage) {
        if (primaryStage == null && stage != null) {
            synchronized (ServiceLocator.class) {
                if (primaryStage == null) {
                    primaryStage = stage;
                    // If we already have an instance but services aren't initialized yet, initialize them
                    if (instance != null && !servicesInitialized) {
                        instance.initializeServices();
                    }
                }
            }
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    private void initializeServices() {
        if (servicesInitialized) {
            return;
        }

        synchronized (lock) {
            if (servicesInitialized) {
                return;
            }

            try {
                logger.info("Initializing application services...");

                // Initialize core services first
                dataManager = RefactoredDataManager.getInstance();
                registerService(RefactoredDataManager.class, dataManager);
                logger.info("DataManager initialized successfully");

                // Initialize repositories with concrete types
                InMemoryUserRepository userRepository = dataManager.getUserRepository();
                InMemoryMovieRepository movieRepository = (InMemoryMovieRepository) dataManager.getMovieRepository();
                InMemorySeriesRepository seriesRepository = (InMemorySeriesRepository) dataManager.getSeriesRepository();

                // Initialize services
                encryptionService = new EncryptionService();
                UserService userService = UserService.getInstance(dataManager, encryptionService);
                registerService(UserService.class, userService);

                // Initialize content services with proper generic types
                ContentService<Movie> movieService = new ContentService<>(Movie.class);
                ContentService<Series> seriesService = new ContentService<>(Series.class);
                CelebrityService<Actor> actorService = new CelebrityService<>(Actor.class);
                CelebrityService<Director> directorService = new CelebrityService<>(Director.class);

                // Register content services with type-safe qualifiers
                registerService(ContentService.class, movieService, "movie");
                registerService(ContentService.class, seriesService, "series");
                registerService(CelebrityService.class, actorService, "actor");
                registerService(CelebrityService.class, directorService, "director");

                // Register default ContentService instance for backward compatibility
                registerService(ContentService.class, movieService);

                // Initialize data loader factory with concrete repository types
                dataLoaderFactory = new DataLoaderFactory(
                        userRepository,
                        movieRepository,
                        seriesRepository,
                        movieService,
                        seriesService,
                        actorService,
                        directorService
                );
                registerService(DataLoaderFactory.class, dataLoaderFactory);

                // Initialize file data loader service with concrete repository types
                fileDataLoaderService = new FileDataLoaderService(
                        userRepository,
                        movieRepository,
                        seriesRepository,
                        seriesService,
                        actorService,
                        directorService,
                        movieService
                );
                registerService(FileDataLoaderService.class, fileDataLoaderService);

                // Initialize search service
                SearchService searchService = new SearchService(dataManager);
                registerService(SearchService.class, searchService);

                // Initialize UI Coordinator if primary stage is available
                if (primaryStage != null) {
                    uiCoordinator = new UICoordinator(dataManager);
                    uiCoordinator.setPrimaryStage(primaryStage);
                    registerService(UICoordinator.class, uiCoordinator);
                    logger.info("UICoordinator initialized successfully with primary stage");
                } else {
                    logger.warn("Primary stage not set, UICoordinator initialization will be deferred");
                }

                servicesInitialized = true;
                logger.info("All services initialized successfully");

            } catch (Exception e) {
                logger.error("Failed to initialize services: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize services", e);
            }
        }
    }

    public RefactoredDataManager getDataManager() {
        if (dataManager == null) {
            synchronized (lock) {
                if (dataManager == null) {
                    dataManager = RefactoredDataManager.getInstance();
                    registerService(RefactoredDataManager.class, dataManager);
                }
            }
        }
        return dataManager;
    }

    public UICoordinator getUICoordinator() {
        if (uiCoordinator == null) {
            synchronized (lock) {
                if (uiCoordinator == null) {
                    if (primaryStage == null) {
                        throw new IllegalStateException("Cannot get UICoordinator: Primary stage is not set");
                    }
                    uiCoordinator = new UICoordinator(getDataManager());
                    uiCoordinator.setPrimaryStage(primaryStage);
                    registerService(UICoordinator.class, uiCoordinator);
                }
            }
        }
        return uiCoordinator;
    }

    /**
     * Register a service instance with a qualifier for multiple implementations
     */
    public <T> void registerService(Class<T> serviceClass, T serviceInstance, String qualifier) {
        if (serviceClass == null || serviceInstance == null || qualifier == null) {
            throw new IllegalArgumentException("Service class, instance, and qualifier cannot be null");
        }
        String key = serviceClass.getName() + "_" + qualifier;
        // Register qualified instance
        services.put(key, serviceInstance);
        // Also register unqualified lookups
        services.putIfAbsent(serviceClass, serviceInstance);
        services.putIfAbsent(serviceClass.getName(), serviceInstance);
        logger.debug("Registered service: {} with qualifier: {}", serviceClass.getSimpleName(), qualifier);
    }

    /**
     * Register a service instance (default qualifier)
     */
    public <T> void registerService(Class<T> serviceClass, T serviceInstance) {
        registerService(serviceClass, serviceInstance, "default");
    }

    /**
     * Get a service instance with a specific qualifier
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass, String qualifier) {
        if (serviceClass == null || qualifier == null) {
            throw new IllegalArgumentException("Service class and qualifier cannot be null");
        }

        // Try with qualified key first
        String qualifiedKey = serviceClass.getName() + "_" + qualifier;
        T service = (T) services.get(qualifiedKey);

        if (service == null) {
            // Fallback to unqualified lookup
            service = (T) services.get(serviceClass);
        }

        if (service == null) {
            throw new IllegalStateException("Service not found: " + serviceClass.getSimpleName() + " with qualifier: " + qualifier);
        }

        return service;
    }

    /**
     * Get a service instance (default qualifier)
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        if (serviceClass == null) {
            throw new IllegalArgumentException("Service class cannot be null");
        }
        // Try by class key first
        T service = (T) services.get(serviceClass);
        if (service == null) {
            // Fallback to class name key
            service = (T) services.get(serviceClass.getName());
        }
        if (service == null) {
            throw new IllegalStateException("Service not found: " + serviceClass.getSimpleName());
        }
        return service;
    }

    /**
     * Check if a service is registered
     */
    public boolean hasService(Class<?> serviceClass) {
        return services.containsKey(serviceClass) || services.containsKey(serviceClass.getName());
    }

    /**
     * Shutdown all services gracefully
     */
    public void shutdown() {
        logger.info("Shutting down services...");

        // Shutdown services in reverse order of dependency
        try {
            // Stop background executors first to prevent new tasks from scheduling
            try {
                logger.debug("AsyncExecutor shut down");
            } catch (Exception e) {
                logger.warn("Failed to shutdown AsyncExecutor", e);
            }

            if (hasService(RefactoredDataManager.class)) {
                // RefactoredDataManager might need cleanup
                logger.debug("Shutting down RefactoredDataManager");
            }
        } catch (Exception e) {
            logger.error("Error shutting down services", e);
        }

        services.clear();
        logger.info("Service shutdown complete");
    }
}