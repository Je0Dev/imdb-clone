module com.papel.imdb_clone {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires org.slf4j;
    requires static org.slf4j.simple;
    requires java.logging;
    requires java.net.http;
    requires java.desktop;
    requires java.sql;

    opens com.papel.imdb_clone to javafx.fxml;
    opens com.papel.imdb_clone.controllers to javafx.fxml;
    opens com.papel.imdb_clone.gui to javafx.fxml;
    exports com.papel.imdb_clone;
    exports com.papel.imdb_clone.controllers;
    exports com.papel.imdb_clone.gui;
    exports com.papel.imdb_clone.model.content;
    opens com.papel.imdb_clone.model.content to javafx.base, javafx.fxml;
    exports com.papel.imdb_clone.model.people;
    opens com.papel.imdb_clone.model.people to javafx.base, javafx.fxml;
    exports com.papel.imdb_clone.model.rating;
    opens com.papel.imdb_clone.model.rating to javafx.base, javafx.fxml;
    exports com.papel.imdb_clone.controllers.people;
    opens com.papel.imdb_clone.controllers.people to javafx.fxml;
    exports com.papel.imdb_clone.controllers.content;
    opens com.papel.imdb_clone.controllers.content to javafx.fxml;
    exports com.papel.imdb_clone.controllers.authentication;
    opens com.papel.imdb_clone.controllers.authentication to javafx.fxml;
    exports com.papel.imdb_clone.controllers.base;
    opens com.papel.imdb_clone.controllers.base to javafx.fxml;
    exports com.papel.imdb_clone.controllers.search;
    opens com.papel.imdb_clone.controllers.search to javafx.fxml;
}