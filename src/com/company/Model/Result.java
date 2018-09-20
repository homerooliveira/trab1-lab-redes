package com.company.Model;

/**
 * Classe para ajudar a armazenar o resultado de uma partida
 */
public class Result {
    private EnumResult enumResult;
    private Player winner;

    public Result(EnumResult enumResult, Player winner) {
        this.enumResult = enumResult;
        this.winner = winner;
    }

    public EnumResult getEnumResult() {
        return enumResult;
    }

    public void setEnumResult(EnumResult enumResult) {
        this.enumResult = enumResult;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }
}
