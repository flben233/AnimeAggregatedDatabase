package org.shirakawatyu.aadb.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.shirakawatyu.aadb.pojo.Anime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.ReactiveBulkOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeFunctions;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UpdateDataService {
    @Autowired
    ReactiveMongoTemplate template;
    @Value("${aadb.date-filepath}")
    String dateFilepath;
    String lastUpdate;

    @Scheduled(cron = "0 0 5 * * ? *")
    public void updateData() throws Exception {
        Logger.getLogger("UpdateDataService.updateData() => ").log(Level.INFO, "Starting to update database...");

        File file = new File(dateFilepath + File.separator + "lastUpdate");
        if (file.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            lastUpdate = br.readLine();
            br.close();
        } else {
            lastUpdate = "";
            file.createNewFile();
        }
        String s = WebClient
                .builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(clientCodecConfigurer ->
                        clientCodecConfigurer.defaultCodecs().maxInMemorySize(64 * 1024 * 1024)).build())
                .baseUrl("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/anime-offline-database-minified.json")
                .build()
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .block();
        JSONObject jsonObject = JSON.parseObject(s);
        String lu = jsonObject.getString("lastUpdate");
        if (!lu.equals(lastUpdate)) {
            List<Anime> data = jsonObject.getList("data", Anime.class);
            ReactiveBulkOperations reactiveBulkOperations = template.bulkOps(BulkOperations.BulkMode.UNORDERED, Anime.class);
            for (Anime anime : data) {
                Pair<Query, Update> upsertPair = upsertPair(anime);
                reactiveBulkOperations.upsert(upsertPair.getFirst(), upsertPair.getSecond());
            }
            reactiveBulkOperations.execute().block();
        }
        lastUpdate = lu;
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(lastUpdate);
        bw.close();

        Logger.getLogger("UpdateDataService.updateData() => ").log(Level.INFO, "Database updating finished.");
    }

    public Pair<Query, Update> upsertPair(Anime anime) {
        Query query = new Query();
        query.addCriteria(Criteria.where("title").is(anime.getTitle()).and("type").is(anime.getType()));
        Update update = new Update();
        Field[] fields = anime.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                update.set(field.getName(), field.get(anime));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return Pair.of(query, update);
    }
}
