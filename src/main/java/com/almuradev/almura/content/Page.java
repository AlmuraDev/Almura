/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content;

import java.util.Comparator;
import java.util.Date;

public final class Page {

    private final String identifier;
    private Date created, lastModified;
    private String name, author, lastContributor, contents;
    private int index;

    public Page(String identifier, int index, String name, Date created, String author, Date lastModified, String lastContributor, String contents) {
        this.identifier = identifier;
        this.index = index;
        this.name = name;
        this.created = created;
        this.author = author;
        this.lastModified = lastModified;
        this.lastContributor = lastContributor;
        this.contents = contents;
    }

    public int getIndex() {
        return index;
    }

    public Page setIndex(int index) {
        this.index = index;
        return this;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Date getCreated() {
        return created;
    }

    public Page setCreated(Date created) {
        this.created = created;
        return this;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public Page setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public String getName() {
        return name;
    }

    public Page setTitle(String name) {
        this.name = name;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public Page setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getLastContributor() {
        return lastContributor;
    }

    public Page setLastContributor(String lastContributor) {
        this.lastContributor = lastContributor;
        return this;
    }

    public String getContents() {
        return contents;
    }

    public Page setContents(String contents) {
        this.contents = contents;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && identifier.equals(((Page) o).identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return "Page { " +
                "identifier= [" + identifier + "]" +
                ", index= [" + index + "]" +
                ", created= [" + created + "]" +
                ", lastModified= [" + lastModified + "]" +
                ", title= [" + name + "]" +
                ", author= [" + author + "]" +
                ", lastContributor= [" + lastContributor + "]" +
                ", contents= [" + contents + "]" +
                " }";
    }

    public static class PageIndexComparator implements Comparator<Page> {

        @Override
        public int compare(Page o1, Page o2) {
            return Integer.compare(o1.index, o2.index);
        }
    }
}
