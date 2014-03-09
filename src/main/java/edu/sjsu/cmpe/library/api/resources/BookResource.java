package edu.sjsu.cmpe.library.api.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.yammer.dropwizard.jersey.params.LongParam;
import com.yammer.metrics.annotation.Timed;

import edu.sjsu.cmpe.library.domain.Author;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Review;
import edu.sjsu.cmpe.library.dto.AuthorDto;
import edu.sjsu.cmpe.library.dto.BookDto;
import edu.sjsu.cmpe.library.dto.LinkDto;
import edu.sjsu.cmpe.library.dto.ReviewDto;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;

@Path("/v1/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@JsonPropertyOrder({ "isbn", "title", "publication-date", "language",
		"num-pages", "status", "reviews", "authors", "links" })
public class BookResource {
	/** bookRepository instance */
	private final BookRepositoryInterface bookRepository;

	/**
	 * BookResource constructor
	 * 
	 * @param bookRepository
	 *            a BookRepository instance
	 */
	public BookResource(BookRepositoryInterface bookRepository) {
		this.bookRepository = bookRepository;
	}

	@GET
	@Path("/{isbn}")
	@Timed(name = "view-book")
	public Response viewBookByISBN(@PathParam("isbn") LongParam isbn) {

		BookDto bookDto = getBookByIsbn(isbn);
		bookDto.getBook();
		bookDto.getLinks();
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		HashMap<String, Object> tempMap = new HashMap<String, Object>();
		hashMap.put("book", tempMap);
		tempMap.put("isbn", bookDto.getBook().getIsbn());
		tempMap.put("title", bookDto.getBook().getTitle());
		tempMap.put("language", bookDto.getBook().getLanguage());
		tempMap.put("num-pages", bookDto.getBook().getNum_pages());
		tempMap.put("status", bookDto.getBook().getStatus());

		if (bookDto.getBook().getReviewList() == null) {
			tempMap.put("reviews", new ArrayList<String>());
		} else {

			ReviewDto reviewDto = new ReviewDto(bookDto.getBook().getReviewList());
			for (Review reviewObject : reviewDto.getReviewList()) {
				reviewDto
						.addLink(new LinkDto("view-review", "/books/"
								+ isbn.get() + "/reviews"
								+ reviewObject.getId(), "GET"));
			}
			tempMap.put("reviews", reviewDto.getLinks());
		}
		AuthorDto authorDto = new AuthorDto(bookDto.getBook().getAuthorList());
		for (Author authorObject : authorDto.getAuthorList()) {
			authorDto.addLink(new LinkDto("view-author", "/books/" + isbn.get()
					+ "/authors/" + authorObject.getId(), "GET"));

		}
		tempMap.put("authors", authorDto.getLinks());

		hashMap.put("links", bookDto.getLinks());

		return Response.status(200).entity(hashMap).build();
	}

	public BookDto getBookByIsbn(@PathParam("isbn") LongParam isbn) {
		Book book = bookRepository.getBookByISBN(isbn.get());
		BookDto bookResponse = new BookDto(book);

		bookResponse.addLink(new LinkDto("view-book", "/books/"
				+ book.getIsbn(), "GET"));
		bookResponse.addLink(new LinkDto("update-book", "/books/"
				+ book.getIsbn(), "PUT"));
		// add more links
		bookResponse.addLink(new LinkDto("delete-book", "/books/"
				+ book.getIsbn(), "DELETE"));
		bookResponse.addLink(new LinkDto("create-review", "/books/"
				+ book.getIsbn() + "/reviews", "POST"));

		if (book.getReviewList() != null && book.getReviewList().size() > 0) {
			bookResponse.addLink(new LinkDto("view-all-reviews", "/books/"
					+ book.getIsbn() + "/reviews", "GET"));
		}

		return bookResponse;
	}

	@POST
	@Timed(name = "create-book")
	public Response createBook(@Valid Book request) {
		// Store the new book in the BookRepository so that we can retrieve it.

		String status;
		if (request.getStatus() == null) {
			status = "available";
			request.setStatus(status);
		} else {
			status = request.getStatus();
		}

		final ArrayList<String> statusList = new ArrayList<String>();
		statusList.add("available");
		statusList.add("checked-out");
		statusList.add("in-queue");
		statusList.add("lost");
		if (!statusList.contains(status.toLowerCase())) {
			return Response
					.status(422)
					.type("text/plain")
					.entity("Invalid value for Status. Status can only be available/checked-out/in-queue/lost")
					.build();
		}

	if (request.getAuthorList() == null || request.getAuthorList().size() == 0) {
			return Response
					.status(422)
					.type("text/plain")
					.entity("Author Field Empty !!!! Author Name is a required field")
					.build();
		} else {
			List<Author> authorList = request.getAuthorList();

			for (Author authorObj : authorList) {
				if (authorObj.getName() == null || authorObj.getName().length() == 0 ) {
					return Response
							.status(422)
							.type("text/plain")
							.entity("Author Field Empty !!!! Author Name is a required field")
							.build();
				}

			}

		}

		Book savedBook = bookRepository.saveBook(request);

		String location = "/books/" + savedBook.getIsbn();
		BookDto bookResponse = new BookDto(savedBook);
		bookResponse.addLink(new LinkDto("view-book", location, "GET"));
		bookResponse.addLink(new LinkDto("update-book", location, "PUT"));
		// Add other links if needed

		bookResponse.addLink(new LinkDto("delete-book", location, "DELETE"));
		bookResponse.addLink(new LinkDto("create-review",
				location + "/reviews", "POST"));

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("links", bookResponse.getLinks());
		return Response.status(201).entity(map).build();
	}

	@DELETE
	@Path("/{isbn}")
	@Timed(name = "delete-book")
	public Response deleteBook(@PathParam("isbn") LongParam isbn) {

		bookRepository.deleteBook(isbn.get());

		// String location = "/books/" + isbn;
		BookDto bookResponse = new BookDto();
		bookResponse.addLink(new LinkDto("create-book", "/books", "POST"));
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("links", bookResponse.getLinks());
		return Response.status(200).entity(map).build();

	}

	@PUT
	@Path("/{isbn}")
	@Timed(name = "update-book")
	public Response updateBook(@PathParam("isbn") LongParam isbn,
			@QueryParam("status") String status) {

		final ArrayList<String> statusList = new ArrayList<String>();
		statusList.add("available");
		statusList.add("checked-out");
		statusList.add("in-queue");
		statusList.add("lost");
		if (!statusList.contains(status.toLowerCase())) {
			return Response
					.status(422)
					.type("text/plain")
					.entity("Invalid value for Status. Status can only be available/checked-out/in-queue/lost")
					.build();
		}

		Book updatedBook = bookRepository.updateBook(isbn.get(), status);

		String location = "/books/" + updatedBook.getIsbn();
		BookDto bookResponse = new BookDto(updatedBook);
		bookResponse.addLink(new LinkDto("view-book", location, "GET"));
		bookResponse.addLink(new LinkDto("update-book", location, "PUT"));
		bookResponse.addLink(new LinkDto("delete-book", location, "DELETE"));
		bookResponse.addLink(new LinkDto("create-review",
				location + "/reviews", "POST"));
		if (bookResponse.getBook().getReviewList() != null
				&& bookResponse.getBook().getReviewList().size() == 0) {
			bookResponse.addLink(new LinkDto("view-all-reviews", location
					+ "/reviews", "GET"));
		}
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("links", bookResponse.getLinks());
		return Response.status(200).entity(map).build();

	}
}
