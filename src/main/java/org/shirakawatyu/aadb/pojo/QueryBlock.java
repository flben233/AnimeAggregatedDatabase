package org.shirakawatyu.aadb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryBlock {
    private String title;
    private String category;
    private List<String> tags;
    private String type;
    private AnimeSeason animeSeason;
    private String status;
}
