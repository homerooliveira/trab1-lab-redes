package com.company.Model;

import sun.invoke.empty.Empty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Player extends Thread {
    private Socket socket;
    private Boolean x;
    private Game game;
    private Player other;
    private Result result;
    BufferedReader input;
    PrintWriter output;


    public Player(Socket skt, boolean x, Game game){
        this.socket = skt;
        this.x = x;
        try {
            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("WELCOME " + getMark() );
            output.println(GameConstants.MESSAGE+"Waiting for opponent...");
        } catch (Exception e) {
            System.out.println("Player died: " + e);
            output.println(GameConstants.MESSAGE+"The Other player left the game");
        }

    }


    public Player getOther(){
        return this.other;
    }
    public String getMark(){
        return this.x ? "X" : "O";
    }
    public String getOtherMark(){
        return !this.x ? "X" : "O";
    }


    public void setOtherPlayer(Player other){
        this.other = other;
    }

    public void otherMoved(int position){
        output.println(GameConstants.MESSAGE + "The other player placed a " + getOtherMark() +" at "+ position);
        this.result = this.game.checkResult();

        switch (this.result.getEnumResult()){
            case proceed:
                output.println(GameConstants.OTHER_MOVED + position);
                break;
            case win:
                output.println(GameConstants.YOU_LOOSE);
                break;
            case tie:
                output.println(GameConstants.YOU_TIE);
                break;
        }
    }


    public void run(){
        try{
            output.println(GameConstants.MESSAGE+"The Game is about to start...");

            if(this.x)
                output.println(GameConstants.MESSAGE+"It is your turn, move!");

            String action;
            int move;
            while (true) {
                action = input.readLine();
                if(action == null) continue;

                if(action.startsWith(GameConstants.MOVE)) {
                    move = Integer.parseInt(action.substring(4));
                    if(game.legalMove(move, this)){
                        switch (this.result.getEnumResult()){
                            case proceed:
                                output.println(GameConstants.VALID_MOOVE);
                                break;
                            case win:
                                output.println(GameConstants.YOU_WIN);
                                break;
                            case tie:
                                output.println(GameConstants.YOU_TIE);
                                break;
                        }
                    } else {
                        output.println(GameConstants.INVALID_MOOVE + "Invalid Move!");
                    }


                } else if(action.startsWith("QUIT")){
                    break;
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
