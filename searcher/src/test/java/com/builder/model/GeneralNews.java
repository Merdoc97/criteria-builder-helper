package com.builder.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDate;

/**
 *
 */
@MappedSuperclass
@Data
@SuppressWarnings("checkstyle:MagicNumber")
public class GeneralNews {

    @Id
    @Column(name = "art_link_pk", unique = true, length = 1000, updatable = false)
    private String linkReadMore;

    @Column(name = "art_name", length = 500)
    private String articleName;

    @Column(name = "art_date", updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate articleDate;

    @Column(name = "art_body", length = 500)
    private String articleBody;

}
