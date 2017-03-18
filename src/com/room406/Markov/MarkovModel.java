package com.room406.Markov;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;

import com.room406.utils.GetChineseNum;


public class MarkovModel {
	
	
	/* 
	* countMatrixA : 
	 *   ALL B M E S 
	 * B *   * * * * 
	 * M *   * * * * 
	 * E *   * * * * 
	 * S *   * * * * 
	 *
	 */  
	private long [][] countMatrixA;
	/*
	* countMatrixB : 
	 *   ALL C1 C2 C3 CN C5168 
	 * B  *   *  *  *  *  1/ALL+5168 
	 * M  *   *  *  *  *  1/ALL+5168 
	 * E  *   *  *  *  *  1/ALL+5168 
	 * S  *   *  *  *  *  1/ALL+5168 
	 *  
	 * 语料中不同中文字符数 => 5167 
	 *  行 -> 4 
	 *  列 -> 5169 
	 */  
	private long[][] countMatrixB;
	
	/*
	 * MatrixA:a(ij)/ALL(i0)
	 */
	private double[][] matrixA;
	/*
	 * MatrixB:b(ij)/ALL(i0)
	 */
	private double[][] MatrixB;
	/*
	 * MatrixPI
	 */
	private double[] PI;
	
	/*
	 * 默认语料库的MarkovModel
	 */
	public MarkovModel() {
		setCountMatrixA("D:/NLP/icwb2-data/training/msr_training.utf8");
		setCountMatrixB("D:/NLP/icwb2-data/training/msr_training.utf8");
		
		setMatrixA();
		setMatrixB();
		setPI();
	}
	/*
	 * 自选语料库的MarkovModel
	 */
	public MarkovModel(String corpus) {
		setCountMatrixA(corpus);
		setCountMatrixB(corpus);
		
		setMatrixA();
		setMatrixB();
		setPI();
	}
	
	private void setCountMatrixA(String corpus) {  
		long [][] countMatrixA=new long[4][5];
		BufferedReader br=null;
        try {  
            br=new BufferedReader(new InputStreamReader(new FileInputStream(corpus),"UTF-8"));  
            //从语料库文本中按行读取（问题：会不会将文本中换行的词组分开，从而影响数据的准确获取）
            String line = null;
            //用来存储每次处理过后的词组
            String preWord = null;  
            
            while ((line = br.readLine()) != null) {
                String[] words = line.split(" ");  
                for (int i=0; i<words.length; i++) { 
                	//去掉词组首尾空格，以保证准确性
                    String word = words[i].trim();  
                    int length = word.length();  
                   
                    if (length < 1){
                    	continue;  
                    } 
                    
                    if (length == 1) {  
                        countMatrixA[3][0]++;  
                        if (preWord != null) {  
                            if (preWord.length() == 1){
                            	countMatrixA[3][4]++;  
                            }  
                            else{
                            	countMatrixA[2][4]++;  
                            }  
                        }  
                    } else {  
                        countMatrixA[2][0]++;  
                        countMatrixA[0][0]++;  
                        if (length > 2) {  
                            countMatrixA[1][0] += length-2;  
                            countMatrixA[0][2]++;  
                            if (length-2 > 1) {  
                                countMatrixA[1][2] += length-3;  
                            }  
                            countMatrixA[1][3]++;  
                        } else {  
                            countMatrixA[0][3]++;  
                        }  
                          
                        if (preWord != null) {  
                            if (preWord.length() == 1) {  
                                countMatrixA[3][1]++;  
                            } else {  
                                countMatrixA[2][1]++;  
                            }  
                        }  
                    }  
                    preWord = word;  
                }  
            }  
            br.close();
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        this.countMatrixA=countMatrixA;
    }  
	
	//矩阵构建需要语料库参数！！！！！！！！！
	private void setCountMatrixB(String corpus) { 
		long[][] countMatrixB=new long[4][5169];
		//初始化计数数组
        for (int row = 0; row < countMatrixB.length; row++) {  
            Arrays.fill(countMatrixB[row], 1);  
            countMatrixB[row][0] = 5168;
        }
         //获得给汉字编号的hashmap
        Map<Character, Integer> dict = GetChineseNum.getchineseNo();  
        try {  
            BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(corpus),"UTF-8"));  
            String line = null;  
            while ((line = br.readLine()) != null) {  
                String[] words = line.split(" ");
                //以词组为单位，针对每个单词的不同状态计数
                for (int i=0; i<words.length; i++) {  
                    String word = words[i].trim(); 
                    if (word.length() < 1){
                    	continue;  
                    }  
                    //状态：S型计数+1 S型对应字符的计数+1，
                    if (word.length() == 1) {  
                        int index = dict.get(word.charAt(0));  
                        countMatrixB[3][0]++;  
                        countMatrixB[3][index]++;  
                    } else {
                    	//多字词组处理
                        for (int j=0; j<word.length(); j++) {  
                            int index = dict.get(word.charAt(j));  
                            if (j == 0) {  
                            	//状态：B型计数+1 B型对应字符计数+1
                            	countMatrixB[0][0]++;  
                            	countMatrixB[0][index]++;  
                            } else if (j == word.length()-1) { 
                            	//状态：E型计数+1 E型对应字符计数+1
                            	countMatrixB[2][0]++;  
                            	countMatrixB[2][index]++;  
                            } else { 
                            	//状态：M型计数+1 M型对应字符计数+1
                            	countMatrixB[1][0]++;  
                            	countMatrixB[1][index]++;  
                            }  
                        }  
                    }  
                      
                }  
            }  
            br.close();
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
          
//        //打印计数数组
//        System.out.println(" ===== matrixBcountMatrixA ===== ");  
//        for (int i=0; i<countMatrixB.length; i++){
//        	System.out.println(Arrays.toString(countMatrixB[i]));  
//        }
        
//        try {  
//            PrintWriter bOut = new PrintWriter(new File("D:/NLP/icwb2-data/output/matrixB2.txt"));  
//            for (int row = 0; row < B.length; row++) {  
//                for (int col = 0; col < B[row].length; col++) {  
//                    bOut.print(B[row][col] + " ");  
//                }  
//                bOut.println("");  
//                bOut.flush();  
//            }  
//            bOut.close();  
//            System.out.println("Finish write B to file ");  
//        } catch (FileNotFoundException e) {  
//            e.printStackTrace();  
//        }  
     this.countMatrixB=countMatrixB;
}	
	
	private long [][] getCountMatrixA(){
		return countMatrixA;
	}
	
	private long[][] getCountMatrixB(){
		return countMatrixB;
	}
	
	public double[][] getMatrixA(){
		return matrixA;
	}
	
	private  void setMatrixA(){
		long [][] countA=getCountMatrixA();
        System.out.println();
		 System.out.println(" matrixA");  
	        //计算状态转移矩阵：从已统计好的计数矩阵计算状态转移概率矩阵
	        double[][] A = new double[4][4];  
	        for (int i=0; i<A.length; i++){
	        	for (int j=0; j<A[i].length; j++){
	        		//一个状态的转移概率：转移到的状态的频数/状态出现的频数，状态对应的行概率之和为一
	        		A[i][j] = (double)countA[i][j+1]/ countA[i][0];  
	        	}
	        } 
	        for (int i=0; i<A.length; i++){
	        	System.out.println(Arrays.toString(A[i]));  
	        }
	        this.matrixA=A;
	}
	
	public double[][] getMatrixB(){
		return MatrixB;
	}
	
	private void setMatrixB(){
		//计算countMatrixB
        long[][] countB=getCountMatrixB();
		 //计算MatrixB
        double[][] B = new double[countB.length][countB[0].length];  
        System.out.println();
        System.out.println("===MatrixB前50列==");
        for (int row = 0; row < B.length; row++) {
            for (int col = 0; col < B[row].length; col++) {
            	//一个字符的某状态频数/某状态出现频数
                B[row][col] = (double) countB[row][col] / countB[row][0];  
                //因为混合矩阵太大，只选择性输出前50列；
                if (col < 50) {
                    System.out.print(B[row][col] + " ");  
                }
            }
            //矩阵换行
            System.out.println("");
        } 
        this.MatrixB=B;
	} 
	
	public double[] getPI(){
		return PI;
	}
	
	private  void setPI(){
		
		long [][] countA=getCountMatrixA();
        System.out.println();
		System.out.println(" ==向量Pi== ");  
        //计算初始状态向量
        double[] pi = new double[4];  
        //词组总数：长词组+单字词组
        long allWordcountMatrixA = countA[2][0] + countA[3][0];  
        //初始概率只有两种：长词组/词组总数，单个字数/词组总数，中间字与尾字不存在初始概率
        pi[0] = (double)countA[2][0] / allWordcountMatrixA;  
        pi[3] = (double)countA[3][0] / allWordcountMatrixA;
        System.out.println(Arrays.toString(pi));
        this.PI=pi;
	}
	
	public static void main(String[] args) {
		
	}
}
