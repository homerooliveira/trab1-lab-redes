package com.company;

import com.company.Model.Game;
import com.company.Model.Player;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {

    public static void main(String args[]) throws IOException {
        ServerSocket socket = new ServerSocket(9000);
        System.out.println("Server Is Running");

        while (true){
            Game game = new Game();
            Player player1 = new Player(socket.accept(), true, game);
            Player player2 = new Player(socket.accept(), false, game);

            player1.setOtherPlayer(player2);
            player2.setOtherPlayer(player1);

            game.setCurrentPlayer(player1);

            player1.start();
            player2.start();
        }

    }

}
