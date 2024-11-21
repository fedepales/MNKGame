/*
 *  Copyright (C) 2021 Pietro Di Lena
 *  
 *  This file is part of the MNKGame v2.0 software developed for the
 *  students of the course "Algoritmi e Strutture di Dati" first 
 *  cycle degree/bachelor in Computer Science, University of Bologna
 *  A.Y. 2020-2021.
 *
 *  MNKGame is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This  is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this file.  If not, see <https://www.gnu.org/licenses/>.
 */
package mnkgame;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import mnkgame.MNKBoard;
import java.util.concurrent.Callable;

public class Froggy implements MNKPlayer {

    

    private static int maximumDepth;
    private static MNKGameState myWin;
    private static MNKGameState yourWin;
    private Random rand;
    private MNKBoard board;    
    private int TIMEOUT;
    private double timeThreshold = 0.95;
    private long start;
    private boolean turn;
    

    

    public String playerName() {
        return "Froggy";
    }


    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        rand = new Random(System.currentTimeMillis());
        board = new MNKBoard(M, N, K);
        myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
        yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
        turn = first;
        TIMEOUT = timeout_in_secs;

    }


    // The alpha-beta pruning algorithm is used to find the best move for the bot inside the selectCell().
    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        maximumDepth = board.getFreeCells().length;
        start = System.currentTimeMillis();

        if (FC.length == 1) {       //O(1)
            board.markCell(FC[0].i, FC[0].j);
            return FC[0];
        }

        if (MC.length > 0) {        //O(1)
            MNKCell c = MC[MC.length - 1];
            board.markCell(c.i, c.j);
        }

        
        // For each cell in the FC list, mark the cell and check if it's a win. If it is, return the
        // cell. If it isn't, unmark the cell and continue.
        for (MNKCell winCell : FC) {    //O(n-FC)
            if (board.markCell(winCell.i, winCell.j) == myWin) {
                return winCell;
            } else {
                board.unmarkCell();
            }
        }

        
        // If the bot can win in the next move, does so. If it can't, 
        // it blocks the opponent from winning.        
        board.markCell(FC[0].i, FC[0].j);
        for (MNKCell winNextMove : FC) {    //O(n-FC)
            if (winNextMove.i != FC[0].i && winNextMove.j != FC[0].j) {
                if (board.markCell(winNextMove.i, winNextMove.j) == yourWin) {
                    board.unmarkCell(); 
                    board.unmarkCell(); 
                    board.markCell(winNextMove.i, winNextMove.j);
                    return winNextMove;
                } else {
                    board.unmarkCell();
                }
            }
        }
        
        // If the bot can win in one move, it does so. Otherwise, it checks if the opponent can win in
        // one move. If so, it blocks the opponent. Otherwise, it marks the cell that will lead to the
        // best outcome.        
        board.unmarkCell(); // Unmarking the cell with the value of the cell that the bot has chosen.
        
       // If the bot has two or more winning moves, it will mark the first one. If the bot has one
       // winning move and the opponent has one winning move, it will mark the first one. If the bot
       // has one winning move and the opponent has no winning move, it will mark the first one.
        if (FC.length > 1) {    //O(2)
            board.markCell(FC[1].i, FC[1].j); 
            MNKCell blockerMove = FC[0];
            if (board.markCell(blockerMove.i, blockerMove.j) == yourWin) {
                board.unmarkCell(); 
                board.unmarkCell(); 
                board.markCell(blockerMove.i, blockerMove.j); 
                return blockerMove;
            } else {
                board.unmarkCell();
            }
            // Unmarking the bot's cell.
            board.unmarkCell(); 
        }

        
        // The alphaBetaPruning function is called recursively until the maximum depth
        // is reached or the time
        // limit is reached.The function returns the best score and the best move.
        // The function is called with the following parameters:
        // board: the current board state
        // turn: a boolean value that indicates whether the current player is maximizing
        // or
        // minimizing
        // maximumDepth: the maximum depth of the search tree
        // alpha: the alpha value
        // beta: the beta value

        int indexBestMove = 0, alphaValue = Integer.MIN_VALUE, score;

        for (int move = 0; move < board.getFreeCells().length; move++) {    //O(n)
            if ((System.currentTimeMillis() - start) / 1000.0 > TIMEOUT * timeThreshold) {
                break;
            } else {
                MNKCell c = FC[move];
                board.markCell(c.i, c.j);
                score = alphaBetaPruning(board, true, maximumDepth-1, Integer.MIN_VALUE, Integer.MAX_VALUE);      //caso pessimo: b^d(minmax), caso ottimo: radice quadrata (b^d) b=n(numero di mosse) e d=maxDepth
                board.unmarkCell();
                if (score > alphaValue) {
                    alphaValue = score;
                    indexBestMove = move;
                }
            }
        }
        board.markCell(FC[indexBestMove].i, FC[indexBestMove].j);
        return FC[indexBestMove];

    }

    // player's turn minimize / opponent's turn maximize
    /**The alphaBetaPruning method is a recursive method that takes a <code>MNKBoard</code> object, a boolean <code>turn</code>, an
    int <code>depth</code>, an int <code>alpha</code>, and an int <code>beta</code> as parameters. 
    If the depth is 0 or the game state is not OPEN, then the method returns the score of the board. 
    If the turn is true, then the method returns the minimum of the current alpha and the getMin method.
    If the turn is false, then the method returns the maximum of the current beta and the getMax method.

     * @param board        The MNKGame board
     * @param turn         Player's turn true opponent turn false 
     * @param alpha        The alpha value
     * @param depth        The depth value
     * @param beta         The beta value
     * @param eval         The evaluation result from the alphabeta cut off  
     
 */
    private int alphaBetaPruning(MNKBoard board, boolean turn, int depth, int alpha, int beta) {

        int eval;
        if ((System.currentTimeMillis() - start) / 1000.0 > TIMEOUT * timeThreshold) {
            return 0;
        } else {
            if (depth > maximumDepth || !board.gameState().equals(MNKGameState.OPEN)) {
                return score(board.gameState(), depth);
            } else {

                if (turn) {
                    eval=Integer.MAX_VALUE;
                    eval= getMin(eval, board, alpha, beta, depth);
                    return eval;
                } else {
                    eval=Integer.MIN_VALUE;
                    eval=getMax(eval, board, alpha, beta, depth);
                    return eval;
                }
            }
        }
    }


    /**
     * For each free cell, mark it and recursively call alphaBetaPruning with the new board, 
     and the opposite player. For each call, update alpha and beta. If alpha is greater than beta, 
     return the current alpha value. If the current player is the computer, 
     return the minimum of the alpha and beta values.If the current player is the human, 
     return the maximum of the alpha and beta values.
     * @param eval          The evaluation result from the alphabeta cut off  
     * @param board         The MNKGame board
     * @param alpha         The alpha value
     * @param beta          The beta value
     * @param depth         The current depth
     * @return              If the current player is the computer, return the minimum of the alpha and beta values.      
                            If the current player is the human, return the maximum of the alpha and beta values.
     */   
    private int getMin(int eval, MNKBoard board, int alpha, int beta, int depth) {//O()
        MNKCell[] c = board.getFreeCells();

        for (MNKCell cl : c) {
            board.markCell(cl.i, cl.j);
            eval = Integer.min(eval, alphaBetaPruning(board, false, depth + 1, alpha, beta));
            beta = Integer.min(eval, beta);
            board.unmarkCell();
            if (beta <= alpha) { 
                return eval;
            }
        }
        return eval;
    }

    

    /**
     * For each free cell, mark it and recursively call alphaBetaPruning with the new board, 
    and the depth decreased by 1. 
    If the depth is 0, then call the evaluation function on the board. 
    Otherwise, call alphaBetaPruning with the new board, and the depth decreased by 1, 
    and the alpha and beta values swapped. 
    If the alpha value is greater than the beta value, then return the alpha value.
    Otherwise, return the maximum of the alpha and beta values.
     * @param eval          The evaluation result from the alphabeta cut off 
     * @param board         The MNKGame board
     * @param alpha         The alpha value
     * @param beta          The beta value
     * @param depth         The current depth
     * @return              If the current player is the computer, return the minimum of the alpha and beta values.      
                            If the current player is the human, return the maximum of the alpha and beta values.
     */ 
    private int getMax(int eval, MNKBoard board, int alpha, int beta, int depth) {  // b^d(minmax)
        MNKCell[] c = board.getFreeCells();

        for (MNKCell cl : c) {
            board.markCell(cl.i, cl.j);
            eval = Integer.max(eval, alphaBetaPruning(board, true, depth + 1, alpha, beta));
            alpha = Integer.max(eval, alpha);
            board.unmarkCell();
            if (beta <= alpha) { 
                return eval;
            }
        }
        return eval;
    }

    
    /**
     * If the game is over, return the score. Otherwise, return 0.
     * @param state The MNKGame state
     * @param depth The current depth of the board
     * 
     */
    private int score(MNKGameState state, int depth) {  //O(1)

        if (state.equals(myWin)) {
            return 1000 - depth;
        } else if (state.equals(yourWin)) {
            return -1000 + depth;
        } else if (state.equals(MNKGameState.DRAW)) {
            return 0;
        }
        return 0;
    }

}

