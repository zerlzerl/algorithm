package cn.edu.seu.lxk.RenjuAI;

import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 包含hash置换表的局面类State
 */
public class State {
    //用于表示盘面情况的二维数组board
    protected final Field[][] board;
    //棋盘方向用一个四维数组来存储，第一第二维是row和col用来确定一个位置，后两维可以看成是关于这个位置的一个二维数组，分别表示方向和该方向上各位置的情况
    //后两维可以表示成
    //[0][0-9] -> 左上到右下方向：该方向上[0-4]在左上从远到近，[5-9]从右下近到远
    //[1][0-9] -> 右上到左下，类似上面
    //[2][0-9] -> 纵向
    //[3][0-9] -> 横向
    protected final Field[][][][] directions;

    protected int currentIndex;


    //保存走棋序列的栈
    private Stack<Move> moveStack;

    /**
     * 创建一个新的状态，构造函数
     * @param intersections 棋盘上的交叉点数
     */
    public State(int intersections) {
        this.board = new Field[intersections][intersections];
        for(int i = 0; i < intersections; i++) {
            for(int j = 0; j < intersections; j++) {
                board[i][j] = new Field(i, j);
            }
        }
        this.directions = new Field[intersections][intersections][4][9];
        this.currentIndex = 1;
        this.moveStack = new Stack<>();
        this.generateDirections(board);

    }

    /**
     * 在棋盘上进行一步走棋
     * @param move
     */
    public void makeMove(Move move) {
        moveStack.push(move);
        this.board[move.row][move.col].index = this.currentIndex;
        this.currentIndex = this.currentIndex == 1 ? 2 : 1;
    }

    /**
     * 在棋盘上撤掉一步棋
     * @param move
     */
    public void undoMove(Move move) {
        moveStack.pop();
        this.board[move.row][move.col].index = 0;
        this.currentIndex = this.currentIndex == 1 ? 2 : 1;
    }

    /**
     * 在给定的距离内，返回这个field周围时候有其他占用的棋子。用于确定棋盘中的一个field是否值得作为一个可能的Move进行评估。
     * @param row
     * @param col
     * @param distance 最大为4
     * @return
     */
    protected boolean hasAdjacent(int row, int col, int distance) {
        for(int i = 0; i < 4; i++) {
            for(int j = 1; j <= distance; j++) {
                if(directions[row][col][i][4 + j].index == 1
                        || directions[row][col][i][4 - j].index == 1
                        || directions[row][col][i][4 + j].index == 2
                        || directions[row][col][i][4 - j].index == 2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 创建四维数组directions
     * 通过遍历四个方向上的临近位置，并存储下相邻位置的引用
     * @param board Field array
     */
    private void generateDirections(Field[][] board) {
        for(int row = 0; row < board.length; row++) {
            for(int col = 0; col < board.length; col++) {
                directions[row][col][0][4] = board[row][col];
                directions[row][col][1][4] = board[row][col];
                directions[row][col][2][4] = board[row][col];
                directions[row][col][3][4] = board[row][col];

                for(int k = 0; k < 5; k++) {
                    //左上
                    if(row - k >= 0 && col - k >=0) {
                        directions[row][col][0][4 - k] = board[row -
                                k][col - k];
                    } else {
                        directions[row][col][0][4 - k] = new Field();
                    }

                    //右下
                    if(row + k < board.length && col + k < board.length) {
                        directions[row][col][0][4 + k] =
                                board[row + k][col + k];
                    } else {
                        directions[row][col][0][4 + k] = new Field();
                    }

                    //右上
                    if(row - k >= 0 && col + k < board.length) {
                        directions[row][col][1][4 - k] =
                                board[row - k][col + k];
                    } else {
                        directions[row][col][1][4 - k] = new Field();
                    }

                    //左下
                    if(row + k < board.length && col - k >=0) {
                        directions[row][col][1][4 + k] =
                                board[row + k][col - k];
                    } else {
                        directions[row][col][1][4 + k] = new Field();
                    }

                    //上
                    if(row - k >= 0) {
                        directions[row][col][2][4 - k] =
                                board[row - k][col];
                    } else {
                        directions[row][col][2][4 - k] = new Field();
                    }

                    //下
                    if(row + k < board.length) {
                        directions[row][col][2][4 + k] =
                                board[row + k][col];
                    } else {
                        directions[row][col][2][4 + k] = new Field();
                    }

                    //左
                    if(col - k >= 0) {
                        directions[row][col][3][4 - k] =
                                board[row][col - k];
                    } else {
                        directions[row][col][3][4 - k] = new Field();
                    }

                    //右
                    if(col + k < board.length) {
                        directions[row][col][3][4 + k] =
                                board[row][col + k];
                    } else {
                        directions[row][col][3][4 + k] = new Field();
                    }
                }
            }
        }
    }

    /**
     * 以最新一步棋为基点，判断当前局面是否已经结束
     * @return 0表示未结束，1表示player1赢，2表示player2赢，3表示平局
     */
    protected int terminal() {
        Move move = moveStack.peek();
        int row = move.row;
        int col = move.col;
        int lastIndex = currentIndex == 1 ? 2 : 1;

        //最新一步下下来之后查看是否已经达到五连
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 6; j++) {
                if(directions[row][col][i][j].index == lastIndex) {
                    int count = 0;
                    for(int k = 1; k < 5; k++) {
                        if(directions[row][col][i][j+k].index == lastIndex) {
                            count++;
                        } else {
                            break;
                        }
                    }
                    if(count == 4) return lastIndex;
                }
            }
        }
        return moveStack.size() == board.length * board.length ? 3 : 0;
    }

    /**
     * 步数统计
     * @return
     */
    protected int getMoves() {
        return moveStack.size();
    }


}

