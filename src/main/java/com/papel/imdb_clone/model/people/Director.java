package com.papel.imdb_clone.model.people;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.service.people.CelebrityManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a director of a movie or TV show.
 * Uses CelebrityManager to prevent duplicate instances.
 */
public class Director extends Celebrity {
    @SuppressWarnings("unchecked")
    private static final CelebrityManager<Director> celebrityManager = CelebrityManager.getInstance(Director.class);
    
    private final List<String> bestWorks;
    private final Ethnicity ethnicity;
    
    /**
     * Protected constructor to enforce use of factory methods.
     */
    protected Director(String firstName, String lastName, LocalDate birthDate, char gender, Ethnicity ethnicity) {
        super(firstName, lastName, birthDate, gender);
        this.bestWorks = new ArrayList<>();
        this.ethnicity = ethnicity;
    }
    
    /**
     * Factory method to get or create a Director instance.
     * @param firstName First name of the director
     * @param lastName Last name of the director
     * @param birthDate Birth date (can be null)
     * @param gender Gender (M/F/other)
     * @return Existing or new Director instance
     */
    public static Director getInstance(String firstName, String lastName, LocalDate birthDate, char gender) {
        return getInstance(firstName, lastName, birthDate, gender, null);
    }
    
    /**
     * Factory method with ethnicity.
     */
    public static Director getInstance(String firstName, String lastName, LocalDate birthDate, 
                                     char gender, Ethnicity ethnicity) {
        if (firstName == null) firstName = "";
        if (lastName == null) lastName = "";
        
        Director temp = new Director(firstName.trim(), lastName.trim(), birthDate, gender, ethnicity);
        return getInstance(temp);
    }
    
    /**
     * Internal factory method that handles the actual instance creation/lookup.
     */
    private static Director getInstance(Director director) {
        return celebrityManager.findCelebrity(director).orElseGet(() -> 
            celebrityManager.addCelebrity(director)
        );
    }

    //getters
    public List<String> getBestWorks() {
        return new ArrayList<>(bestWorks);
    }

    public Ethnicity getEthnicity() {
        return ethnicity;
    }

    /**
     * Gets the notable works of the director.
     * First checks the bestWorks list, then falls back to the parent's notableWorks.
     *
     * @return A list of notable works, or an empty list if not set
     */
    @Override
    public List<String> getNotableWorks() {
        // First, check if we have any works in bestWorks
        if (bestWorks != null && !bestWorks.isEmpty()) {
            return new ArrayList<>(bestWorks);
        }
        
        // Fall back to parent's notableWorks
        return super.getNotableWorks();
    }

    @Override
    public void setNotableWorks(String notableWorks) {
        this.bestWorks.clear();
        if (notableWorks != null && !notableWorks.trim().isEmpty()) {
            String[] works = notableWorks.split(",");
            for (String work : works) {
                String trimmedWork = work.trim();
                if (!trimmedWork.isEmpty()) {
                    this.bestWorks.add(trimmedWork);
                }
            }
        }
        // Also update the parent's notableWorks for consistency
        super.setNotableWorks(notableWorks);
    }
    
    @Override
    public void setNotableWorks(List<String> works) {
        this.bestWorks.clear();
        if (works != null) {
            this.bestWorks.addAll(works.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList()));
        }
        // Also update the parent's notableWorks for consistency
        super.setNotableWorks(works);
    }

    /**
     * Gets the nationality/ethnicity as a string.
     *
     * @return The ethnicity label or null if not set
     */
    public Ethnicity getNationality() {
        return ethnicity != null ? Ethnicity.valueOf(ethnicity.getLabel()) : null;
    }

    /**
     * Gets a formatted string with the director's full name, birth year, and notable works.
     * @return Formatted string with director information
     */
    public String getFormattedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><b>").append(getFullName()).append("</b>");
        
        // Add birth year if available
        if (getBirthDate() != null) {
            sb.append("<br>Born: ").append(getBirthDate());
        }
        
        // Add gender if available
        if (getGender() != '\u0000') {
            String genderStr = "";
            if (getGender() == 'M' || getGender() == 'm') {
                genderStr = "Male";
            } else if (getGender() == 'F' || getGender() == 'f') {
                genderStr = "Female";
            } else if (getGender() == 'U') {
                genderStr = "Unknown";
            } else {
                genderStr = String.valueOf(getGender());
            }
            //Add gender to string
            sb.append("<br>Gender: ").append(genderStr);
        }
        
        // Add nationality/ethnicity if available
        if (getEthnicity() != null) {
            sb.append("<br>Nationality: ").append(getEthnicity().getLabel());
        }
        
        // Add notable works if available
        List<String> works = getNotableWorks();
        if (!works.isEmpty()) {
            //Add notable works to string
            sb.append("<br><br><b>Notable Works:</b><br>");
            for (int i = 0; i < Math.min(5, works.size()); i++) {
                sb.append("• ").append(works.get(i));
                if (i < Math.min(5, works.size()) - 1) {
                    sb.append("<br>");
                }
            }
            if (works.size() > 5) {
                //Add more works to string
                sb.append("<br>... and ").append(works.size() - 5).append(" more");
            }
        }
        
        sb.append("</html>");
        return sb.toString();
    }

    @Override
    public String toString() {
        return getFullName();
    }

    //setters
    public void setId(int id) {
        this.id = id;
    }


    //set first and last name
    @Override
    public void setFirstName(String firstName) {
        super.setFirstName(firstName);
    }

    @Override
    public void setLastName(String lastName) {
        super.setLastName(lastName);
    }
    
    public void addBestWork(String work) {
        if (work != null && !work.trim().isEmpty()) {
            bestWorks.add(work.trim());
        }
    }
}