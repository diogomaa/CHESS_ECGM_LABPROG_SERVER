package server;


import javafx.application.Platform;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;


public class Server {
    private ArrayList<Socket> clientSockets = new ArrayList<>(); //array com 
    private ArrayList<PrintWriter> clientWriters = new ArrayList<>();
    private ServerSocket serverSocket;
    private ArrayList<String> playerNames = new ArrayList<>(); //array com os nomes dos jogadores

    private OnDataReceiveListener dataListener = null;
    private OnConnectionEstablishListener connectionListener = null;

    //iniciação do server
    public void start() {
        try {
            serverSocket = new ServerSocket(5000);//porta em que vai usar para comunicar
            
            for (int i = 1; i <= 2; i++) {
                Socket socket = serverSocket.accept();
                clientSockets.add(socket);
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                clientWriters.add(writer);

                Thread t = new Thread(new ConnectionHandler(socket));
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //verifica se recebe informaçao
    public void setOnDataReceiveListener(OnDataReceiveListener listener) { 
        this.dataListener = listener;                                                               
    }
    //verifica de conecçao foi estabelicida
    public void setOnConnectionEstablishListener(OnConnectionEstablishListener listener) {
        this.connectionListener = listener;                                                         
    }
    //????
    private void startDataThread() {
        for (Socket socket : clientSockets) {
            Thread t = new Thread(new DataHandler(socket));
            t.start();
        }
    }
     //envia as mensagens do jogo, empate, vitoria, chat, etc etc
    public void sendChatData(String message) {
        Iterator it = clientWriters.iterator();                                                    
        while (it.hasNext()) {
            try {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(message);
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //envia a infotmação de cada jogador 
    private void sendPlayerInfo() {
        clientWriters.get(0).println("1" + "_" + playerNames.get(0) + "_" + playerNames.get(1)); //id_playerinimigo_playername
        clientWriters.get(0).flush();
        clientWriters.get(1).println("2" + "_" + playerNames.get(1) + "_" + playerNames.get(0)); //id_playername_playerinimigo   
        clientWriters.get(1).flush();
    }
    //passar a informaçao de qual foi o movimento executado por algum dos jogadores
    private void sendMoveInfo(String message) {
        String[] s = message.split("_"); //separa a mensagem
        String data = message.substring(2);
        if (s[0].equals("1")) {
            clientWriters.get(1).println(data);    //envia a string com a mensagem do movimento                                                         
            clientWriters.get(1).flush();
        } else if (s[0].equals("2")) {
            clientWriters.get(0).println(data);
            clientWriters.get(0).flush();
        }
    }
    //actualizar o registo das jogadas
    private void updateLog(String message) {
        Platform.runLater(new Runnable() {
            @Override                                                                                       
            public void run() {
                dataListener.onDataReceive(message);
            }
        });
    }
    //gerente de conecção
    private class ConnectionHandler implements Runnable {                                                  
        BufferedReader reader;                                                                            
        Socket socket;
        //????
        public ConnectionHandler(Socket clientSocket) {
            try {
                socket = clientSocket;
                InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
                reader = new BufferedReader(isReader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //inicia a coneçao entre os jogadores e assim começa a permitir enviar e receber a informaçao de ambos
        @Override
        public void run() {
            String playerName;
            try {
                if ((playerName = reader.readLine()) != null) {
                    playerNames.add(playerName);
                    if (playerNames.size() == 2) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                connectionListener.onConnectionEstablish();                                                 
                            }                                                                                               
                        });
                        updateLog("Jogador " + playerNames.get(0) + " conectado!");
                        updateLog("Jogador " + playerNames.get(1) + " conectado!");
                        sendPlayerInfo();
                        startDataThread();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    //Manipulador de dados
    private class DataHandler implements Runnable {
        Socket socket;
        BufferedReader reader; 
        
        public DataHandler(Socket socket) {
            try {
                this.socket = socket;
                InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
                reader = new BufferedReader(isReader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        //diferentes mensagens conforme o que acontece no jogo se decidiu jogar, desisistir, se perdeou ou venceu etc...
        // ESTAS MENSAGENS SÃO TANTO IMPRIMIDAS NO SERVER COMO ENTRE JOGADORES  
        public void run() {
            String message;
            try {                                                                                                       
                while ((message = reader.readLine()) != null) {
                    if (message.contains("_")) {
                        String name = (message.split("_")[0].equals("1")) ? playerNames.get(0) : playerNames.get(1);
                        String data = "Jogador " + name + " fez um movimento...";
                        updateLog(data);
                        sendMoveInfo(message);
                    } else if(message.contains("desistiu!")) {
                        String data = "Um dos Jogadores desistiu...";
                        updateLog(data);
                        sendChatData(message);
                    }else if(message.contains("Troca peão por outra peça.")) {
                        String data = "Troca de Peão por peça...";
                        updateLog(data);
                        sendChatData(message);
                    }else if(message.contains("Pedido de Empate")) {
                        String data = "Pedido de Empate por jogador...";
                        updateLog(data);
                        sendChatData(message);
                    }else if(message.contains(" aceitou o Empate!")) {
                        String data = "Jogador aceitou o Empate...";
                        updateLog(data);
                        sendChatData(message);
                    }else if(message.contains("REJEITADO!")) {
                        String data = "Jogador Rejeitou empate...";
                        updateLog(data);
                        sendChatData(message);    
                    }else if(message.contains("Adversário ganhou o jogo!")) {
                        String data = "Fim do jogo, Xeque-Mate...";
                        updateLog(data);
                        sendChatData(message);        
                    }else {
                        String data = "Mensagens enviadas...";
                        updateLog(data);
                        sendChatData(message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    
    interface OnDataReceiveListener {
        void onDataReceive(String message);
    }

    interface OnConnectionEstablishListener {
        void onConnectionEstablish();
    }
}
