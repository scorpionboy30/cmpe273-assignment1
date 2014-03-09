package edu.sjsu.cmpe.library.api.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.yammer.dropwizard.jersey.params.IntParam;
import com.yammer.dropwizard.jersey.params.LongParam;
import com.yammer.metrics.annotation.Timed;

import edu.sjsu.cmpe.library.domain.Author;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.dto.AuthorDto;
import edu.sjsu.cmpe.library.dto.LinkDto;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;

@Path("/v1/books/{isbn}/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@JsonPropertyOrder({"isbn" , "title" , "publication-date" , "language" , "num-pages" , "status" , "reviews" , "authors" , "links"})
public class AuthorResource {

	private final BookRepositoryInterface bookRepository;

	public AuthorResource(BookRepositoryInterface bookRepository) {
		this.bookRepository = bookRepository;
	}

	@GET
	@Timed(name = "view-all-authors")
	public Response viewAllAuthors(@PathParam("isbn") LongParam isbn,
			Author author) {

		Book book = bookRepository.getBookByISBN(isbn.get());

		AuthorDto authorDto = new AuthorDto(book.getAuthorList());
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("authors", authorDto.getAuthorList());
		map.put("links", new ArrayList<String>());
		return Response.status(200).entity(map).build();

	}

	@GET
	@Path("/{authorId}")
	@Timed(name = "view-author-by-Id")
	public Response viewAuthorById(@PathParam("isbn") LongParam isbn,
			@PathParam("authorId") IntParam authorId) {

		Book book = bookRepository.getBookByISBN(isbn.get());

		AuthorDto authorResponse = null;
		List<Author> authorList = book.getAuthorList();
		String location = "/books/" + book.getIsbn() + "/authors/" + authorId;
		List<Author> tempList = new ArrayList<Author>();
		for (Author authorObject : authorList) {

			if (authorObject.getId() == authorId.get())
				tempList.add(authorObject);

		}
		authorResponse = new AuthorDto(tempList);
		authorResponse.addLink(new LinkDto("view-author", location, "GET"));

		HashMap<String, Object> map = new HashMap<String, Object>();

		map.put("author", authorResponse.getAuthorList().get(0));
		map.put("links", authorResponse.getLinks());

		return Response.status(200).entity(map).build();
	}

}
