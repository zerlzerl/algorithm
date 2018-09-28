package cn.edu.seu.lxk.RenjuAI;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zerlz on 2018/9/21.
 * 威胁局面的情况判断工具类
 */
public class ThreatUtils {

    private static final List<ThreatPattern> REFUTATIONS;
    private static final List<ThreatPattern> THREES;
    private static final List<ThreatPattern> FOURS;

    static {
        THREES = new ArrayList<>();
        FOURS = new ArrayList<>();
        REFUTATIONS = new ArrayList<>();
        //活3，两头没堵，所以要六个空，以及0的索引
        THREES.add(new ThreatPattern(new int[] {0, 1, 1, 1, 0, 0}, new int[]{0, 4, 5}));
        THREES.add(new ThreatPattern(new int[] {0, 0, 1, 1, 1, 0}, new int[]{0, 1, 5}));
        THREES.add(new ThreatPattern(new int[] {0, 1, 0, 1, 1, 0}, new int[]{0, 2, 5}));
        THREES.add(new ThreatPattern(new int[] {0, 1, 1, 0, 1, 0}, new int[]{0, 3, 5}));
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

