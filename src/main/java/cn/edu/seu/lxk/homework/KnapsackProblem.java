package cn.edu.seu.lxk.homework;

/**
 * Created by zerlz on 2018/9/15.
 * 01背包问题Java实现
 * 参考链接：https://www.cnblogs.com/Christal-R/p/Dynamic_programming.html
 *
 */
public class KnapsackProblem {
    public static int capacity = 50;//背包体积
    public static int items[][] = {
            {0,10,60},
            {0,20,100},
            {0,30,120}
    };//使用一个二维数组表示可以用于背包问题的物体，第一个维度表示物体的序列，下标为[i][]表示第i+1个物体
    //第二个维度则表示该物体的一些属性，[i][0]表示物体的是否被选中，选中为1未选为0；[i][1]表示物体的体积；[i][2]表示物体的价值

    //递归做法
    public static int Knapsack(int capacity, int items[][], int itemCount){
        if(capacity == 0 || itemCount == 0)
            return 0;

        if(items[itemCount-1][1] > capacity)
            return Knapsack(capacity, items, itemCount - 1);
        else return max(Knapsack(capacity - items[itemCount -1][1], items, itemCount -1) + items[itemCount -1][2],
                Knapsack(capacity, items, itemCount - 1));
    }

    //基于动态规划的做法
    public static int DPKnapsack(int capacity, int items[][], int itemCount){
        int count, c;//count表示当前位置的物体个数,c表示当前物体的容量
        int DPTable[][] = new int[itemCount + 1][capacity + 1];//初始化动态规划表

        for(count = 0; count <= itemCount; count++){
            for(c = 0; c <= capacity; c++){
                if(count == 0 || c == 0)
                    DPTable[count][c] = 0;//第一行列为0，因为数量为0或容量为0的情况下最大价值就是0
                else if(items[count - 1][1] <= c)//前一物体的容量小于当前容量
                    DPTable[count][c] = max(items[count - 1][2] + DPTable[count - 1][c - items[count -1][1]], DPTable[count - 1][c]);//取较大的一个是前一状态的最优解
                else
                    DPTable[count][c] = DPTable[count -1][c];
            }
        }

        return DPTable[itemCount][capacity];
    }

    public static int max(int a, int b){ return (a > b)? a : b; }

    public static void main(String[] args) {
        System.out.println("The max value is :" + Knapsack(capacity, items, items.length));
        System.out.println("The max value is :" + DPKnapsack(capacity, items, items.length));
    }
}
