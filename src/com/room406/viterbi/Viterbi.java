package com.room406.viterbi;

import java.math.BigDecimal;
import java.util.Arrays;

import com.room406.Markov.MarkovModel;
import com.room406.utils.GetChineseNum;

public class Viterbi {
	public static void main(String[] args) {
		MarkovModel gp=new MarkovModel();
		Integer []charNo=GetChineseNum.getValueByKey("这并不是一种最好的处理技术，因为这有可能低估或高估真实概率，更");
		// 这  并  不是  一种  最好  的  处理  技术  ，  因为  这有  可能  低估  或  高估  真实  概率  ，  更 
        System.out.println();
		System.out.println("分词结果 <"+toViterbi(gp.getPI(),gp.getMatrixA(),gp.getMatrixB(),charNo)+">");
		}
	/*
	 * viterbi对具体的目标句子分词。
	 * 参数：三个markov的模型参数，PI,A,B；
	 * 		sentences，是一个句子中每个字符在map中对于的编号
	 * 		这样，用编号代表字符以便进行矩阵的计算，一个sentences代表一个观察词序。sentences的得到需要预处理；
	 */
	public static String toViterbi(double[] PI, double[][] A, double[][] B, Integer[] sentences) {  
        StringBuilder ret = new StringBuilder();
        //matrix，past数组：4行，列为待分割句子数组的长度。注意，两个数组的元素类型不一样
        //matrix数组用来存储每次计算的局部概率；last数组用以记录最短路径
        double[][] matrix = new double[PI.length][sentences.length];
        int[][] past = new int[PI.length][sentences.length];  
       
        int supplementStartColumn = -1;  
        //大数数组
        BigDecimal[][] supplement = null; //new BigDecimal[][];  
         
        //计算初始状态的概率，即t0时刻：初始隐藏状态概率*P(句子首字符|隐藏状态)
        for (int row=0; row<matrix.length; row++){
        	matrix[row][0] = PI[row] * B[row][sentences[0]];  
        }
        //col是词序长度，以此为外层循环的循环变量
        for (int col=1; col<sentences.length; col++) { 
            if (supplementStartColumn > -1) { //Use supplement BigDecimal matrix  
                for (int row=0; row<matrix.length; row++) {  
                    BigDecimal max = new BigDecimal(0d);  
                    int last = -1;
                    //与else分支的原理一直，只是用大数类型来计算式子，且大数数组的行列要进行调整
                    for (int r=0; r<matrix.length; r++) {
                        BigDecimal value = supplement[r][col-1-supplementStartColumn].multiply(new BigDecimal(A[r][row])).multiply(new BigDecimal(B[row][sentences[col]]));  
                        if (value.compareTo(max) > 0) {  
                            max = value;  
                            last = r;  
                        }  
                    }
                    
                    supplement[row][col-supplementStartColumn] = max;  
                    past[row][col] = last;  
                }  
            } else {  
            	
                boolean switchSupplement = false;
                //row标志将要转移到的状态，求出每个将要转移到的状态下的最大概率，存入matrix[row][col]中
                for (int row=0; row<matrix.length; row++) {  
                    double max = 0;  
                    int last = -1;
                    //转移到的状态一定时，从不同状态转移的最大概率
                    for (int r=0; r<matrix.length; r++) {
                    	//δt+1(i)=max[δt(j)aji]bi(ot+1) 公式-->δt(j)*aji*bi(ot+1)
                        double value = matrix[r][col-1] * A[r][row] * B[row][sentences[col]];  
                       //取下一个时刻状态中的最大局部概率
                        if (value > max) {
                            max = value;  
                            //概率最大时的路径：是转移前状态的横坐标
                            last = r;  
                        }  
                    } 
                    //matrix记录t(i)到t(i+1)的不同转移后状态的最大概率
                    matrix[row][col] = max;
                    //past记录t(i)到t(i+1)的不同转移后状态的最大概率对应的转移前行下标
                    past[row][col] = last;
                    
                    //判断局部最大概率的大小？有何意义？
                    if (max < 1E-250)  
                        switchSupplement = true;  
                }  
                 
                //在处理词序第一个字符时，就判断得到的概率的大小，以决定是否要使用大数数组，这样是为了在精度与性能上权衡
                //Really small data, should switch to supplement BigDecimal matrix now, or we will loose accuracy soon  
                if (switchSupplement) {  
                    supplementStartColumn = col;//supplementStartColumn=1，且只会发生一次
                    supplement = new BigDecimal[PI.length][sentences.length - supplementStartColumn];  
                    for (int row=0; row<matrix.length; row++) {
                    	//matrix的第一列存放的是初始概率积，故从第二列开始，将得到的大数放入大数数组中
                        supplement[row][col - supplementStartColumn] = new BigDecimal(matrix[row][col]);  
                    }  
                }  
            }  
        }
          
        int lastX = -1;
        //这里同样要区分是否为大数数组
        if (supplementStartColumn > -1) {  
            BigDecimal max = new BigDecimal(0d);  
            int column = supplement[0].length-1;  
            for (int row=0; row<supplement.length; row++) {  
                if (supplement[row][column].compareTo(max) > 0) {  
                    max = supplement[row][column];  
                    lastX = row;  
                }  
            }  
        } else { 
            double max = 0;
            //取matrix最后一列中的最大概率，即取词序的最大概率，并记下取最大概率的行号index
            for (int row=0; row<matrix.length; row++)  
                if (matrix[row][sentences.length-1] > max) {  
                    max = matrix[row][sentences.length-1];  
                    //last数组的最后一列存放的是matrix倒数第二列的4个最大概率 
                    lastX = row;
                } 
        }  
        System.out.println();
        System.out.println("==局部概率矩阵==");
        for (int i=0; i<matrix.length; i++){
        	System.out.println(Arrays.toString(matrix[i]));
        }
        System.out.println();
        System.out.println("==最短路径矩阵==");
        for (int i=0; i<past.length; i++){
        	System.out.println(Arrays.toString(past[i]));
        }
        
        
        //GetChineseNum gcn=new GetChineseNum();
        for(int x=lastX,y=sentences.length-1;y>=0;y--){
        	switch (x) {
			case 0://B
				ret.append(GetChineseNum.getKeyOfValue(sentences[y])+" ");
				break;
			case 1://M
				ret.append(GetChineseNum.getKeyOfValue(sentences[y]));
				break;
			case 2://E
				ret.append(" "+GetChineseNum.getKeyOfValue(sentences[y]));
				break;
			case 3://S
				ret.append(" "+GetChineseNum.getKeyOfValue(sentences[y])+" ");
				break;
			default:
				ret.append(" ");
				break;
			}
        	x=past[x][y];
        }

        return ret.reverse().toString();
    }  
}
