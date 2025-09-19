package com.papel.imdb_clone.controllers.content;

// Class to hold content item data
    public class ContentItem {
        private final String id;
        private final String title;
        private final String year;
        private final String type;
        private final double rating;

        public ContentItem(String id, String title, String year, String type, double rating) {
            this.id = id;
            this.title = title;
            this.year = year;
            this.type = type;
            this.rating = rating;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getYear() { return year; }
        public String getType() { return type; }
        public double getRating() { return rating; }

        @Override
        public String toString() {
            return String.format("%s (%s) - %s - %.1f", title, year, type, rating);
        }
    }
