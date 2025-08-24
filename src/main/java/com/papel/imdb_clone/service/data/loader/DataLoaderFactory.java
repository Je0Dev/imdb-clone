package com.papel.imdb_clone.service.data.loader;

import com.papel.imdb_clone.repository.impl.InMemoryMovieRepository;
import com.papel.imdb_clone.repository.impl.InMemoryUserRepository;
import com.papel.imdb_clone.service.CelebrityService;
import com.papel.imdb_clone.service.ContentService;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating and managing data loaders.
 */
public class DataLoaderFactory {
    private final Map<Class<?>, Object> loaders = new HashMap<>();
    private final InMemoryUserRepository userRepository;
    private final InMemoryMovieRepository movieRepository;
    private final ContentService<com.papel.imdb_clone.model.Series> seriesService;
    private final CelebrityService<com.papel.imdb_clone.model.Actor> actorService;
    private final CelebrityService<com.papel.imdb_clone.model.Director> directorService;

    /**
     * Creates a new DataLoaderFactory with the required dependencies.
     *
     * @param userRepository the user repository
     * @param movieRepository the movie repository
     * @param seriesService the series service
     * @param actorService the actor service
     * @param directorService the director service
     */
    public DataLoaderFactory(
            InMemoryUserRepository userRepository,
            InMemoryMovieRepository movieRepository,
            ContentService<com.papel.imdb_clone.model.Series> seriesService,
            CelebrityService<com.papel.imdb_clone.model.Actor> actorService,
            CelebrityService<com.papel.imdb_clone.model.Director> directorService) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.seriesService = seriesService;
        this.actorService = actorService;
        this.directorService = directorService;
    }

    /**
     * Gets a loader instance of the specified type.
     *
     * @param <T> the type of loader to get
     * @param loaderClass the class of the loader to get
     * @return the loader instance
     * @throws IllegalArgumentException if the loader type is not supported
     */
    @SuppressWarnings("unchecked")
    public <T> T getLoader(Class<T> loaderClass) {
        if (!loaders.containsKey(loaderClass)) {
            initializeLoader(loaderClass);
        }
        return (T) loaders.get(loaderClass);
    }

    /**
     * Initializes a loader of the specified type.
     *
     * @param loaderClass the class of the loader to initialize
     */
    private void initializeLoader(Class<?> loaderClass) {
        if (loaderClass.equals(ActorDataLoader.class)) {
            loaders.put(loaderClass, new ActorDataLoader(actorService));
        } else if (loaderClass.equals(DirectorDataLoader.class)) {
            loaders.put(loaderClass, new DirectorDataLoader(directorService));
        } else if (loaderClass.equals(MovieDataLoader.class)) {
            loaders.put(loaderClass, new MovieDataLoader(movieRepository, actorService, directorService));
        } else if (loaderClass.equals(SeriesDataLoader.class)) {
            loaders.put(loaderClass, new SeriesDataLoader(seriesService, actorService, directorService));
        } else if (loaderClass.equals(UserDataLoader.class)) {
            loaders.put(loaderClass, new UserDataLoader(userRepository));
        } else if (loaderClass.equals(AwardsDataLoader.class)) {
            loaders.put(loaderClass, new AwardsDataLoader(movieRepository, seriesService));
        } else {
            throw new IllegalArgumentException("Unsupported loader type: " + loaderClass.getName());
        }
    }
}
