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

    public static void start() throws IOException {
        int port = 9000;
        ServerSocket socket = new ServerSocket(port);

        System.out.println("The server is running at "+ port);
        cleanner();
        while (true){
            Socket _socket = socket.accept();
            Player player = new Player( _socket);
            player.start();
//            sendAvailableMachs(player);
//            players.put(_socket.getRemoteSocketAddress(),player);
        }
    }

    private static void sendAvailableMachs(Player player){
        player.getOutput().println(GameConstants.LIST_GAME+getGamesJson());
    }

    private static void cleanner(){
        System.out.println("setting up Cleaner");
        new Thread(()->{
            while (true) {
                checkForLostConnections();
                try { // Reduzindo o uso de CPU
                    Thread.sleep(1000);
                } catch (Exception e){}
            }
        }).start();
        System.out.println("Server Cleaner set up");
    }

    private static void checkForLostConnections() {
        for (SocketAddress socketAddress : players.keySet()) {
                Player player = players.get(socketAddress);
                if(!player.getSocket().isConnected()){
                    try {
                        _disconnect(socketAddress);
                    } catch (Exception e){}
                }
        }
    }

    public static void _disconnect(SocketAddress socketAddress) {
        Player player = players.remove(socketAddress);
        System.out.println("DISCONNECTED: "+ socketAddress);
        try {
            player.getSocket().close();
        } catch (IOException e) {
            System.out.println("Exception close");
        }
    }

    public static boolean createGame(SocketAddress socketAddress, String name) {
        if(!games.containsKey(name)) {
            Game game = new Game();
            games.put(name, game);

            Player player = players.remove(socketAddress);
            player.startingGame(game);
            game.setCurrentPlayer(player);
            player.getOutput().println(GameConstants.OK+"GAME CREATED, WAITING OTHER PLAYER");
            broadcastPlayers(GameConstants.LIST_GAME+getGamesJson());
            return true;
        } else {
            return false;
        }
    }

    public static boolean joinGame(SocketAddress socketAddress, String name) {
        Game game = games.remove(name);
        if(game != null){
            player = players.remove(socketAddress);
            if(!player.joiningGame(game)) return false;

            game.setCrossRelationPlayers();

            player.notifyStart();
            game.playerX.notifyStart();
            player.getOutput().println(GameConstants.OK+"JOINED GAME");
            broadcastPlayers(GameConstants.LIST_GAME+getGamesJson());
            return true;
        }
        return false;
    }

    private static String getGamesJson(){
        String games_string = gson.toJson(games.keySet());
        return  games_string;
    }

    public static void broadcastPlayers(String message){
        System.out.println("Broadcasting\n"+message);
        for (Player player: players.values()) {
            player.getOutput().println(message);
        }
    }

    public static void addPlayer(Player player) {
        sendAvailableMachs(player);
        players.put(player.getSocketAddress(), player);
    }
}
