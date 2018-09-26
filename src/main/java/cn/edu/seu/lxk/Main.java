package cn.edu.seu.lxk;


import org.json.simple.JSONValue;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * botzone整合类
 */
public class Main {
    private static final int BOARD_SIZE = 15;

    private long time;
    private long startTime;


    private State state;

    public Main() {
        this.time = (2200 - 100) * 1000000;
    }

    /**
     * 决定是否需要对有威胁的步骤做出回应，采用一个威胁降序排列的步骤列表来进行搜索
     * @param state
     * @return List
     */
    private List<Move> getThreatResponses(Main.State state) {
        int playerIndex = state.currentIndex;
        int opponentIndex = state.currentIndex == 2 ? 1 : 2;

        HashSet<Move> fours = new HashSet<>();
        HashSet<Move> threes = new HashSet<>();
        HashSet<Move> refutations = new HashSet<>();

        HashSet<Move> opponentFours = new HashSet<>();
        HashSet<Move> opponentThrees = new HashSet<>();
        HashSet<Move> opponentRefutations = new HashSet<>();

        // Check for threats first and respond to them if they exist
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == opponentIndex) {
                    opponentFours.addAll(ThreatUtils.getFours(state,
                            state.board[i][j], opponentIndex));
                    opponentThrees.addAll(ThreatUtils.getThrees(state,
                            state.board[i][j], opponentIndex));
                    opponentRefutations.addAll(ThreatUtils.getRefutations
                            (state, state.board[i][j], opponentIndex));
                }
                else if(state.board[i][j].index == playerIndex) {
                    fours.addAll(ThreatUtils.getFours(state, state.board[i][j],
                            playerIndex));
                    threes.addAll(ThreatUtils.getThrees(state, state
                            .board[i][j], playerIndex));
                    refutations.addAll(ThreatUtils.getRefutations(state, state
                            .board[i][j], playerIndex));
                }
            }
        }

        // 棋盘上有活四，下活四
        if(!fours.isEmpty()) {
            return new ArrayList<>(fours);
        }

        //对方有四的话就赌住
        if(!opponentFours.isEmpty()) {
            return new ArrayList<>(opponentFours);
        }

        // 当前下子方有一个活三，考虑下这手三或者对手冲
        if(!threes.isEmpty()) {
            opponentRefutations.addAll(threes);
            return new ArrayList<>(threes);
        }

        //对手有活三，考虑堵或者自己冲
        if(!opponentThrees.isEmpty()) {
            opponentThrees.addAll(refutations);
            return new ArrayList<>(opponentThrees);
        }

        return new ArrayList<>();
    }

    /**
     * 为当前的局面生成合适的下子顺序集(经过剪枝)，只在有子的附近下，发现有威胁的步骤优先回应有威胁的步骤
     * @param state
     * @return
     */
    private List<Move> getSortedMoves(State state) {
        //空棋盘的话下中间
        if(state.getMoves() == 0) {
            List<Move> moves = new ArrayList<>();
            moves.add(new Move(state.board.length / 2, state.board.length / 2));
            return moves;
        }

        List<Move> threatResponses = getThreatResponses(state);
        if(!threatResponses.isEmpty()) {
            return threatResponses;
        }

        List<ScoredMove> scoredMoves = new ArrayList<>();

        //在临近的位置找到空位并对该位置进行评估
        List<Move> moves = new ArrayList<>();
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == 0) {
                    if(state.hasAdjacent(i, j, 2)) {
                        int score = Evaluator.evaluateField(state, i, j,
                                state.currentIndex);
                        scoredMoves.add(new ScoredMove(new Move(i, j), score));
                    }
                }
            }
        }

        //按分数排序
        Collections.sort(scoredMoves);
        for(ScoredMove move : scoredMoves) {
            moves.add(move.move);
        }
        return moves;
    }

    /**
     * negamax算法主体
     * @param state 要搜索的局面，随递归层数改变
     * @param depth 深度
     * @param alpha
     * @param beta
     * @return
     * @throws InterruptedException 超时异常抛出
     */
    private int negamax(State state, int depth, int alpha, int beta)
            throws InterruptedException {
        if(Thread.interrupted() || (System.nanoTime() - startTime) > time) {
            throw new InterruptedException();
        }
        if(state.terminal() != 0 || depth == 0) {
            return Evaluator.evaluateState(state, depth);
        }

        int value;
        int best = Integer.MIN_VALUE;

        List<Move> moves = getSortedMoves(state);

        for (Move move : moves) {
            //尝试步骤，更新状态
            state.makeMove(move);
            //新状态递归negamax
            value = -negamax(state, depth - 1, -beta, -alpha);
            //回撤上一状态
            state.undoMove(move);

            //更新分数、alpha值
            if(value > best) {
                best = value;
            }
            if(best > alpha) alpha = best;
            if(best >= beta) {
                break;
            }
        }
        return best;
    }

    /**
     * 调用nagemax，获取各点分数最高的步骤，并把它们按分数排序
     * @param depth
     * @return
     */
    private List<Move> searchMoves(State state, List<Move> moves, int depth)
            throws InterruptedException {
        //存放各节点最佳步骤的list
        List<ScoredMove> scoredMoves = new ArrayList<>();
        for(Move move : moves) {
            scoredMoves.add(new ScoredMove(move, Integer.MIN_VALUE));
        }

        //设置alpha和beta的值
        int alpha = -11000;
        int beta = 11000;
        int best = Integer.MIN_VALUE;

        for(ScoredMove move : scoredMoves) {
            state.makeMove(move.move);
            move.score = -negamax(state, depth - 1, -beta, -alpha);
            state.undoMove(move.move);
            if(move.score > best) best = move.score;
            if(best > alpha) alpha = best;
            if(best >= beta) break;
        }

        //结果排序
        scoredMoves.sort((move1, move2) -> move2.score - move1.score);

        moves.clear();
        for(ScoredMove move : scoredMoves) moves.add(move.move);
        return moves;
    }

    /**
     * negamax搜索的包装方法，实际调用getSortedMoves()方法，超时异常处理，超时停止并及时返回当前最佳结果
     * @param startDepth
     * @param endDepth
     * @return
     */
    private Move iterativeDeepening(int startDepth, int endDepth)  {
        this.startTime = System.nanoTime();
        List<Move> moves = getSortedMoves(state);
        if(moves.size() == 1) return moves.get(0);
        for(int i = startDepth; i <= endDepth; i++) {
            try {
                moves = searchMoves(state, moves, i);
            } catch (InterruptedException e) {
                break;
            }
        }
        return moves.get(0);
    }

    /**
     * 获取最佳结果的上层入口
     * @param gameState
     * @return
     */
    public Move getMove(GameState gameState) {
        //从GameState对象创建一个适合AI进行搜索的含hash置换表的State对象
        this.state = new State(BOARD_SIZE);
        List<Move> moves = gameState.getMovesMade();
        moves.forEach((move) -> state.makeMove(move));

        //调用包装方法做2到8层的搜索尝试
        Move best = iterativeDeepening(2, 8);
        return best;
    }


    //内置对象，实现comparable接口，以方便使用Collection.sort()方法
    private class ScoredMove implements Comparable<ScoredMove> {
        public Move move;
        public int score;
        public ScoredMove(Move move, int score) {
            this.move = move;
            this.score = score;
        }

        @Override
        public int compareTo(ScoredMove move) {
            return move.score - this.score;
        }
    }

    //主程序入口，暂未考虑换手问题
    public static void main(String[] args) {
        String inputFull = new Scanner(System.in).nextLine();
        Map<String, List> input = (Map) JSONValue.parse(inputFull);
        Main player = new Main();
        GameState state = new GameState(BOARD_SIZE);
        List<Map> requests = input.get("requests");
        List<Map> responses = input.get("responses");

        List<Move> moves = new ArrayList<>();
        //request size比 response大1
        for(int i = 0; i < requests.size(); i++){
            //遍历两个回复数组
            int req_x = ((Map<String, Long>)requests.get(i)).get("x").intValue();
            int req_y = ((Map<String, Long>)requests.get(i)).get("y").intValue();
            if(req_x != -1 && req_y != -1){
                state.makeMove(new Move(req_x, req_y));
            }
            if(i < responses.size()) {
                int resp_x = ((Map<String, Long>) responses.get(i)).get("x").intValue();
                int resp_y = ((Map<String, Long>) responses.get(i)).get("y").intValue();
                if(resp_x != -1 && resp_y != -1){
                    state.makeMove(new Move(resp_x, resp_y));
                }
            }
        }
        //long s = System.currentTimeMillis();
        Move best = player.getMove(state);
        //long e = System.currentTimeMillis();
        //System.out.println(s-e);
        //结果封装
        Map output = new HashMap();
        Map response = new HashMap();
        response.put("x",best.row);
        response.put("y",best.col);
        output.put("response", response);
        System.out.print(JSONValue.toJSONString(output));
    }

    /**
     * Created by zerlz on 2018/9/18.
     * 该类封装了一个棋盘盘面上的一个位置对象
     */
    static class Field {
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
    }

    /**
     * 包含hash置换表的局面类State
     */
    static class State {
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

    /**
     * 局势评估方法类封装
     */
    static class Evaluator {

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

    /**
     * Created by zerlz on 2018/9/21.
     * 威胁局面的情况判断工具类
     */
    static class ThreatUtils {

        private static final List<ThreatPattern> REFUTATIONS;
        private static final List<ThreatPattern> THREES;
        private static final List<ThreatPattern> FOURS;

        static {
            THREES = new ArrayList<>();
            FOURS = new ArrayList<>();
            REFUTATIONS = new ArrayList<>();
            //活3，两头没堵，所以要六个空
            THREES.add(new ThreatPattern(new int[] {0, 1, 1, 1, 0, 0}, new int[]
                    {0, 4, 5}));
            THREES.add(new ThreatPattern(new int[] {0, 0, 1, 1, 1, 0}, new int[]
                    {0, 1, 5}));
            THREES.add(new ThreatPattern(new int[] {0, 1, 0, 1, 1, 0}, new int[]
                    {0, 2, 5}));
            THREES.add(new ThreatPattern(new int[] {0, 1, 1, 0, 1, 0}, new int[]
                    {0, 3, 5}));
            //成四，不分眠四和活四
            FOURS.add(new ThreatPattern(new int[] {1, 1, 1, 1, 0}, new int[] {4} ));
            FOURS.add(new ThreatPattern(new int[] {1, 1, 1, 0, 1}, new int[] {3} ));
            FOURS.add(new ThreatPattern(new int[] {1, 1, 0, 1, 1}, new int[] {2} ));
            FOURS.add(new ThreatPattern(new int[] {1, 0, 1, 1, 1}, new int[] {1} ));
            FOURS.add(new ThreatPattern(new int[] {0, 1, 1, 1, 1}, new int[] {0} ));

            //可以冲四的模式
            REFUTATIONS.add(new ThreatPattern(new int[] {1, 1, 1, 0, 0}, new
                    int[] {3, 4}));
            REFUTATIONS.add(new ThreatPattern(new int[] {1, 1, 0, 0, 1}, new
                    int[] {2, 3} ));
            REFUTATIONS.add(new ThreatPattern(new int[] {1, 0, 0, 1, 1}, new
                    int[] {1, 2} ));
            REFUTATIONS.add(new ThreatPattern(new int[] {0, 0, 1, 1, 1}, new
                    int[] {0, 1} ));
        }

        /**
         * 在属于棋盘上检查一个field，查看是否有一个中断的三连或一个连续三连(0XXX0和0X0XX0)。
         * @param playerIndex
         * @return
         */
        public static List<Move> getThrees(State state, Field field, int
                playerIndex) {
            return getThreatMoves(THREES, state, field, playerIndex);
        }

        /**
         * 在属于棋盘上检查一个field，查看是否有一个中断的四连或一个连续四连(XXXX0和X0XXX)。
         * @param playerIndex
         * @return
         */
        public static List<Move> getFours(State state, Field field, int
                playerIndex) {
            return getThreatMoves(FOURS, state, field, playerIndex);
        }
        /**
         * 在属于棋盘上检查一个field，查看是否有可以成四的
         * @param playerIndex
         * @return
         */
        public static List<Move> getRefutations(State state, Field field, int
                playerIndex) {
            return getThreatMoves(REFUTATIONS, state, field, playerIndex);
        }

        /**
         * 在游戏状态下的一个field中搜索威胁，如果发现每个威胁，返回攻击/防御动作。
         * @param patternList
         * @param state
         * @param field
         * @param playerIndex
         * @return
         */
        private static List<Move> getThreatMoves(
                List<ThreatPattern> patternList,
                State state,
                Field field,
                int playerIndex) {
            List<Move> threatMoves = new ArrayList<>();
            //循环遍历四个方向
            for(int direction = 0; direction < 4; direction++) {
                Field[] directionArray = state.directions[field.row][field.col]
                        [direction];
                for(ThreatPattern pattern : patternList) {
                    //识别棋型
                    int patternIndex = matchPattern(directionArray, pattern
                            .getPattern(playerIndex));
                    if(patternIndex != -1) {
                        for(int patternSquareIndex : pattern.getPatternSquares()) {
                            Field patternSquareField = directionArray[patternIndex +
                                    patternSquareIndex];
                            threatMoves.add(new Move(patternSquareField.row,
                                    patternSquareField.col));
                        }
                    }
                }
            }
            return threatMoves;
        }

        /**
         * 辅助函数，在一维的field数组中寻找适合的棋型
         * @param direction 待匹配数组
         * @param pattern 棋型
         * @return 返回匹配位置，或-1表示未找到匹配
         */
        private static int matchPattern(Field[] direction, int[] pattern) {
            for(int i = 0; i < direction.length; i++) {
                //在边界内寻找匹配的棋型
                if(i + (pattern.length - 1) < direction.length) {
                    int count = 0;
                    for(int j = 0; j < pattern.length; j++) {
                        if(direction[i + j].index == pattern[j]) {
                            count++;
                        } else {
                            break;
                        }
                    }
                    //返回匹配位置
                    if(count == pattern.length) {
                        return i;
                    }
                } else {
                    break;
                }
            }
            return -1;
        }

    }

    /**
     * Created by zerlz on 2018/9/20.
     * 威胁模式
     */
    static class ThreatPattern {
        private int[][] pattern;
        private final int[] patternSquares;

        /**
         * 构造方法,pattern表示为一维数组，其中0为空位，1为存在的棋子
         * @param pattern
         * @param patternSquares
         */
        public ThreatPattern(int[] pattern, int[] patternSquares) {
            //在pattern[][]中存储每个player的pattern
            this.pattern = new int[2][1];
            this.pattern[0] = pattern;
            this.pattern[1] = switchPattern(pattern);
            this.patternSquares = patternSquares;
        }

        /**
         * 从player的角度获得pattern
         * @param playerIndex 玩家标示
         * @return
         */
        public int[] getPattern(int playerIndex) {
            return this.pattern[playerIndex - 1];
        }

        /**
         * 返回pattern中的进攻/防守方的square pattern
         * @return int[] containing all the square indices
         */
        public int[] getPatternSquares() {
            return this.patternSquares;
        }

        /**
         * 切换player的视角看pattern
         * @param pattern
         * @return
         */
        private int[] switchPattern(int[] pattern) {
            int[] patternSwitched = new int[pattern.length];
            for(int i = 0; i < pattern.length; i++) {
                if(pattern[i] == 1) {
                    patternSwitched[i] = 2;
                }
            }
            return patternSwitched;
        }
    }

    /**
     * Created by zerlz on 2018/9/18.
     * 该类封装了棋盘上的一次下子动作
     */
    static class Move {
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

    /**
     * Created by zerlz on 2018/9/19.
     * 当前游戏状态类
     */
    static class GameState {

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

}