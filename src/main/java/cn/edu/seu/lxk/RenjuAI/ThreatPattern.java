package cn.edu.seu.lxk.RenjuAI;

/**
 * Created by zerlz on 2018/9/20.
 * 威胁模式
 */
public class ThreatPattern {
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

