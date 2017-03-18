package com.room406.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

//语料库处理工具
public class GetChineseNum {

	//<key,value>=<word,no>，其中标点符号为中文符号
	private static Map<Character,Integer> chineseNo=new HashMap<Character,Integer>();
	
	//给语料库中的不同字符编号，排除空格
	static{
		 String path = "D:/NLP/icwb2-data/training/msr_training.utf8"; //文件名    
	        try{  
	            File file=new File(path);  
	            BufferedReader br= new BufferedReader(new FileReader(file));  
	           
	            String str = null;  
	            int charNo=1;//汉字编号从1开始
	            
	            while((str=br.readLine())!=null){
	            	//按空格分割词组，标点符号同样算入map中
	            	StringTokenizer st=new StringTokenizer(str);
	            	
	            	while(st.hasMoreTokens()){
	            		String partStr=st.nextToken();
	            		partStr=partStr.trim();
	            		for(int i=0;i<partStr.length();i++){
	            			if(!chineseNo.containsKey(partStr.charAt(i))){
	            				chineseNo.put(partStr.charAt(i),charNo++);
	            			}
	            		}
	            	}
	            }  
	            char c=' ';
	            chineseNo.remove(c);
	            chineseNo.remove('\t');
	            System.out.println("文本中不同字符的数量："+charNo);
	            br.close();
	        }catch(Exception e){  
	            e.printStackTrace();  
	        } 
	        
	}

	//获得一个句子的字符编号序列
	public static Integer [] getValueByKey(String sentences){
		Integer [] mid=new Integer[sentences.length()];
		//根据训练的markov模型，标点符号当做S型汉字处理
		StringTokenizer st=new StringTokenizer(sentences);//," ,.，。[]【】?？!！：；;:“”（）()_——-"
		int l=0;
		int length=0;
		while(st.hasMoreTokens()){
			String words=st.nextToken();
			if(words!=null){
				words=words.trim();
				length+=words.length();
				for(int i=0;i<words.length();i++){
					mid[l]=chineseNo.get(words.charAt(i));
					l++;
				}
			}
		
    	}
		Integer[] sentence=new Integer[length];
		for(int i=0;i<length;i++){
			sentence[i]=mid[i];
		}
		System.out.println("句子为："+sentences);
		System.out.println("句子对应编号序列:"+Arrays.toString(sentence)+"其长度为："+length);
		return sentence;
	}
	
	//根据字符编号，获取字符
	public  static Character getKeyOfValue(Integer value){
		Character no=0;
		for(Entry<Character, Integer> m :chineseNo.entrySet()){
			if(value==m.getValue()){
				no= m.getKey();
			}
		}
		return no;
	}
	
	public static Map<Character, Integer> getchineseNo() {
		return chineseNo;
	}
	
	public static void main(String[] args) {
		//new GetChineseNum().getValueByKey("，我是中国人");
	}
}
