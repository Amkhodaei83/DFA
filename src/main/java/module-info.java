module com.example.dfa_app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.logging;
    requires java.desktop;

    opens com.example.dfa_app to javafx.fxml;
    exports com.example.dfa_app;
    exports com.example.dfa_app.DFA;
    opens com.example.dfa_app.DFA to javafx.fxml;
}