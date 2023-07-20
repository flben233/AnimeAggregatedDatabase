package org.shirakawatyu.aadb.controller;

import org.shirakawatyu.aadb.pojo.Anime;
import org.shirakawatyu.aadb.pojo.AnimeSeason;
import org.shirakawatyu.aadb.pojo.QueryBlock;
import org.shirakawatyu.aadb.repository.AnimeRepo;
import org.shirakawatyu.aadb.util.FilterUtil;
import org.shirakawatyu.aadb.util.TagUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class QueryDataController {
    private final AnimeRepo animeRepo;
    private final ArrayList<String> tagList;

    @Autowired
    public QueryDataController(AnimeRepo animeRepo) {
        this.animeRepo = animeRepo;
        tagList = new ArrayList<>(1070);
        try {
            BufferedReader br = new BufferedReader(new FileReader(ResourceUtils.getFile("classpath:tag.txt")));
            String s;
            while ((s = br.readLine()) != null) {
                tagList.add(s.split(" ")[0]);
            }
        } catch (IOException ignored){}
    }

    @PostMapping("/api/query")
    public Flux<Anime> queryAnime(@RequestBody QueryBlock queryBlock) {
        if (queryBlock.getTitle() != null) {
             return animeRepo.findByTitle(queryBlock.getTitle());
        } else {
            if (queryBlock.getCategory() != null) {
                queryBlock.getTags().addAll(TagUtil.getTags(queryBlock.getCategory()));
            }
            if (queryBlock.getTags().size() > 0) {
                return FilterUtil.tagFilter(animeRepo.findByTags(queryBlock.getTags()), queryBlock);
            } else if (queryBlock.getType() != null) {
                return FilterUtil.commonFilter(animeRepo.findByType(queryBlock.getType()), queryBlock);
            } else if (queryBlock.getStatus() != null) {
                return FilterUtil.commonFilter(animeRepo.findByStatus(queryBlock.getStatus()), queryBlock);
            } else if (queryBlock.getAnimeSeason() != null) {
                AnimeSeason season = queryBlock.getAnimeSeason();
                if (season.getYear() != null) {
                    return FilterUtil.commonFilter(animeRepo.findByYear(season.getYear()), queryBlock);
                } else if (season.getSeason() != null) {
                    return FilterUtil.commonFilter(animeRepo.findBySeason(season.getSeason()), queryBlock);
                }
            }
        }
        return null;
    }

    @GetMapping("/api/tag")
    public List<String> associationTags(@RequestParam("tag") String tag) {
        int limit = 10;
        ArrayList<String> result = new ArrayList<>(limit);
        for (int i = 0; i < tagList.size() && result.size() < limit; i++) {
            if (tagList.get(i).contains(tag)) {
                result.add(tagList.get(i));
            }
        }
        return result;
    }
}
