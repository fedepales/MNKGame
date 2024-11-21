/*
 *  Copyright (C) 2021 Gabriele Crestanello, Pietro Volpe
 *  
 *  This file is part of the MNKGame v2.0 software developed for the
 *  students of the course "Algoritmi e Strutture di Dati" first 
 *  cycle degree/bachelor in Computer Science, University of Bologna
 *  A.Y. 2020-2021.
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

public class alphaBetaPruning implements MNKPlayer{

    private static int maximumDepth;
    private Random rand;
    private MNKBoard board;
    private static MNKGameState myWin;
    private static MNKGameState yourWin;
    private int TIMEOUT;
    private long start;
    private double powLimit;
    private boolean turn;

    /**
     * AlphaBetaAdvanced cannot be instantiated.
     */
    public alphaBetaPruning() {}

    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        rand = new Random(System.currentTimeMillis());
        board = new MNKBoard(M, N, K);
        myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
        yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
        turn=first;
        TIMEOUT = timeout_in_secs;
        powLimit = timeout_in_secs * Math.pow(10, 9);
    }

    public String playerName() {
        return "AlphaBeta";
    }

public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC){

    start = System.currentTimeMillis();
    maximumDepth= GetMaxDepth(FC.length);
    if (FC.length == 1) {
        board.markCell(FC[0].i, FC[0].j);
        return FC[0];
    }
    
    if(MC.length>0){
        MNKCell c = MC[MC.length-1];
        board.markCell(c.i, c.j);
    }

     //controllo la presenza di mosse in grado di far vincere il bot/l'avversario al prossimo turno, reagisco di conseguenza
        //controllo se il bot può vincere con una mossa
        for (MNKCell d : FC) {
            if (board.markCell(d.i, d.j) == myWin) {
                //DEBUG OUTPUT
                //System.out.println("HO VINTO");
                System.out.println("gestione gamestate 'game ended' OLIVIER");
                return d;
            } else {
                board.unmarkCell();
            }
        }

        //controllo se l'avversario può vincere alla prossima mossa
        board.markCell(FC[0].i, FC[0].j);   //segno la mossa del bot
        for (MNKCell d : FC) {
            if (d.i != FC[0].i && d.j != FC[0].j) {
                if (board.markCell(d.i, d.j) == yourWin) {
                    board.unmarkCell();     //annullo la mossa dell'avversario
                    board.unmarkCell();     //annullo la mia mossa
                    board.markCell(d.i, d.j);   //segno la mossa per bloccare l'avversario
                    System.out.println("blocca mossa vincente OLIVIER");
                    System.out.println(d);
                    //DEBUG OUTPUT
                    //System.out.println("HO BLOCCATO UNA MOSSA VINCENTE");
                    return d;
                } else {
                    board.unmarkCell();
                }
            }
        }
        board.unmarkCell();     //annullo la mossa iniziale del bot
        //controllo se l'avversario può vincere in posizione FC[0], visto che con lo scorso controllo usavo la "mossa segnaposto" del bot in quella posizione
        if (FC.length > 1) {
            board.markCell(FC[1].i, FC[1].j);   //segno la mossa del bot
            MNKCell d = FC[0];
            if (board.markCell(d.i, d.j) == yourWin) {
                board.unmarkCell();     //annullo la mossa dell'avversario
                board.unmarkCell();     //annullo la mia mossa
                board.markCell(d.i, d.j);   //segno la mossa per bloccare l'avversario
                System.out.println("controllo FC[0] OLIVIER");
                System.out.println(d);
                //DEBUG OUTPUT
                //System.out.println("HO BLOCCATO UNA MOSSA VINCENTE");
                return d;
            } else {
                board.unmarkCell();
            }
            board.unmarkCell();     //annullo la mossa iniziale del bot
        }
     
    int move = 0, bestScore = Integer.MIN_VALUE, score = bestScore;

        for (int i = 0; i < board.getFreeCells().length; i++) {
            if ((System.currentTimeMillis() - start) / 1000.0 > TIMEOUT) { //se ho sforato il limite del tempo di timeout
                //DEBUG OUTPUT
                break;
            } else {
                MNKCell c = FC[i];
                board.markCell(c.i, c.j);
                score = alphaBetaPruning(board, false, maximumDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
                board.unmarkCell();
                if (score > bestScore) {
                    bestScore = score;
                    move = i;
                }
            }
        }
    board.markCell(FC[move].i, FC[move].j);
    return FC[move];
    
}




     //turno avversario si massimizza / giocatore minimizza
private int alphaBetaPruning(MNKBoard board, boolean turn, int depth, int alpha, int beta) { 
    int eval;
    if( depth == 0 || !board.gameState().equals(MNKGameState.OPEN)){
        return score(board.gameState(), depth);
    }else{
        
        if(!turn){//minimizzo
            eval=Integer.MAX_VALUE;
            eval= getMin(eval, board, alpha, beta, depth);
            return eval; 
        }else{
            
           /*  MNKCell[] c = board.getFreeCells();
            eval=Integer.MIN_VALUE;
            for(MNKCell cl : c){
                board.markCell(cl.i,cl.j);
                eval=Integer.max(eval, alphaBetaPruning(board, true, depth-1, alpha, beta));
                alpha= Integer.max(eval, alpha);
                board.unmarkCell();
                if(beta<=alpha){//taglio
                    break;
                }
            } */
            eval=Integer.MIN_VALUE;
            eval=getMax(eval, board, alpha, beta, depth);
            return eval;
        }
    }
        
}
    
private int getMin (int eval, MNKBoard board, int alpha, int beta, int depth) {
    MNKCell[] c = board.getFreeCells();
    eval=Integer.MAX_VALUE;
    for(MNKCell cl : c){
        board.markCell(cl.i,cl.j);
        eval=Integer.min(eval, alphaBetaPruning(board, !turn, depth-1, alpha, beta));
        beta=Integer.min(eval, beta);
        board.unmarkCell();
        if(beta <= alpha){//taglio
            break;
        }
    }
    return eval;
}

private int getMax (int eval, MNKBoard board, int alpha, int beta, int depth) {
    MNKCell[] c = board.getFreeCells();
    eval=Integer.MIN_VALUE;
    for(MNKCell cl : c){
        board.markCell(cl.i,cl.j);
        eval=Integer.max(eval, alphaBetaPruning(board, !turn, depth-1, alpha, beta));
        alpha= Integer.max(eval, alpha);
        board.unmarkCell();
        if(beta<=alpha){//taglio
            break;
        }
    }
    return eval;
}

private int score (MNKGameState state, int depth) {
    //Board.State opponent = (turn == Board.State.X) ? Board.State.O : Board.State.X;
    
    if (state.equals(myWin)) {
        return 1000 - depth;
    } else if (state.equals(yourWin)) {
        return -1000 + depth;
    } else if (state.equals(MNKGameState.DRAW)) {
        return 0;
    }
    return 0;
}

private int GetMaxDepth(int len) {
    long n = len, counter = n;
    int ret = 1;
    do {
        n = n - 1;
        if (counter * n <= powLimit) {
            ret++;
        }
        counter = counter * n;
    } while (counter < powLimit && n > 1);
    return ret;
}

}


