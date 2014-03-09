package edu.sjsu.cmpe.library.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.sjsu.cmpe.library.domain.Book;

@JsonPropertyOrder({"isbn" , "title" , "publication-date" , "language" , "num-pages" , "status" , "reviews" , "authors" , "links"})
public class BookDto extends LinksDto {
    private Book book;

    /**
     * @param book
     */
    
    public BookDto() {
	super();
	  }

    public BookDto(Book book) {
		super();
		this.book = book;
	}

	/**
     * @return the book
     */
    public Book getBook() {
	return book;
    }

    /**
     * @param book
     *            the book to set
     */
    public void setBook(Book book) {
	this.book = book;
    }
}