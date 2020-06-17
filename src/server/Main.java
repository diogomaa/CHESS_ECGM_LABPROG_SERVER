package server;

import java.awt.Toolkit;
import java.util.Optional;
import server.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Main extends Application implements Server.OnConnectionEstablishListener{
    Stage primaryStage = null;
   
    Server server = new Server();  //criar o servidorr
    @Override
    //iniciar servidor
    public void start(Stage primaryStage) throws Exception {                                                                                        
        this.primaryStage = primaryStage;
        
        server.setOnConnectionEstablishListener(this);  // alteral o valor da variavel setOnConnectionEstablishListener/conexção estabelecida que foi passado pelo parametro

        FXMLLoader loader = new FXMLLoader(getClass().getResource("loading_screen.fxml"));//screen inicial esperando os jogadores

        Parent root = loader.load();                                                                                                              
        primaryStage.setTitle("Jogo Xadrez - Server");
        primaryStage.setScene(new Scene(root));
        
        primaryStage.setResizable(false);
        
        
        
       primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
    @Override
    // pop-up de quando se fecha a janela do servidor
    public void handle(WindowEvent event) {
        System.out.println("hidding");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Desligar o servidor!");
        alert.setHeaderText("Deseja mesmo desligar o servidor??");                                                                                  
        alert.setContentText("Se sim carregue em SIM, caso pretenda continuar NÃO...Avisamos que os jogos abertos ficaram Desconectados...");            
        Toolkit.getDefaultToolkit().beep();
  
        
        //opções sim ou nao da janela pop-up  
        ButtonType buttonTypeOne = new ButtonType("SIM");                                                                                           
        ButtonType buttonTypeTwo = new ButtonType("NÃO");
 
                    

        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);
                         
        Optional<ButtonType> result = alert.showAndWait();
        // verificar opção selecionada e se sim fechar a janela do servidor         
        if ( result.get() == buttonTypeOne ) {
         
                 server.sendChatData("Desculpe! Servidor Caiu!" ); // caso o jogador esteja em jogo e fechar a janela
                 Platform.exit();                                                                                                                 
                 System.exit(0);                                                                                                                  
            
           
              
        }else if (result.get() == buttonTypeTwo){
      
        }
         event.consume();
    }});
       
        primaryStage.show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                server.start();
            }
        });
        t.start(); //inicia o server thread
    }

    @Override
    public void onConnectionEstablish() { // quando a coxeção é estabelecida iniciar o ecra principal do servidor
        try {
            startMainScreen(primaryStage);                                                                                                      
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
     //apresentar o ecra principal apos a conecção é estabelicida
    private void startMainScreen(Stage stage) throws Exception {
        MainController controller = new MainController(server);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main_screen.fxml"));                                                        
        loader.setController(controller);                                                                                                       

        Parent root = loader.load();
        stage.getScene().setRoot(root);
        stage.show();
        stage.sizeToScene();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
