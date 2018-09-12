package com.company;

import com.company.Model.GameConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;




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

    private void browseGames() {
        while (true){
            // TODO implementar
        }
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
