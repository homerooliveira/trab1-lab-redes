package com.company;

import com.company.Model.GameConstants;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Implementacao do cliente
 */
public class Client {
    boolean debug = false;

    private char [] board;

    private char playerType;
    private char otherPlayerType;

    int lastPlay;
    private final int port = 9000;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    Scanner keyboard = new Scanner(System.in);
    boolean ok = false;

    private boolean shouldListenServer = false;

    Gson gson = new Gson();

    public static void main(String args[]){
        String server = "localhost";
        try {
            while (true){
                // Enquanto o jogador quiser jogar conecta-se ao servidor
                if(!WantToPlay()) { // Pergunta ao jogador se quer jogar
                    System.out.println("GOOD BYE...\nSee you later alligator");
                    break;
                }
                //Conecta-se ao servidor através de um socket tcp
                Client client = new Client(server);
                //Busca/Cria uma partida
                client.browseGames();
                //Inicia o Jogo
                client.play();
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Connection Problem");
        }

    }

    /**
     * Construtor da classe cliente que recebe apenas a localizaćao do servidor
     * @param server
     * @throws IOException
     */
    public Client(String server) throws IOException {
        this.socket = new Socket(server, this.port);
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        this.board = new char[9];
    }

    private void browseGames() throws IOException {

        shouldListenServer = true;
        ok = false;

        /**
         * Thread que é responsável por escutar os inputs do jogador
         */
        Thread inputListener = new Thread(()->{
            String opt;
            do {
                do {
                    System.out.println("Do you want to enter a existing game? press 1 - yes\n" +
                            "Else if you want to create a game press 2");
                    opt = keyboard.next().trim();

                } while (!opt.equals("1")  && !opt.equals("2"));
                switch (opt) {
                    case "1": // Enter an existing game
                        System.out.println("Which match do you want to join?");
                        opt = keyboard.next().trim();
                        out.println(GameConstants.JOIN_GAME + opt);
                        break;
                    case "2": // Create a game
                        System.out.println("Choose the name of the match?");
                        opt = keyboard.next().trim();
                        out.println(GameConstants.CREATE_GAME + opt);
                        break;
                }
                try {
                    synchronized (this){
                        wait(100000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (!ok);
        });
        inputListener.start();
        String serverMsg, message, code;

        //Tread responsável por escutar o servidor
        do {
            serverMsg= in.readLine();
            print("listening " + serverMsg);
            if(serverMsg.length() <3) continue;
            code = serverMsg.substring(0,4);
            switch (code){
                case GameConstants.CREATE_GAME_OK:
                    System.out.println("GAME CREATED");
                    ok = true;
                    this.setPlayerType('X');
                    shouldListenServer = false;
                    break;
                case GameConstants.CREATE_GAME_ERROR:
                    System.out.println("ERROR try again");
                    break;
                case GameConstants.JOIN_GAME_OK:
                    System.out.println("JOINED GAME");
                    ok = true;
                    this.setPlayerType('O');
                    shouldListenServer = false;
                    break;
                case GameConstants.JOIN_GAME_ERROR:
                    System.out.println("ERROR try again");
                    break;
                case GameConstants.LIST_GAME:
                    message = serverMsg.substring(4);
                    String[] matchs = gson.fromJson(message, String[].class);
                    System.out.println("-> SALAS DISPONÍVEIS:");
                    for (String k: matchs) {
                        System.out.println(k);
                    }
                    break;
            }
            synchronized (this){
                //Sincroniza as duas threads e avisa caso nao seja mais necessário escutar inputs do usuário
                notify();
            }
        } while (shouldListenServer);
        try {
            inputListener.join();
        } catch (InterruptedException ignored) {}

    }

    /**
     *  Seta se o jogador é X ou O
     * @param type
     */
    private void setPlayerType(char type) {
        this.playerType = type;
        this.otherPlayerType = type =='X' ? 'O' : 'X';
    }

    /**
     * Print para debug
     * @param serverMsg
     */
    private void print(String serverMsg) {
        if(debug == true)
            System.out.println("* DEBUG | " + serverMsg);
    }

    /**
     *     Questiona se o jogador quer jogar
     */
    private static boolean WantToPlay() {
        Scanner keyboard = new Scanner(System.in);
        while (true) {
            System.out.println("Do you want to play?\n 1 - yes\n  - or -\n 2 - no");
            String input = keyboard.next();
            switch (input.trim().charAt(0)){
                case '1':
                    return true;
                case '2':
                    return false;
                default:
                    System.out.println("Error");
            }
        }

    }


    /**
     * Lógica do lado do cliente sobre a partida, apresenta comunicaćão com o servidor
     * @throws IOException
     */
    public void play() throws IOException {
        String response = in.readLine();
        String code;
        String rest;
        boolean endGame = false;
        //Recebe a primeira mensagem do servidor dizedo que a partida se iniciará
        if(response.startsWith("WELCOME")){
            this.playerType = response.charAt(8);
            this.otherPlayerType = this.playerType == 'X' ? 'O' : 'X';
        }

        while (true){
            response = in.readLine();
            print("play "+response);
            if(response.length() < 4) continue;
            code = response.substring(0, 4);
            rest = response.substring(4); // might be null
            switch (code){
                case GameConstants.VALID_MOOVE: // Caso quando o movimento feito foi válidado no servidor
                    this.board[this.lastPlay] = this.playerType; // Altera tabuleiro local para ter o movimento feito
                    System.out.println("Wait for the other player`s turn");
                    printBoard();
                    break;

                case GameConstants.INVALID_MOOVE: // Caso quando o movimento feito foi inválidado no servidor
                    System.out.println("Are you trying to fool me? Try it again...");
                    System.out.println("Your Turn!");
                    printBoard();
                    myTurn();
                    break;

                case GameConstants.OTHER_MOVED: // Caso quando o outro jogador fez um movimento
                    int i = Integer.parseInt(rest.trim());
                    print("OTHER moved "+i);
                    this.board[i] = this.otherPlayerType;
                    System.out.println("The other player moved");
                    break;

                case GameConstants.YOUR_TURN:// Caso quando é a vez do jogador
                    System.out.println(rest);
                    printBoard();
                    myTurn();
                    break;

                case GameConstants.YOU_WIN: // Caso quando o jogador atual ganha a partida
                    System.out.println("YOU WIN");
                    endGame = true;
                    printBoard();
                    break;

                case GameConstants.YOU_LOOSE: // Caso quando o jogador atual perde a partida
                    System.out.println("YOU LOOSE");
                    endGame = true;
                    printBoard();
                    break;

                case GameConstants.YOU_TIE: // Caso quando há empate
                    System.out.println("BORRING... It`s a Tie...");
                    endGame = true;
                    printBoard();
                    break;

                case GameConstants.MESSAGE: // Caso genérico para que o servidor possa comunicar o jogador com mensagens simples
                    System.out.println(rest);
                    break;
            }
            if(endGame)
                break;

        }
    }

    /**
     * Turno do jogador atual
     * espera-se que diga em que posićão deseja jogar
     */
    private void myTurn() {
        int play;
        while (true) {
            System.out.println("Press 1 - 9");
            String input = keyboard.next();
            try {
                play = Integer.parseInt(""+input.trim().charAt(0));
                if(play >=1 && play <=9){
                    play = play - 1;
                    out.println(GameConstants.MOVE+play);
                    this.lastPlay = play;
                    break;
                }
            } catch (Exception e){}
        }

    }


    /**
     * Método auxiliar para imprimir o tabuleiro
     */
    public void printBoard(){
        if(debug) {
            String s = String.valueOf(board);
            print(s);
        }

        for (int i = 0; i < board.length; i++) {
            if(i % 3 == 0) System.out.print("\n");
            if(board[i] != '\u0000')
                System.out.print(" "+board[i]);
            else
                System.out.print(" " + (i+1));

        }
        System.out.println();
    }
}
