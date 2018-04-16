package com.learning.jpa;

import javax.persistence.*;

@Entity
public class Article {
    @Id
    @GeneratedValue
    private long id;

    private String content;

    public Article() {
    }

    public Article(String content) {
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }
}
