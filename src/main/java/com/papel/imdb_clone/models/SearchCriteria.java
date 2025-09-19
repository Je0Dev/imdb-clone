package com.papel.imdb_clone.models;

import java.util.List;

public class SearchCriteria {
    private String title;
    private String director;
    private List<String> cast;
    private List<String> keywords;
    private Integer yearFrom;
    private Integer yearTo;
    private double minRating;
    private ContentType contentType;
    private List<String> genres;
    private Integer minRuntime;
    private Integer maxRuntime;
    private boolean oscarWinner;
    private boolean goldenGlobeWinner;
    private boolean emmyWinner;
    private boolean cannesWinner;
    private boolean hasPoster;
    private boolean hasTrailer;
    private boolean includeAdultContent;
    private String sortBy;
    private String sortOrder;
    private int pageSize;
    private int pageNumber;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public List<String> getCast() { return cast; }
    public void setCast(List<String> cast) { this.cast = cast; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public Integer getYearFrom() { return yearFrom; }
    public void setYearFrom(Integer yearFrom) { this.yearFrom = yearFrom; }

    public Integer getYearTo() { return yearTo; }
    public void setYearTo(Integer yearTo) { this.yearTo = yearTo; }

    public double getMinRating() { return minRating; }
    public void setMinRating(double minRating) { this.minRating = minRating; }

    public ContentType getContentType() { return contentType; }
    public void setContentType(ContentType contentType) { this.contentType = contentType; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public Integer getMinRuntime() { return minRuntime; }
    public void setMinRuntime(Integer minRuntime) { this.minRuntime = minRuntime; }

    public Integer getMaxRuntime() { return maxRuntime; }
    public void setMaxRuntime(Integer maxRuntime) { this.maxRuntime = maxRuntime; }

    public boolean isOscarWinner() { return oscarWinner; }
    public void setOscarWinner(boolean oscarWinner) { this.oscarWinner = oscarWinner; }

    public boolean isGoldenGlobeWinner() { return goldenGlobeWinner; }
    public void setGoldenGlobeWinner(boolean goldenGlobeWinner) { this.goldenGlobeWinner = goldenGlobeWinner; }

    public boolean isEmmyWinner() { return emmyWinner; }
    public void setEmmyWinner(boolean emmyWinner) { this.emmyWinner = emmyWinner; }

    public boolean isCannesWinner() { return cannesWinner; }
    public void setCannesWinner(boolean cannesWinner) { this.cannesWinner = cannesWinner; }

    public boolean isHasPoster() { return hasPoster; }
    public void setHasPoster(boolean hasPoster) { this.hasPoster = hasPoster; }

    public boolean isHasTrailer() { return hasTrailer; }
    public void setHasTrailer(boolean hasTrailer) { this.hasTrailer = hasTrailer; }

    public boolean isIncludeAdultContent() { return includeAdultContent; }
    public void setIncludeAdultContent(boolean includeAdultContent) { this.includeAdultContent = includeAdultContent; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    @Override
    public String toString() {
        return "SearchCriteria{" +
                "title='" + title + '\'' +
                ", director='" + director + '\'' +
                ", cast=" + cast +
                ", keywords=" + keywords +
                ", yearFrom=" + yearFrom +
                ", yearTo=" + yearTo +
                ", minRating=" + minRating +
                ", contentType=" + contentType +
                ", genres=" + genres +
                ", minRuntime=" + minRuntime +
                ", maxRuntime=" + maxRuntime +
                ", oscarWinner=" + oscarWinner +
                ", goldenGlobeWinner=" + goldenGlobeWinner +
                ", emmyWinner=" + emmyWinner +
                ", cannesWinner=" + cannesWinner +
                ", hasPoster=" + hasPoster +
                ", hasTrailer=" + hasTrailer +
                ", includeAdultContent=" + includeAdultContent +
                ", sortBy='" + sortBy + '\'' +
                ", sortOrder='" + sortOrder + '\'' +
                ", pageSize=" + pageSize +
                ", pageNumber=" + pageNumber +
                '}';
    }
}
