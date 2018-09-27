package cn.edu.seu.lxk.RenjuAI;

/**
 * Created by zerlz on 2018/9/18.
 * 该类封装了一个棋盘盘面上的一个位置对象
 */
public class Field {
    protected final int row;//该位置的横坐标
    protected final int col;//该位置的纵坐标
    //TODO 改回protect
    public int index;//用来表示该位置的棋子状态，0表示无棋子，1表示player1占据了该位置，2表示player2占据了该位置，3表示越界

    /**
     * 默认构造函数总是一个越界的位置
     */
    public Field() {
        this.row = 0;
        this.col = 0;
        this.index = 3;
    }

    /**
     * 带横纵坐标构造的对象表示该位置上无棋子
     * @param row
     * @param col
     */
    public Field(int row, int col) {
        this.row = row;
        this.col = col;
        this.index = 0;
    }

    public Field(int row, int col, int index) {
        this.row = row;
        this.col = col;
        this.index = index;
    }
}

