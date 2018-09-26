package cn.edu.seu.lxk.RenjuAI;

/**
 * 局势评估方法类封装
 */
public class Evaluator {

    private static final int[] SCORES = {19, 15, 11, 7, 3};

    /**
     * 给当前棋盘状态State与沿垂直/水平/对角线方向的数组，根据可能形成的五连和移动的次数计算分数。
     *
     * @param direction 表示方向的一维数组
     * @return 该方向上的得分
     */
    private static int scoreDirection(Field[] direction, int index) {
        int score = 0;

        //通过一个field数组传递一个五连的窗口
        for(int i = 0; (i + 4) < direction.length; i++) {
            int empty = 0;
            int stones = 0;
            for(int j = 0; j <= 4; j++) {
                if(direction[i + j].index == 0) {
                    empty++;
                }
                else if(direction[i + j].index == index) {
                    stones++;
                } else {
                    break;
                }
            }
            //忽略已成五，清空窗口
            if(empty == 0 || empty == 5) continue;

            //window只包含空的位置和棋子，可以形成一个五，根据需要多少步得到分数
            if(stones + empty == 5) {
                score += SCORES[empty];
            }
        }
        return score;
    }

    /**
     * 从当前玩家的角度评估状态分数
     * @param state
     * @return
     */
    public static int evaluateState(State state, int depth) {
        int playerIndex = state.currentIndex;
        int opponentIndex = playerIndex == 1 ? 2 : 1;

        //对局是否结束
        int terminal = state.terminal();
        if(terminal == playerIndex) return 10000 + depth;
        if(terminal == opponentIndex) return -10000 - depth;

        // 对每个field分别求值，如果field属于对手，则从分数中减去它，如果field属于玩家，则添加它
        int score = 0;
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == opponentIndex) {
                    //对每个field进行评估
                    score -= evaluateField(state, i, j, opponentIndex);
                } else if(state.board[i][j].index == playerIndex) {
                    score += evaluateField(state, i, j, playerIndex);
                }
            }
        }
        return score;
    }

    /**
     * 对当前字段进行评估
     * @param state
     * @param row
     * @param col
     * @param index
     * @return
     */
    public static int evaluateField(State state, int row, int col, int index) {
        int score = 0;
        for(int direction = 0; direction < 4; direction++) {
            //按方向评估并加和
            score += scoreDirection(state.directions[row][col][direction],
                    index);
        }
        return score;
    }
}

