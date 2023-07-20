package org.shirakawatyu.aadb.repository;

import org.shirakawatyu.aadb.pojo.Anime;
import org.shirakawatyu.aadb.pojo.AnimeSeason;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Repository
public interface AnimeRepo extends ReactiveMongoRepository<Anime, String> {
    @Query("{'synonyms': { $in: [/?0/i] }}")
    Flux<Anime> findByTitle(String title);
    @Query("{ 'tags': {$in: ?0} }")
    Flux<Anime> findByTags(List<String> tags);
    Flux<Anime> findByType(String type);
    @Query("{ 'animeSeason.year': ?0}")
    Flux<Anime> findByYear(Integer year);
    @Query("{ 'animeSeason.season': ?0}")
    Flux<Anime> findBySeason(String season);
    Flux<Anime> findByStatus(String status);
}
