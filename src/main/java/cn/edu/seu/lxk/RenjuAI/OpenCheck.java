package cn.edu.seu.lxk.RenjuAI;

import org.json.simple.JSONValue;

import java.util.*;

/**
 * Created by zerlz on 2018/9/25.
 */
public class OpenCheck {
    public static void main(String[] args) {
        String inputFull = new Scanner(System.in).nextLine();
        Map<String, List> input = (Map) JSONValue.parse(inputFull);
        Main player = new Main();
        GameState state = new GameState(15);
        Map output = new HashMap();
        Map response = new HashMap();
        List<Map> requests = input.get("requests");
        List<Map> responses = input.get("responses");

        if(((Map<String, Long>) requests.get(0)).get("x").intValue() == -1){//先手方
/*            if(requests.size() == 1) {//先手的第一手
                Move best = player.getMove(state);//正常走，输出
                response.put("x", best.row);
                response.put("y", best.col);
                output.put("response", response);
                System.out.print(JSONValue.toJSONString(output));
            }*/
            if(requests.size() == 2){//先手的第二手，要判断白方第一手是不是落在角落里
                int resp_1_x = ((Map<String, Long>) responses.get(0)).get("x").intValue();//白方第一手棋x
                int resp_1_y = ((Map<String, Long>) responses.get(0)).get("y").intValue();//白方第一手棋y
                if(resp_1_x < 4 || resp_1_x > 10 || resp_1_y < 4 || resp_1_y > 10){
                    //白方第二手下在最边上的四列,黑方也落对称位置的地方
                    response.put("x", 15 - resp_1_x);
                    response.put("y", 15 - resp_1_y);
                    output.put("response", response);
                    System.out.print(JSONValue.toJSONString(output)); return;
                } else {
                    //白方第二手正常下，黑方落在离当前格子4格的位置，即使被换先手也不会太吃亏
                    int req_1_x = ((Map<String, Long>) requests.get(1)).get("x").intValue();//黑方第一手棋x
                    int req_1_y = ((Map<String, Long>) requests.get(1)).get("y").intValue();//黑方第一手棋y

                    if(req_1_x - 4 != req_1_x && req_1_y - 4 != req_1_y){//不和第一手白棋冲突
                        response.put("x", req_1_x - 4);
                        response.put("y", req_1_y - 4);
                        output.put("response", response);
                        System.out.print(JSONValue.toJSONString(output)); return;
                    } else { // 冲突了就换另一个位置
                        response.put("x", req_1_x + 4);
                        response.put("y", req_1_y + 4);
                        output.put("response", response);
                        System.out.print(JSONValue.toJSONString(output)); return;
                    }
                }
            }
        } else{//后手方
/*            if(requests.size() == 1){//白方第一手
                //正常下
                int req_1_x = ((Map<String, Long>) requests.get(0)).get("x").intValue();//黑方第一手棋x
                int req_1_y = ((Map<String, Long>) requests.get(0)).get("y").intValue();//黑方第一手棋y
                Move move = new Move(req_1_x, req_1_y);
                state.makeMove(move);
                Move best = player.getMove(state);
                response.put("x", best.row);
                response.put("y", best.col);
                output.put("response", response);
                System.out.print(JSONValue.toJSONString(output));
            }*/
            if(requests.size() == 2){//白方的第二手
                //考虑是否换手
                int req_1_x = ((Map<String, Long>) requests.get(0)).get("x").intValue();//黑方第一手棋x
                int req_1_y = ((Map<String, Long>) requests.get(0)).get("y").intValue();//黑方第一手棋y

                int req_2_x = ((Map<String, Long>) requests.get(1)).get("x").intValue();//黑方第二手棋x
                int req_2_y = ((Map<String, Long>) requests.get(1)).get("y").intValue();//黑方第二手棋y

                if(req_1_x > 5 && req_1_x < 9 && req_1_y > 5 && req_1_y < 9
                        && req_2_x > 5 && req_2_x < 9 && req_2_y > 5 && req_2_y < 9) {
                    //换手
                    response.put("x", -1);
                    response.put("y", -1);
                    output.put("response", response);
                    System.out.print(JSONValue.toJSONString(output)); return;
                }
            }

        }

        //完成check，正常走
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
