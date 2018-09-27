package cn.edu.seu.lxk.RenjuAI;

import java.util.Scanner;
import java.util.Stack;

/**
 * 一个再控制台打印棋盘以实现与AI下棋，用于调试，不用整合到botzone的代码中
 * Created by zerlz on 2018/9/18.
 */
public class ConsolePrintChessBoard {
    public static int BOARD_SIZE = 15;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);//获取标准输出对象
        Main AI_player = new Main();//AI
        GameState state = new GameState(15);//局面对象
        Field history[][] = new Field[15][15];//历史棋局

        int index = 1;

        System.out.println("Welcome to Play Gomoku Chess!!\nPlease choose which side you want to stand:\n 1. Black\n 2. White\n ~. Quit");
        int stand = scanner.nextInt();
        System.out.println("You choose " + (stand == 1 ? "Black" : "White") + "\nGame start!\nIf you want to quit the game, please enter \"~\"");



        if(stand == 1){//黑方
            //打印空棋盘，并让玩家输入想要落子的位置代码
            if(AI_player.state == null) {
                printBoard(history);
            }
            scanner = new Scanner(System.in);
            String userInput = scanner.nextLine();
            while(!userInput.equals("~")){//终止符是~
                if (userInput != null) {
                    int col = userInput.charAt(0) - 'A';
                    int row = 15 - Integer.parseInt(userInput.substring(1));
                    Move move = new Move(row, col);
                    history[row][col] = new Field(row, col, index);
                    index = index == 1 ? 2 : 1;
                    state.makeMove(move);//玩家下棋

                    Move resp = AI_player.getMove(state);//AI计算
                    state.makeMove(resp);//AI下棋
                    history[resp.row][resp.col] = new Field(resp.row, resp.col, index);
                    index = index == 1 ? 2 : 1;
                    printBoard(history);
                    userInput = scanner.nextLine();
                }
            }
        } else if(stand == 2){//白方
            //得到第一步，打印历史棋局
            Move resp = AI_player.getMove(state);//AI计算
            state.makeMove(resp);//AI下棋
            history[resp.row][resp.col] = new Field(resp.row, resp.col, index);
            index = index == 1 ? 2 : 1;
            printBoard(history);
            scanner = new Scanner(System.in);
            String userInput = scanner.nextLine();
            while(!userInput.equals("~")) {//终止符是~
                if (userInput != null) {
                    int col = userInput.charAt(0) - 'A';
                    int row = 15 - Integer.parseInt(userInput.substring(1));
                    Move move = new Move(row, col);
                    history[row][col] = new Field(row, col, index);
                    index = index == 1 ? 2 : 1;
                    state.makeMove(move);//玩家下棋

                    resp = AI_player.getMove(state);//AI计算
                    state.makeMove(resp);//AI下棋
                    history[resp.row][resp.col] = new Field(resp.row, resp.col, index);
                    index = index == 1 ? 2 : 1;
                    printBoard(history);
                    userInput = scanner.nextLine();
                }
            }
        }


    }

    /**
     * 打印空棋盘
     */
    public static void printBoard(Field[][] history){
        for(int row = 0; row < BOARD_SIZE; row++){
            //打一个字头
            int rowPosiition = BOARD_SIZE - row;//行位置
            System.out.print((rowPosiition < 10 ? (" " + rowPosiition) : rowPosiition) + " ");//打印行标
            for(int col = 0; col < BOARD_SIZE; col ++){
                if (history[row][col] == null) {
                    System.out.print(" + ");
                } else if (history[row][col].index == 1) {
                    System.out.print(" @ ");
                } else if (history[row][col].index == 2) {
                    System.out.print(" 0 ");
                }
            }
            System.out.println();
        }
        //先留空
        System.out.print("    ");
        for(int col = 0; col < BOARD_SIZE; col ++){
            char colPosition = (char) ('A' + col);//列位置
            System.out.print(Character.toString(colPosition) + "  ");
        }
        System.out.println();
        System.out.println("Please Enter the Position Code which you want to play:");
    }
}
