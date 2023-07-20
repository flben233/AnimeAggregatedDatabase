package org.shirakawatyu.aadb.util;

import com.alibaba.fastjson2.JSON;
import org.shirakawatyu.aadb.pojo.Anime;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.*;

public class TagUtil {

    public static List<String> getTags(String category) {
        try {
            File tagMapFile = ResourceUtils.getFile("classpath:tagMap.json");
            LinkedHashMap<String, Object> tagMap = JSON.parseObject(new FileReader(tagMapFile));
            return (List<String>) tagMap.get(category);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean subContain(String a, String b) {
        List<String> nonSenseList = List.of("of", "a", "the", "on", "all", "an", "based", "fiction",
                "protagonists", "primarily", "cast", "world", "arts", "age", "coming", "anime", "&", "and");
        a = a.replaceAll("-", " ");
        b = b.replaceAll("-", " ");
        String[] as = a.split(" ");
        String[] bs = b.split(" ");
        for (String a1 : as) {
            if (nonSenseList.contains(a1)) continue;
            for (String b1 : bs) {
                if (nonSenseList.contains(b1)) continue;
                if (a1.equals(b1)) return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("D:\\IdeaProjects\\AnimeAggregatedDatabase\\anime-offline-database.json"));
        String text = "";
        StringBuilder sb = new StringBuilder();
        while ((text = br.readLine()) != null) {
            sb.append(text);
        }
        br.close();
        List<Anime> data = JSON.parseObject(sb.toString()).getList("data", Anime.class);
        HashMap<String, Integer> tagSet = new HashMap<>();
        for (Anime anime : data) {
            for (String tag : anime.getTags()) {
                tagSet.compute(tag, (k, v) -> {
                    if (v == null) return 1;
                    else return v + 1;
                });
            }
        }
        tagSet.remove("slice of life");
        ArrayList<Map.Entry<String, Integer>> entryList = new ArrayList<>(tagSet.entrySet().stream().toList());
        entryList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        LinkedHashMap<String, List<String>> tagMap = new LinkedHashMap<>();
        for (int i = 0; i < entryList.size(); i++) {
            boolean flag = true;
            for (int j = 0; j < i; j++) {
                if (entryList.get(j) != null && subContain(entryList.get(i).getKey(), entryList.get(j).getKey())) {
                    flag = false;
                    try {
                        tagMap.get(entryList.get(j).getKey()).add(entryList.get(i).getKey());
                    } catch (Exception e) {
                        System.out.println(entryList.get(i).getKey() + " " + entryList.get(j).getKey());
                    }
                    entryList.set(i, null);
                    break;
                }
            }
            if (flag) {
                ArrayList<String> list = new ArrayList<>();
                list.add(entryList.get(i).getKey());
                tagMap.put(entryList.get(i).getKey(), list);
            }
        }
        System.out.println(tagMap.size());
        BufferedWriter bw = new BufferedWriter(new FileWriter("D:\\IdeaProjects\\AnimeAggregatedDatabase\\tagMap.json"));
        bw.append(JSON.toJSONString(tagMap));
        bw.close();
    }
}
