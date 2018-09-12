package com.company.Model;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    static Gson gson = new Gson();

    static HashMap<String, Game> games = new HashMap<>();
    static ConcurrentHashMap<SocketAddress, Player> players = new ConcurrentHashMap<SocketAddress, Player>();
    static Player player;
    static String response;
    static String action;
    static String name;

    public static void start() throws IOException {
        int port = 9000;
        ServerSocket socket = new ServerSocket(port);

        System.out.println("The server is running at "+ port);

        while (true){
            System.out.println("here");
            Socket _socket = socket.accept();
            System.out.println("here");
            Player player = new Player( _socket);
            players.put(_socket.getRemoteSocketAddress(),player);
            checkForActions();
            checkForLostConnections();
        }
    }

    private static void checkForLostConnections() {
        for (SocketAddress socketAddress : players.keySet()) {
            new Thread(() -> {
                Player player = players.get(socketAddress);
                if(!player.getSocket().isConnected()){
                    try {

                    } catch (Exception e){}
                }
            }).run();
        }

    }

    private static void checkForActions() {
        for (SocketAddress socketAddress: players.keySet()) {
            try {
                player = players.get(socketAddress);
                response = player.getInput().readLine();
                System.out.println("Player " + socketAddress + " sent " + response);
                if (response.length() < 4) continue;
                action = response.substring(0, 4);
                switch (action) {
                    case GameConstants.CREATE_GAME:
                        name = response.substring(4);
                        createGame(socketAddress);
                        break;

                    case GameConstants.JOIN_GAME:
                        name = response.substring(4);
                        joinGame(socketAddress);
                        break;

                    case GameConstants.EXIT:
                        player.getSocket().close();
                        players.remove(socketAddress);
                        break;
                }
            } catch (Exception e){
                Disconect(socketAddress);
            }
        }
    }

    private static void Disconect(SocketAddress socketAddress) {
        Player player = players.remove(socketAddress);
        try {
            player.getSocket().close();
        } catch (IOException e) {}
    }

    private static void createGame(SocketAddress socketAddress) {
        if(!games.containsKey(games)) {
            Game game = new Game();
            games.put(name, game);

            Player player = players.remove(socketAddress);
            player.startingGame(game);
            player.start();
            game.setCurrentPlayer(player);

            broadcastPlayers(GameConstants.LIST_GAME+getGamesJson());
        }
    }

    private static void joinGame(SocketAddress socketAddress) {
        Game game = games.remove(name);
        if(game != null){
            player = players.remove(socketAddress);
            player.joiningGame(game);
            player.start();

            broadcastPlayers(GameConstants.LIST_GAME+getGamesJson());
        }
    }

    private static String getGamesJson(){
        String games_string = gson.toJson(games.keySet());
        return  games_string;
    }

    public static void broadcastPlayers(String message){
        for (Player player: players.values()) {
            player.getOutput().println(message);
        }
    }
}
