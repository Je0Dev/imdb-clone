package com.papel.imdb_clone.service;

import com.papel.imdb_clone.controllers.coordinator.UICoordinator;
import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.model.*;
import com.papel.imdb_clone.repository.impl.InMemoryMovieRepository;
import com.papel.imdb_clone.repository.impl.InMemorySeriesRepository;
import com.papel.imdb_clone.repository.impl.InMemoryUserRepository;
import com.papel.imdb_clone.service.data.FileDataLoaderService;
import com.papel.imdb_clone.service.data.loader.DataLoaderFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
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
    private static final ConcurrentMap<Object, Object> services = new ConcurrentHashMap<>();
    private static RefactoredDataManager dataManager;
    private static UICoordinator uiCoordinator;
    private static Stage primaryStage;
    private static FileDataLoaderService fileDataLoaderService;
    private static DataLoaderFactory dataLoaderFactory;
    private static volatile boolean servicesInitialized = false;
    private static final Object lock = new Object();

    private ServiceLocator() {
        // Private constructor to prevent instantiation
    }

    /**
     * Singleton pattern implementation for ServiceLocator.
     * Ensures that only one instance of ServiceLocator is created.
     * Thread-safe implementation using double-checked locking.
     * Sychronized method to ensure thread safety means that only one thread can access the method at a time.
     * @return
     */
    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            synchronized (ServiceLocator.class) {
                if (instance == null) {
                    instance = new ServiceLocator();
                    // Initialize services when the instance is first created
                    instance.initializeServices();
                }
            }
        }
        return instance;
    }

    /**
     * Sets the primary stage for the application.
     * If the primary stage is not already set and the provided stage is not null,
     * it synchronizes on the ServiceLocator class to ensure thread safety.
     * If the primary stage is not already set and the provided stage is not null,
     * it synchronizes on the ServiceLocator class to ensure thread safety.
     * @param stage
     */
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

    //getter for primary stage
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    //initialize services
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

                UserService userService = UserService.getInstance(dataManager);
                registerService(UserService.class, userService);

                // Initialize content services with proper types
                MoviesService moviesService = MoviesService.getInstance();
                SeriesService seriesService = SeriesService.getInstance();
                CelebrityService<Actor> actorService = new CelebrityService<>(Actor.class);
                CelebrityService<Director> directorService = new CelebrityService<>(Director.class);

                // Register services with type-safe qualifiers
                registerService(MoviesService.class, moviesService);
                registerService(SeriesService.class, seriesService);
                registerService(CelebrityService.class, actorService, "actor");
                registerService(CelebrityService.class, directorService, "director");

                // Register for backward compatibility
                registerService(MoviesService.class, moviesService);

                // Initialize data loader factory with concrete repository types
                dataLoaderFactory = new DataLoaderFactory(
                        userRepository,
                        movieRepository,
                        seriesRepository,
                        moviesService,
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
                        moviesService
                );
                registerService(FileDataLoaderService.class, fileDataLoaderService);

                // Initialize and register SearchService early in the process
                // to ensure it's available when other services need it
                SearchService searchService = new SearchService(dataManager);
                registerService(SearchService.class, searchService);
                logger.info("SearchService initialized successfully");

                // Initialize UI Coordinator if primary stage is available
                if (primaryStage != null) {
                    uiCoordinator = new UICoordinator(dataManager);
                    uiCoordinator.setPrimaryStage(primaryStage);
                    registerService(UICoordinator.class, uiCoordinator);
                    logger.info("UICoordinator initialized successfully with primary stage");
                } else {
                    logger.warn("Primary stage not set, UICoordinator initialization will be deferred");
                }

                // Set services as initialized after all services are registered
                servicesInitialized = true;
                logger.info("All services initialized successfully");

            } catch (Exception e) {
                logger.error("Failed to initialize services: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize services", e);
            }
        }
    }

    /**
     * Get the RefactoredDataManager instance.
     * If the RefactoredDataManager is not already initialized, it synchronizes on the ServiceLocator class to ensure thread safety.
     * @return
     */
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

    /**
     * Get the UICoordinator instance.
     * If the UICoordinator is not already initialized, it synchronizes on the ServiceLocator class to ensure thread safety.
     * If the primary stage is not set, it throws an IllegalStateException.
     * @return
     */
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
     * @param serviceClass The class of the service to register
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
            if (hasService(RefactoredDataManager.class)) {
                logger.debug("Shutting down RefactoredDataManager");
            }
        } catch (Exception e) {
            logger.error("Error shutting down services", e);
        }

        services.clear();
        logger.info("Service shutdown complete");
    }

    /**
     * Get a service instance by class
     * @param serviceClass that the service implements
     * @return the service instance which means the object that implements the service
     * @param <T> which means the type of the service that we want to get
     */
    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> serviceClass) {
        if (!services.containsKey(serviceClass)) {
            throw new IllegalArgumentException("Service not found: " + serviceClass.getName());
        }
        logger.debug("Service found: {}", serviceClass.getName());

        return (T) services.get(serviceClass);
    }

    /**
     * Get a service instance by class and qualifier
     * @param serviceClass that the service implements
     * @param qualifier the qualifier of the service
     * @return the service instance which means the object that implements the service
     * @param <T> which means the type of the service that we want to get
     */
    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> serviceClass, String qualifier) {
        if (!services.containsKey(serviceClass.getName() + "_" + qualifier)) {
            throw new IllegalArgumentException("Service not found: " + serviceClass.getName() + "_" + qualifier);
        }
        logger.debug("Service found: {} with qualifier: {}", serviceClass.getName(), qualifier);
        logger.info("Service found: {} with qualifier: {}", serviceClass.getName(), qualifier);
        return (T) services.get(serviceClass.getName() + "_" + qualifier);
    }
}