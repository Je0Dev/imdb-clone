package com.papel.imdb_clone.service;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.Series;
import com.papel.imdb_clone.model.Actor;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

/**
 * Service class for managing Series content.
 */
public class SeriesService extends BaseContentService<Series> {
    private static final Logger logger = Logger.getLogger(SeriesService.class.getName());
    private static SeriesService instance;
    
    private SeriesService() {
        super(Series.class);
    }
    
    public static synchronized SeriesService getInstance() {
        if (instance == null) {
            instance = new SeriesService();
        }
        return instance;
    }

    @Override
    protected void initializeSampleData() {
        lock.writeLock().lock();
        try {
            // Clear existing data
            contentList.clear();
            
            // Series 1 - Breaking Bad
            List<Genre> breakingBadGenres = new ArrayList<>();
            breakingBadGenres.add(Genre.CRIME);
            breakingBadGenres.add(Genre.DRAMA);
            breakingBadGenres.add(Genre.THRILLER);
            
            Series series1 = new Series(
                "Breaking Bad",
                Genre.CRIME,
                9.5,
                9.5,
                2008
            );
            series1.setDirector("Vince Gilligan");
            series1.setGenres(breakingBadGenres);
            
            List<Actor> breakingBadActors = new ArrayList<>();
            breakingBadActors.add(new Actor("Bryan", "Cranston", LocalDate.of(1956, 3, 7), 'M', "White"));
            breakingBadActors.add(new Actor("Aaron", "Paul", LocalDate.of(1979, 8, 27), 'M', "White"));
            series1.setActors(breakingBadActors);
            series1.setEndYear(2013);
            save(series1);
            
            // Series 2 - Game of Thrones
            List<Genre> gotGenres = new ArrayList<>();
            gotGenres.add(Genre.FANTASY);
            gotGenres.add(Genre.DRAMA);
            gotGenres.add(Genre.ADVENTURE);
            
            Series series2 = new Series(
                "Game of Thrones",
                Genre.FANTASY,
                9.3,
                9.0,
                2011
            );
            series2.setDirector("David Benioff, D.B. Weiss");
            series2.setGenres(gotGenres);
            series2.getGenres().add(Genre.ADVENTURE);
            series2.getActors().add(new Actor("Emilia", "Clarke", LocalDate.of(1986, 10, 23), 'F', "White"));
            series2.getActors().add(new Actor("Kit", "Harington", LocalDate.of(1986, 12, 26), 'M', "White"));
            series2.getActors().add(new Actor("Peter", "Dinklage", LocalDate.of(1969, 6, 11), 'M', "White"));
            series2.setEndYear(2019);
            save(series2);
            
            // Series 3 - Stranger Things
            Series series3 = new Series("Stranger Things", Genre.SCI_FI, 8.7, 8.7, 2016);
            series3.setDirector("The Duffer Brothers");
            series3.getGenres().add(Genre.SCI_FI);
            series3.getGenres().add(Genre.HORROR);
            series3.getGenres().add(Genre.MYSTERY);
            series3.getActors().add(new Actor("Millie Bobby", "Brown", LocalDate.of(2004, 2, 19), 'F', "White"));
            series3.getActors().add(new Actor("Finn", "Wolfhard", LocalDate.of(2002, 12, 23), 'M', "White"));
            series3.setEndYear(2025);
            save(series3);
            
            logger.info("Initialized with " + contentList.size() + " sample series");
        } finally {
            lock.writeLock().unlock();
        }
    }
}
