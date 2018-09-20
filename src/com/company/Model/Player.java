package com.company.Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * SERVIDOR
 * Classe que armazena a lógica para cada jogador,
 * desde a busca de uma partida a
 * jogabilidade
 */
public class Player extends Thread {
    private Socket socket;
    private Boolean x;
    private Game game;
    private Player other;
    private Result result;
    BufferedReader input;
    PrintWriter output;

    /**
     * Inicializa o jogador atribuindo o seu socket e inicializando Streams para o input e output
     * @param skt
     */
    public Player(Socket skt){
        this.socket = skt;
        this.x = null;
        this.game = null;

        try {
            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("WELCOME ");
        } catch (Exception e) {
            System.out.println("Player died: " + e);
            output.println(GameConstants.MESSAGE+"ops, something went wrong!");
        }

    }

    /**
     * Helper para obter o entrećo do socket que nesta implementaćao é único
     * @return
     */
    public SocketAddress getSocketAddress(){
        return this.socket.getRemoteSocketAddress();
    }

    /**
     * Inicializa/CRIA jogo
     * @param game
     */
    public void startingGame(Game game) {
        this._ActionGame(game, true);
    }

    /**
     * Jogador se junta a um jogo já pré existente
     * @param game
     * @return
     */
    public boolean joiningGame(Game game) {
        if(game.playerY != null) return false;
        this._ActionGame(game, false);
        return true;
    }

    /**
     * Classe Genéria para inializaćão de jogo, tanto pata a criacao
     * quanto para o jogador que quiser se jutar a um jogo
     * já pre existente
     * @param game
     * @param x
     */
    private void _ActionGame(Game game, Boolean x) {
        this.game = game;
        this.x = x;
        if(x) this.game.playerX = this;
        else  this.game.playerY = this;
    }

    /**
     * Retorna o socket do jogador
     * @return
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * retorna o input do jogador BufferedReader
     * @return
     */
    public BufferedReader getInput() {
        return input;
    }

    public PrintWriter getOutput() {
        return output;
    }

    /**
     * retorna o output do jogador PrintWriter
     * @return
     */
    public Player getOther(){
        return this.other;
    }

    /**
     * Pega a marcacao do jogador atual X ou O
     * @return
     */
    public String getMark(){
        return this.x ? "X" : "O";
    }

    /**
     * Pega a marcacao do outro jogador  X ou O
     * @return
     */
    public String getOtherMark(){
        return !this.x ? "X" : "O";
    }


    /**
     * Adiciona a referencia para o jogador adversário
     * @param other
     */
    public void setOtherPlayer(Player other){
        this.other = other;
    }

    /**
     * lógica para quando o outro jogador moveu
     * informa se houve empate derrota ou é sua vez
     * @param position
     */
    public void otherMoved(int position){
        output.println(GameConstants.MESSAGE + "The other player placed a " + getOtherMark() +" at "+ position);
        this.result = this.game.checkResult();

        switch (this.result.getEnumResult()){
            case proceed:
                System.out.println("output: other_moved");
                output.println(GameConstants.OTHER_MOVED + position);
                output.println(GameConstants.YOUR_TURN+"Your Turn");
                break;
            case win:
                System.out.println("output: you_loose");
                output.println(GameConstants.OTHER_MOVED + position);
                output.println(GameConstants.YOU_LOOSE);
                break;
            case tie:
                System.out.println("output: you_tie");
                output.println(GameConstants.OTHER_MOVED + position);
                output.println(GameConstants.YOU_TIE);
                break;
        }
    }

    /**
     * lógica geral do jogo
     * @throws IOException
     * @throws InterruptedException
     */
    public void gameLogic() throws IOException, InterruptedException {
        output.println(GameConstants.MESSAGE+"The Game is about to start...");

        if(this.x) {
            this.awaitStart(); // ESPERA PELO OUTRO JOGADOR
            output.println(GameConstants.YOUR_TURN + "You are the X`s. It is your turn, move!");
        }
        else
            output.println(GameConstants.MESSAGE+"You are the O`s. Wait for the other player`s turn...");

        String action;
        int move;
        while (true) {
            action = input.readLine();
            if(action == null) continue;
            if(action.startsWith("6666")) {
                output.println(GameConstants.YOU_WIN);
                break;
            }

            if(action.startsWith(GameConstants.MOVE)) {
                move = Integer.parseInt(action.substring(4));
                if(game.legalMove(move, this)){
                    switch (game.result.getEnumResult()){
                        case proceed:
                            System.out.println("output: valid_moove");
                            output.println(GameConstants.VALID_MOOVE);
                            break;
                        case win:
                            System.out.println("output: you_win");
                            output.println(GameConstants.VALID_MOOVE);
                            output.println(GameConstants.YOU_WIN);
                            break;
                        case tie:
                            System.out.println("output: you_tie");
                            output.println(GameConstants.VALID_MOOVE);
                            output.println(GameConstants.YOU_TIE);
                            break;
                    }
                } else {
                    System.out.println("output: invalid_moove");
                    output.println(GameConstants.INVALID_MOOVE + "Invalid Move!");
                }
            } else if(action.startsWith("QUIT")){
                break;
            }
        }
        this.game = null;
    }

    /**
     * Escuta por input do jogador para criar/entrar partida
     * ou sair do serivor
     * @throws IOException
     */
    public void playerListener() throws IOException {
            String action, name;
            GameManager.addPlayer(this);
            String response = this.input.readLine();
            System.out.println("Player " + this.getSocketAddress() + " sent " + response);
            if (response.length() > 4) {
                action = response.substring(0, 4);
                switch (action) {
                    case GameConstants.CREATE_GAME:
                        name = response.substring(4);
                        if (GameManager.createGame(this.getSocketAddress(), name))
                            this.getOutput().println(GameConstants.CREATE_GAME_OK);
                        else
                            this.getOutput().println(GameConstants.CREATE_GAME_ERROR);
                        break;

                    case GameConstants.JOIN_GAME:
                        name = response.substring(4);
                        if (GameManager.joinGame(this.getSocketAddress(), name))
                            this.getOutput().println(GameConstants.JOIN_GAME_OK);
                        else
                            this.getOutput().println(GameConstants.JOIN_GAME_ERROR);
                        break;

                    case GameConstants.EXIT:
                        GameManager._disconnect(this.getSocketAddress());
                        break;
                }
            }

    }

    /**
     * Método que roda a Thread
     */
    public void run(){
        try{
            while (true) {
                if (game == null)
                    //ENQUANTO NAO ESTÄ E NENHUMA PARTIDA ESCUTA PELO JOGADOR PAR CRIAR PARTIDAS
                    playerListener();
                else
                    try {
                        //SE NAO JOGA
                        gameLogic();
                        this.game = null;
                    } catch (Exception e){
                        this.game = null;
                        e.printStackTrace();
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

    /**
     * Sincronizaćao da thread para início do jogo / ESPERA
     * @throws InterruptedException
     */
    private synchronized void awaitStart() throws InterruptedException {
        this.wait();
    }
    /**
     * Sincronizaćao da thread para inicio do jogo/
     * caso Outro jogador entrou
     * pode prosseguir
     */
    public synchronized void notifyStart() {
        this.notify();
    }
}
