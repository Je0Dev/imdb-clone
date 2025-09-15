package com.papel.imdb_clone.service;

import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.model.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles loading and initializing content data from various sources.
 * Supports both synchronous and asynchronous loading of content.
 */
public class ContentDataLoader<T extends Content> {
    private static final Logger logger = LoggerFactory.getLogger(ContentDataLoader.class);
    private static ContentDataLoader instance;

    private final RefactoredDataManager dataManager;
    private final ExecutorService executorService;

    /**
     * Constructor for ContentDataLoader.
     * @param dataManager
     */
    public ContentDataLoader(RefactoredDataManager dataManager) {
        this.dataManager = dataManager;
        /**
         * Creates a fixed thread pool with the number of available processors.
         * Each thread is set as a daemon thread and has a custom name.
         */
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.setName("content-loader-" + t.getId());
                    return t;
                }
        );
    }

    /**
     * Returns the singleton instance of ContentDataLoader.
     * @param dataManager
     * @return ContentDataLoader instance which means the same instance is returned every time.
     * Instance is created only once and reused and it means that the same instance is returned every time.
     * This is a thread-safe implementation.
     */
    public static synchronized ContentDataLoader getInstance(RefactoredDataManager dataManager) {
        if (instance == null) {
            instance = new ContentDataLoader(dataManager);
        }
        return instance;
    }
}
