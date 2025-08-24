package com.papel.imdb_clone.service;

import com.papel.imdb_clone.controllers.coordinator.UICoordinator;
import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.repository.MovieRepository;
import com.papel.imdb_clone.repository.UserRepository;
import com.papel.imdb_clone.repository.impl.InMemoryMovieRepository;
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

    private ServiceLocator() {
        initializeServices();
    }

    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }

    private void initializeServices() {
        logger.info("Initializing application services with refactored architecture...");

        // Initialize core services first
        if (dataManager == null) {
            dataManager = RefactoredDataManager.getInstance();
            registerService(RefactoredDataManager.class, dataManager);
        }

        // Initialize repositories with concrete types
        InMemoryUserRepository userRepository = (InMemoryUserRepository) dataManager.getUserRepository();
        InMemoryMovieRepository movieRepository = (InMemoryMovieRepository) dataManager.getMovieRepository();

        // Initialize services
        ContentService<com.papel.imdb_clone.model.Series> seriesService = new ContentService<>(com.papel.imdb_clone.model.Series.class);
        CelebrityService<com.papel.imdb_clone.model.Actor> actorService = new CelebrityService<>(com.papel.imdb_clone.model.Actor.class);
        CelebrityService<com.papel.imdb_clone.model.Director> directorService = new CelebrityService<>(com.papel.imdb_clone.model.Director.class);

        // Initialize data loader factory with concrete repository types
        dataLoaderFactory = new DataLoaderFactory(
                userRepository,
                movieRepository,
                seriesService,
                actorService,
                directorService
        );
        registerService(DataLoaderFactory.class, dataLoaderFactory);

        // Initialize file data loader service with concrete repository types
        fileDataLoaderService = new FileDataLoaderService(
                userRepository,
                movieRepository,
                seriesService,
                actorService,
                directorService
        );
        registerService(FileDataLoaderService.class, fileDataLoaderService);

        // Initialize search service
        SearchService searchService = new SearchService(dataManager);
        registerService(SearchService.class, searchService);

        logger.info("Service initialization complete with {} services registered", services.size());
    }

    /**
     * Gets the RefactoredDataManager instance.
     *
     * @return The RefactoredDataManager instance
     */
    public RefactoredDataManager getDataManager() {
        if (dataManager == null) {
            dataManager = RefactoredDataManager.getInstance();
            registerService(RefactoredDataManager.class, dataManager);
        }
        return dataManager;
    }

    /**
     * Gets the FileDataLoaderService instance.
     *
     * @return The FileDataLoaderService instance
     */
    public FileDataLoaderService getFileDataLoaderService() {
        if (fileDataLoaderService == null) {
            throw new IllegalStateException("FileDataLoaderService not initialized. Call initializeServices() first.");
        }
        return fileDataLoaderService;
    }

    /**
     * Gets the DataLoaderFactory instance.
     *
     * @return The DataLoaderFactory instance
     */
    public DataLoaderFactory getDataLoaderFactory() {
        if (dataLoaderFactory == null) {
            throw new IllegalStateException("DataLoaderFactory not initialized. Call initializeServices() first.");
        }
        return dataLoaderFactory;
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