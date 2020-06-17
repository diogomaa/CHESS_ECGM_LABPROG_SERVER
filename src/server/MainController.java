package server;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable, Server.OnDataReceiveListener{
    @FXML
    private TextArea log;  //registo do historico de texto
    private Server server = null;

    public MainController(Server server) {
        this.server = server;
    }

    @Override
    //inicializa o main controller 
    public void initialize(URL location, ResourceBundle resources) {
        log.setEditable(false);
        server.setOnDataReceiveListener(this);
    }

    @Override
    public void onDataReceive(String message) {
        log.appendText(message + "\n");  //recebe a informaçao e apresenta
    }
}
