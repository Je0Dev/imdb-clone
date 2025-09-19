package com.papel.imdb_clone.service.content;

import com.papel.imdb_clone.model.content.Content;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base abstract class for content services providing common CRUD operations.
 * @param <T> The type of content this service manages, must extend Content
 */
public abstract class BaseContentService<T extends Content> implements ContentService<T> {
    protected final List<T> contentList = new ArrayList<>();
    protected final AtomicInteger nextId = new AtomicInteger(1);
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    protected final Class<T> contentType;

    protected BaseContentService(Class<T> contentType) {
        this.contentType = contentType;
        initializeSampleData();
    }

    @Override
    public List<T> getAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(contentList);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<T> getById(int id) {
        lock.readLock().lock();
        try {
            return contentList.stream()
                    .filter(content -> content.getId() == id)
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public T save(T content) {
        lock.writeLock().lock();
        try {
            content.setId(nextId.getAndIncrement());
            contentList.add(content);
            return content;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public T update(T content) {
        lock.writeLock().lock();
        try {
            int index = -1;
            for (int i = 0; i < contentList.size(); i++) {
                if (contentList.get(i).getId() == content.getId()) {
                    index = i;
                    break;
                }
            }
            
            if (index != -1) {
                contentList.set(index, content);
                return content;
            }
            throw new NoSuchElementException("Content with id " + content.getId() + " not found");
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean delete(int id) {
        lock.writeLock().lock();
        try {
            return contentList.removeIf(content -> content.getId() == id);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean remove(T content) {
        if (content == null) {
            return false;
        }
        return delete(content.getId());
    }

    @Override
    public Optional<T> findByTitleAndYear(String title, int year) {
        lock.readLock().lock();
        try {
            return contentList.stream()
                    .filter(content -> content.getTitle().equalsIgnoreCase(title) && 
                                     content.getReleaseDate().getYear() == year)
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String getContentType() {
        return contentType.getSimpleName();
    }

    /**
     * Initialize sample data for the service.
     * This method should be implemented by concrete classes to provide their own sample data.
     */
    protected abstract void initializeSampleData();
}
