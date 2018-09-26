package cn.edu.seu.lxk.RenjuAI;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by zerlz on 2018/9/19.
 * 当前游戏状态类
 */
public class GameState {

    private int size;
    private int[][] board;
    private Stack<Move> moves;//用栈保存步骤
    private int currentIndex = 1;

    /**
     * 新游戏
     *
     * @param size
     */
    public GameState(int size) {
        this.size = size;
        this.board = new int[size][size];
        this.moves = new Stack<>();
    }


    /**
     * 将栈结果按顺序返回到数组
     *
     * @return
     */
    public List<Move> getMovesMade() {
        return new ArrayList(moves);
    }


    /**
     * 进行一步游戏
     *
     * @param move Move to make
     */
    public void makeMove(Move move) {
        this.moves.push(move);//压栈
        this.board[move.row][move.col] = currentIndex;
        this.currentIndex = currentIndex == 1 ? 2 : 1;//换手
    }
}
