package org.shirakawatyu.aadb.util;

import org.shirakawatyu.aadb.pojo.Anime;
import org.shirakawatyu.aadb.pojo.AnimeSeason;
import org.shirakawatyu.aadb.pojo.QueryBlock;
import reactor.core.publisher.Flux;

import java.lang.reflect.Field;

public class FilterUtil {
    public static Flux<Anime> tagFilter(Flux<Anime> flux, QueryBlock query) {
        return flux.mapNotNull(anime -> {
            if (anime.getType().equals("SPECIAL")) {
                return null;
            }
            if (query.getType() != null && !query.getType().equalsIgnoreCase(anime.getType())) {
                return null;
            }
            if (query.getStatus() != null && !query.getStatus().equalsIgnoreCase(anime.getStatus())) {
                return null;
            }
            AnimeSeason animeSeason = query.getAnimeSeason();
            if (animeSeason != null) {
                String season = animeSeason.getSeason();
                Integer year = animeSeason.getYear();
                if (season != null && !season.equalsIgnoreCase(anime.getAnimeSeason().getSeason())) {
                    return null;
                }
                if (year != null && year > 0 && !year.equals(anime.getAnimeSeason().getYear())) {
                    return null;
                }
            }
            return anime;
        });
    }

    public static Flux<Anime> commonFilter(Flux<Anime> flux, QueryBlock query) {
        return flux.mapNotNull(anime -> {
            AnimeSeason animeSeason = anime.getAnimeSeason();
            Field[] fields = query.getClass().getDeclaredFields();
            if (anime.getType().equals("SPECIAL")) {
                return null;
            }
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    if (field.get(query) != null) {
                        if (field.getName().equals("tags")) {
                            continue;
                        }
                        if (field.getName().equals("animeSeason")) {
                            AnimeSeason aSeason = query.getAnimeSeason();
                            if (aSeason.getYear() != null && !aSeason.getYear().equals(animeSeason.getYear())
                            || aSeason.getSeason() != null && !aSeason.getSeason().equals(animeSeason.getSeason())) {
                                return null;
                            }
                        } else {
                            if (!anime.getClass().getDeclaredField(field.getName()).equals(field.get(query))) {
                                return null;
                            }
                        }
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
            return anime;
        });
    }
}
