package cn.edu.seu.lxk.RenjuAI;

/**
 * Created by zerlz on 2018/9/18.
 * 该类封装了棋盘上的一次下子动作
 */
public class Move {
    public final int row;
    public final int col;


    /**
     * 通过一个横纵坐标定义一个下子动作
     * @param row
     * @param col
     */
    public Move(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * 棋盘从下往上是1,2,3,....,15行，从左往右是A,B,C,...,O列，按"列+行"表示一个棋盘位置
     * @return
     */
    public String getPositionStr(int boardSize) {
        int rowPosiition = boardSize - row;//行位置
        char colPosition = (char) ('A' + col);//列位置
        return new String(Character.toString(colPosition) + rowPosiition);//"列+行"
    }


}

