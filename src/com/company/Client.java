package com.company;

import com.company.Model.GameConstants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Future;


public class Client {
    public char [] board;

    public char playerType;
    public char otherPlayerType;

    int lastPlay;
    public final int port = 9000;
    public Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    Scanner keyboard = new Scanner(System.in);
    boolean ok = false;

    private boolean shoudListenServer = false;

    Gson gson = new Gson();
    JsonParser jsonParser= new JsonParser();


    public static void main(String args[]){
        String server = "localhost";

        while (true){
            try {
                if(!WantToPlay()) {
                    System.out.println("GOOD BYE...\nSee you later alligator");
                    break;
                }
                Client client = new Client(server);
                client.browseGames();
                client.play();
            } catch (Exception e){
                System.out.println("Connection Problem");
            }

        }
    }

    private void browseGames() throws IOException {
        System.out.println("TESTING");
        out.println("Connected");
        shoudListenServer = true;

        ok = false;

        Thread inputListenner = new Thread(()->{
            String opt = "";
            do {
                do {
                    System.out.println("Do you want to enter a existing game? press 1 - yes\n" +
                            "Else if you want to create a game press 2");
                    opt = keyboard.next().trim();

                } while (opt != "1" || opt != "2");
                shoudListenServer = false;
                switch (opt) {
                    case "1": // Enter an existing game
                        System.out.println("Which match do you want to join?");
                        opt = keyboard.next().trim();
                        out.print(GameConstants.CREATE_GAME + opt);
                        break;
                    case "2": // Create a game
                        System.out.println("Choose the name of the match?");
                        opt = keyboard.next().trim();
                        out.print(GameConstants.JOIN_GAME + opt);
                        break;
                }
                try {
                    synchronized (this){
                        wait(100000000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (!ok);
        });
        inputListenner.start();
        String serverMsg, message, code;

        do {
            serverMsg= in.readLine();
            if(serverMsg.length() <3) continue;
            code = serverMsg.substring(0,4);
            switch (code){
                case GameConstants.CREATE_GAME_OK:
                    System.out.println("GAME CREATED");
                    ok = true;
                    break;
                case GameConstants.CREATE_GAME_ERROR:
                    System.out.println("ERROR try again");
                    break;
                case GameConstants.JOIN_GAME_OK:
                    System.out.println("JOINED GAME");
                    ok = true;
                    break;
                case GameConstants.JOIN_GAME_ERROR:
                    System.out.println("ERROR try again");
                    break;
                case GameConstants.LIST_GAME:
                    message = serverMsg.substring(4);
                    JsonObject jsonObject = jsonParser.parse(message).getAsJsonObject();
                    Set<String> set = jsonObject.keySet();
                    System.out.println("-> SALAS DISPONÍVEIS:");
                    for (String k: set) {
                        System.out.println(k);
                    }
                    break;
            }
            synchronized (this){
                notify();
            }
        } while (shoudListenServer);
        try {
            inputListenner.join();
        } catch (InterruptedException e) {}

    }

    private void listenToServer(){

    }

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



    public Client(String server) throws IOException {
        this.socket = new Socket(server, this.port);
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        this.board = new char[9];
    }


    public void play() throws IOException {

        String response = in.readLine();
        String code;
        String rest;
        boolean endGame = false;
        if(response.startsWith("WELCOME")){
            this.playerType = response.charAt(8);
            this.otherPlayerType = this.playerType == 'X' ? 'O' : 'X';
        }

        while (true){
            response = in.readLine();
            if(response.length() < 4) continue;
            code = response.substring(0, 4);
            rest = response.substring(4); // might be null
            System.out.println(code);
            switch (code){
                case GameConstants.VALID_MOOVE:
                    this.board[this.lastPlay] = this.playerType;
                    System.out.println("Wait for the other player`s turn");
                    printBoard();
                    break;

                case GameConstants.INVALID_MOOVE:
                    System.out.println("Are you trying to fool me? Try it again...");
                    System.out.println("Your Turn!");
                    printBoard();
                    myTurn();
                    break;

                case GameConstants.OTHER_MOVED:
                    int i = Integer.parseInt(rest.trim());
                    this.board[i] = this.otherPlayerType;
                    System.out.println("The other player moved");
                    printBoard();
                    break;

                case GameConstants.YOUR_TURN:
                    System.out.println(rest);
                    printBoard();
                    myTurn();
                    break;

                case GameConstants.YOU_WIN:
                    System.out.println("YOU WIN");
                    endGame = true;
                    printBoard();
                    break;

                case GameConstants.YOU_LOOSE:
                    System.out.println("YOU LOOSE");
                    endGame = true;
                    printBoard();
                    break;

                case GameConstants.YOU_TIE:
                    System.out.println("BORRING... It`s a Tie...");
                    endGame = true;
                    printBoard();
                    break;

                case GameConstants.MESSAGE:
                    System.out.println(rest);
                    break;
            }
            if(endGame)
                break;

        }
    }

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


    // AUX
    public void printBoard(){
        for (int i = 0; i < board.length; i++) {
            if(i % 3 == 0) System.out.print("\n");
            System.out.print(" "+board[i]);
        }
        System.out.println();
    }
}
