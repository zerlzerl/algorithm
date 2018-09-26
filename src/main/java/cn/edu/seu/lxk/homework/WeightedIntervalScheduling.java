package cn.edu.seu.lxk.homework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 区间问题作业代码，区间问题也可称之为工作调度问题，在单一时间轴上，一个区间可表示一个工作，一个工作有起始时间，终止时间和价值
 * 对区间最优的规划即对工作的最优优化，在一个时间段内可以达到最大的价值
 * Created by zerlz on 2018/9/19.
 */
public class WeightedIntervalScheduling {
    private int[][] jobs;	//作业队列，每个作业都是 [id, startTime, finishTime, value]
    private int[] jobsSchedule;		//记忆数组
    private ArrayList<Integer> includedJobs = new ArrayList<>();

    public void calcSchedule(int[][] inputJobs){
        jobs= inputJobs;
        jobsSchedule = new int[jobs.length];

        Arrays.sort(jobs, Comparator.comparingInt(a -> a[2]));

        jobsSchedule[0]=0;

        for(int i = 1; i<jobs.length; i++){
            jobsSchedule[i] = Math.max( jobs[i][3]+ jobsSchedule[latestCompatible(i)],   jobsSchedule[i-1] );
        }

        System.out.println("记忆数组中的工作: " + Arrays.toString(jobsSchedule));
        System.out.println("最右子问题的收益：" + jobsSchedule[jobsSchedule.length-1]);

        findSolutionRecursive(jobsSchedule.length-1);
        System.out.println("\n最优结果:");
        for(int i=includedJobs.size()-1; i>=0; i--){
            System.out.println(getJobInfo(includedJobs.get(i)));
        }
    }

    //在job i开始之前找到完成的job的索引(存放在按完成时间排序的job[][]数组)
    private int latestCompatible(int i){
        int low = 0, high = i - 1;

        while (low <= high){		//二分查找
            int mid = (low + high) / 2;		//向下取整
            if (jobs[mid][2] <= jobs[i][1]) {
                if (jobs[mid + 1][2] <= jobs[i][1])
                    low = mid + 1;
                else
                    return mid;
            }
            else
                high = mid - 1;
        }
        return 0;	//没有找到合适的工作。返回0，使用job[0]中的的值
    }

    //迭代版本的实现
    public void findSolutionIterative(int j){
        while (j>0){	//j==0停止
            int compatibleIndex = latestCompatible(j);	//找到与j兼容的最新完成工作
            if(jobs[j][3]+ jobsSchedule[compatibleIndex] > jobsSchedule[j-1]){	//包含作业j的情况(最优子问题)
                includedJobs.add(j);	//将当前工作的索引添加到数组
                j=compatibleIndex;		//更新j
            }
            else{	//如果没有包含job j，则将job j从solution中删除&查看job 1到(j-1)
                j=j-1;
            }
        }
    }

    //递归版本的实现
    private void findSolutionRecursive(int j){
        if(j==0){	//递归返回点
            return;
        }
        else{
            int compatibleIndex = latestCompatible(j);	//找到与j兼容的最新完成工作
            if(jobs[j][3]+ jobsSchedule[compatibleIndex] > jobsSchedule[j-1]){	//包含作业j的情况(最优子问题)
                includedJobs.add(j);	//将当前工作的索引添加到数组
                findSolutionRecursive(compatibleIndex);	//递归找到最新的兼容的工作
            }
            else{	//如果没有包含job j，则将job j从solution中删除&查看job 1到(j-1)
                findSolutionRecursive(j-1);
            }
        }
    }

    //打印工作流的方法
    private String getJobInfo(int jobIndex){
        return "Job " + jobs[jobIndex][0] + ":  Time (" + jobs[jobIndex][1] +"-" + jobs[jobIndex][2] +") Value=" + jobs[jobIndex][3];
    }


    public static void main(String args[]) {
        WeightedIntervalScheduling scheduler = new WeightedIntervalScheduling();
        int[][] inputJobs = {{0,0,0,0},		//空0项对齐索引
                {1, 0, 6, 3},
                {2, 1, 4, 5},
                {3, 3, 5, 5},
                {4, 3, 8, 8},
                {5, 4, 7, 3},
                {6, 5, 9, 7},
                {7, 6, 10, 3},
                {8, 8, 11, 4}
        };
        scheduler.calcSchedule(inputJobs);
    }
}
