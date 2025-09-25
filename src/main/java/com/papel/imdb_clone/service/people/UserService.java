package com.papel.imdb_clone.service.people;

import com.papel.imdb_clone.data.DataManager;
import com.papel.imdb_clone.model.people.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for user-related operations including authentication, registration,
 * and profile management.
 */
public class UserService {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static UserService instance;
    private final DataManager dataManager;


    private User currentUser;
    private User user;
    private boolean authenticated;

    /**
     * Private constructor for singleton pattern.
     *
     * @param dataManager  The data manager instance
     */
    private UserService(DataManager dataManager) {
        this.dataManager = dataManager;
    }


    /**
     * @param dataManager The data manager instance
     * @return The instance
     * @throws IllegalArgumentException if the data manager is null
     */
    public static synchronized UserService getInstance(DataManager dataManager) throws IllegalArgumentException {
      try{
          // Check if instance is already created
        if (instance == null) {
            instance = new UserService(dataManager);
        }
        return instance;
      } catch (Exception e) {
        logger.error("Error getting instance of UserService", e);
        throw new IllegalArgumentException("Error getting instance of UserService", e);
      }
    }

}