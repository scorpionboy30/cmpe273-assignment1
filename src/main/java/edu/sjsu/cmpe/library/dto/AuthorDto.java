package edu.sjsu.cmpe.library.dto;

import java.util.List;

import edu.sjsu.cmpe.library.domain.Author;

public class AuthorDto extends LinksDto {

	private List<Author> authorList;
	/*private Author author;

	public AuthorDto(Author author) {
		super();
		this.author = author;
	}*/

	public AuthorDto(List<Author> authorList) {
		super();
		this.authorList = authorList;
	}
/*
	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}
*/
	public List<Author> getAuthorList() {
		return authorList;
	}

	public void setAuthorList(List<Author> authorList) {
		this.authorList = authorList;
	}

}
