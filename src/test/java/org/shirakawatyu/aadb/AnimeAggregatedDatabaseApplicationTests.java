package org.shirakawatyu.aadb;

import org.junit.jupiter.api.Test;
import org.shirakawatyu.aadb.repository.AnimeRepo;
import org.shirakawatyu.aadb.service.UpdateDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class AnimeAggregatedDatabaseApplicationTests {
    @Autowired
    ReactiveMongoTemplate mongoTemplate;
    @Autowired
    AnimeRepo animeRepo;
    @Autowired
    UpdateDataService service;
    @Test
    void contextLoads() throws Exception {
//        Anime anime = JSON.parseObject(jsonData, Anime.class);
        System.out.println(animeRepo
                .findByTags(List.of("band", "drama"))
                .collectList()
                .block());
//        service.updateData();
    }
}
