package com.company;

import com.company.Model.Game;
import com.company.Model.GameManager;
import com.company.Model.Player;

import java.io.IOException;
import java.net.ServerSocket;


/**
 * Implementacao do servidor
 */
public class Server {

    static GameManager gameManager;
    public static void main(String args[]) throws IOException {
        gameManager = new GameManager();
        gameManager.start();
    }

}
