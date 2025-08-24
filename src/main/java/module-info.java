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
    opens com.papel.imdb_clone.model to javafx.base, javafx.fxml;
    opens com.papel.imdb_clone.gui to javafx.fxml;
    exports com.papel.imdb_clone;
    exports com.papel.imdb_clone.controllers;
    exports com.papel.imdb_clone.model;
    exports com.papel.imdb_clone.gui;
}