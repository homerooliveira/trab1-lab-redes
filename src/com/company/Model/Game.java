package com.company.Model;


/**
 * SERVIDOR
 * Classe que implementa a lógica do jogo
 */
public class Game {
    //Iinicializa o quadro vazio
    private Player[] board = {
            null, null, null,
            null, null, null,
            null, null, null};

    public Player playerX;
    public Player playerY;
    Player currentPlayer;
    Result result;


    /**
     * Construitor da classe game que só inicializa o resultado
     */
    public Game() {
        this.result = new Result(EnumResult.proceed, null);
    }

    /**
     * Verifica se a partida acabou em vitória de um jogador
     * @return
     */
    private boolean hasWinner() {
        if (board[0] != null && board[0] == board[1] && board[0] == board[2])
            return this.setWinner(board[0]);
        else if (board[3] != null && board[3] == board[4] && board[3] == board[5])
            return this.setWinner(board[3]);
        else if (board[6] != null && board[6] == board[7] && board[6] == board[8])
            return this.setWinner(board[6]);
        else if (board[0] != null && board[0] == board[3] && board[0] == board[6])
            return this.setWinner(board[0]);
        else if (board[1] != null && board[1] == board[4] && board[1] == board[7])
            return this.setWinner(board[1]);
        else if (board[2] != null && board[2] == board[5] && board[2] == board[8])
            return this.setWinner(board[2]);
        else if (board[0] != null && board[0] == board[4] && board[0] == board[8])
            return this.setWinner(board[0]);
        else if (board[2] != null && board[2] == board[4] && board[2] == board[6])
            return this.setWinner(board[2]);

        return false;
    }

    /**
     * Verifica por empates
     * @return
     */
    private boolean checkTie() {
        for (int i = 0; i < board.length; i++) {
            if (board[i] == null) {
                return false;
            }
        }
        this.result.setEnumResult(EnumResult.tie);
        return true;
    }

    /**
     * Seta a vez para algum jogador
     * @param player
     */
    public void setCurrentPlayer(Player player){
        this.currentPlayer = player;
    }

    /**
     * Seta o jogador vencedor
     * @param winner
     * @return
     */
    private Boolean setWinner(Player winner) {
        this.result.setWinner(winner);
        this.result.setEnumResult(EnumResult.win);
        return true;
    }

    /**
     * classe genéria que busca pelo resultado da pertida, se empatou ou ganhou/pardeu
     * @return
     */
    public Result checkResult() {
        if(this.hasWinner())
            return this.result;
        this.checkTie();
        return this.result;
    }

    /**
     * Checa se o movimento do jogador foi válido
     * @param location
     * @param player
     * @return
     */
    public synchronized boolean legalMove(int location, Player player) {
        if( !(location >= 0 && location<=8) )
            return false;

        if (player == currentPlayer && board[location] == null) {
            board[location] = currentPlayer;
            currentPlayer = currentPlayer.getOther();
            currentPlayer.otherMoved(location);
            return true;
        }
        return false;
    }

    /**
     * Seta relaćao entre os dois jogadores... para referencia
     */
    public void setCrossRelationPlayers() {
        this.playerX.setOtherPlayer(this.playerY);
        this.playerY.setOtherPlayer(this.playerX);
    }
}