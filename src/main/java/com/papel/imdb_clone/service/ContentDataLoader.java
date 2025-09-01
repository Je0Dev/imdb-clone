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

    public ContentDataLoader(RefactoredDataManager dataManager) {
        this.dataManager = dataManager;
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

    public static synchronized ContentDataLoader getInstance(RefactoredDataManager dataManager) {
        if (instance == null) {
            instance = new ContentDataLoader(dataManager);
        }
        return instance;
    }
}
