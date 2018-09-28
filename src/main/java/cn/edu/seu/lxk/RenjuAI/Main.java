package cn.edu.seu.lxk.RenjuAI;


import org.json.simple.JSONValue;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 主程序入口
 */
public class Main {
    private static final int BOARD_SIZE = 15;

    private long time = 2500000000L;
    private long startTime;


    protected State state;


    /**
     * 决定是否需要对有威胁的步骤做出回应，采用一个威胁降序排列的步骤列表来进行搜索
     * @param state
     * @return List
     */
    private List<Move> getThreatResponses(State state) {
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
        if(state.terminal() != 0 || depth == 0) {//找到结束状态或者是递归深度达到，就返回当前状态和深度下对于局面的评估分数
            return Evaluator.evaluateState(state, depth);
        }

        int value;
        int best = Integer.MIN_VALUE;

        List<Move> moves = getSortedMoves(state);

        for (Move move : moves) {
            //尝试步骤，更新状态
            state.makeMove(move);
            //新状态递归negamax,因为
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

    /**
     * 判断换手的方法
     * @return
     */
    //TODO 未完成
    public static boolean isChangeHands(List<Map> requests, List<Map> responses){
        //判断自己是不是白方且第三手，不是白方直接返回false
        int req_0_x = ((Map<String, Long>) requests.get(0)).get("x").intValue();//黑方的第一手是(-1,-1)
        int req_0_y = ((Map<String, Long>) requests.get(0)).get("x").intValue();
        if(requests.size() == 2 && req_0_x != -1 && req_0_y != -1){//requests有两个，且第一个不是(-1,-1)则说明是白方且可以换手
            //对当前的局势进行评估

        }else{
            return false;
        }

       return false;
    }


    //内置对象，实现comparable接口，以方便使用Collection.sort()方法
    //主程序入口，暂未考虑换手问题
    public static void main(String[] args) {
        String inputFull = new Scanner(System.in).nextLine();
        Map<String, List> input = (Map) JSONValue.parse(inputFull);
        Main player = new Main();
        GameState state = new GameState(BOARD_SIZE);
        Map output = new HashMap();
        Map response = new HashMap();
        List<Map> requests = input.get("requests");
        List<Map> responses = input.get("responses");


        List<Move> moves = new ArrayList<>();
        //request size比 response大1
        for (int i = 0; i < requests.size(); i++) {
            //遍历两个回复数组
            int req_x = ((Map<String, Long>) requests.get(i)).get("x").intValue();
            int req_y = ((Map<String, Long>) requests.get(i)).get("y").intValue();
            if (req_x != -1 && req_y != -1) {
                state.makeMove(new Move(req_x, req_y));
            }
            if (i < responses.size()) {
                int resp_x = ((Map<String, Long>) responses.get(i)).get("x").intValue();
                int resp_y = ((Map<String, Long>) responses.get(i)).get("y").intValue();
                if (resp_x != -1 && resp_y != -1) {
                    state.makeMove(new Move(resp_x, resp_y));
                }
            }

            //long s = System.currentTimeMillis();
            Move best = player.getMove(state);
            //long e = System.currentTimeMillis();
            //System.out.println(s-e);
            //结果封装
            response.put("x", best.row);
            response.put("y", best.col);
            output.put("response", response);
            System.out.print(JSONValue.toJSONString(output));
        }
    }
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


}