package org.shirakawatyu.aadb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document
public class Anime {
    private List<String> sources;
    private String title;
    private String type;
    private int episodes;
    private String status;
    private AnimeSeason animeSeason;
    private String picture;
    private String thumbnail;
    private List<String> synonyms;
    private List<String> relations;
    private List<String> tags;
}
