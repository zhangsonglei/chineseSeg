package com.room406.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

public class SetTag {
	public static void main(String[] args) throws IOException {
		InputStreamReader inputStreamReader = new InputStreamReader(
				new FileInputStream("D:/NLP/icwb2-data/training/labeltest.utf8"), Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(inputStreamReader);

		String line = null;
		String txt = "";
		while ((line = br.readLine()) != null) {
			txt = txt + line;
		}

		StringTokenizer strt = new StringTokenizer(txt, "！：——（）— /();;？!?，。《》、“”’‘");

		System.out.println(txt);
		
		String targetTxt =" ";
		String partTxt=" ";
		while (strt.hasMoreTokens()) {
			String words = strt.nextToken();
			int strLen = words.length();
			if (strLen == 1) {
				targetTxt = targetTxt +" "+ words + "/S";
			} else {
				for (int l = 0; l < words.length(); l++) {
					if (l == 0) {
						partTxt = partTxt+ words.substring(l,l+1) + "/B";
					} else if (l == strLen - 1) {
						partTxt = partTxt + words.substring(l,l+1) + "/E";
					} else {
						partTxt = partTxt + words.substring(l,l+1) + "/M";
					}
				}
				
				targetTxt=targetTxt+partTxt;
				partTxt=" ";
				
			}

		}
		System.out.println(targetTxt);
		
		PrintStream ps=new PrintStream ("D:/NLP/icwb2-data/training/get_tag.txt");
		ps.println(targetTxt);
		
		ps.close();
		br.close();
		inputStreamReader.close();
	}
}
