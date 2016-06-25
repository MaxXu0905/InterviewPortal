package com.ailk.sets.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Dictionary {
	public static void main(String[] args) {

		try {
			Map<String, Integer> map = new HashMap<String, Integer>();
			String[] files = { "DICTIONARY.txt", "bookAllWords.txt" };
			for (int j = 0; j < files.length; j++) {
				String str = "";
				BufferedReader br = new BufferedReader(new FileReader(
						"D:\\项目\\SETS\\SETS文档\\03.Design Doc\\01.Data Model\\PASSCODE_AVAILABLE\\"+files[j]));
				String[] arrs = null;
				while ((str = br.readLine()) != null) {
					arrs = str.split("\\s");
					for (int i = 0; i < arrs.length; i++) {

						if (null == arrs[i] || "".equals(arrs[i])
								|| arrs[i].length() == 0
								|| arrs[i].indexOf("-") >= 0
								|| arrs[i].indexOf("'") >= 0
								|| arrs[i].length() < 3 || arrs[i].length() > 5)
							continue;

						String key = arrs[i].toLowerCase();
						key = key.replaceAll(
								"[.,\"\\?!:;')(*=\\/><-_+~`@#$%^&{}]", "");

						if ("\"".indexOf(key) >= 0) {
							key = key.replace("\"", "");
						}

						if (!map.containsKey(key)) {
							System.out.println(key);
							map.put(key, 1);
						} else {
							int num = map.get(key);
							map.remove(key);
							map.put(key, num + 1);
						}
					}
				}
			}

			System.out.println(map.size());
			ArrayList<String> list = new ArrayList<String>();
			Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Integer> entry = it.next();
				list.add("INSERT INTO PASSCODE_AVAILABLE(passcode,status) VALUES(\""
						+ entry.getKey() + "\"," + "0);");
			}
			Collections.shuffle(list);
			
			FileOutputStream out = new FileOutputStream(new File(
					"D:\\DICTIONARY1.txt"));
			
			for(int k=0;k<list.size();k++)
			{
				out.write(( list.get(k) + "\r\n").getBytes());
			}
			
			out.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
