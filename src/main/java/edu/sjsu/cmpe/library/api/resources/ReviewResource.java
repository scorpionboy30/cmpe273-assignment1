package edu.sjsu.cmpe.library.api.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.yammer.dropwizard.jersey.params.IntParam;
import com.yammer.dropwizard.jersey.params.LongParam;
import com.yammer.metrics.annotation.Timed;

import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Review;
import edu.sjsu.cmpe.library.dto.BookDto;
import edu.sjsu.cmpe.library.dto.LinkDto;
import edu.sjsu.cmpe.library.dto.ReviewDto;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;

@Path("/v1/books/{isbn}/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@JsonPropertyOrder({ "isbn", "title", "publication-date", "language",
		"num-pages", "status", "reviews", "authors", "links" })
public class ReviewResource {

	private final BookRepositoryInterface bookRepository;

	public ReviewResource(BookRepositoryInterface bookRepository) {
		this.bookRepository = bookRepository;
	}

	@POST
	@Timed(name = "create-review")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createReview(@PathParam("isbn") LongParam isbn,
			Review request) {

		// int rating = request.getRating();

		if (request.getComment() == null) {
			return Response
					.status(400)
					.type("text/plain")
					.entity(" Comment is a required field")
					.build();
		}

		if (request.getRating() == 0) {
			return Response
					.status(400)
					.type("text/plain")
					.entity(" Rating is a required field !!!!")
					.build();
		}
		final List<Integer> ratingList = Arrays.asList(1, 2, 3, 4, 5);

		if (!(ratingList.contains(request.getRating()))) {
			return Response
					.status(400)
					.type("text/plain")
					.entity("Invalid value for Rating. Ratings can be between 1 - 5 stars")
					.build();
		}

		Review reviewObject = bookRepository.createReview(isbn.get(), request);

		Book book = bookRepository.getBookByISBN(isbn.get());

		String location = "/books/" + book.getIsbn() + "/reviews/"
				+ reviewObject.getId();
		BookDto bookResponse = new BookDto(book);
		bookResponse.addLink(new LinkDto("view-review", location, "GET"));

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("links", bookResponse.getLinks());
		return Response.status(201).entity(map).build();

	}

	@GET
	@Timed(name = "view-book-review")
	public Response viewAllBookReview(@PathParam("isbn") LongParam isbn,
			Review request) {

		Book book = bookRepository.getBookByISBN(isbn.get());

		ReviewDto reviewResponse = new ReviewDto(book.getReviewList());

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("reviews", reviewResponse.getReviewList());
		map.put("links", new ArrayList<String>());
		return Response.status(200).entity(map).build();
	}

	@GET
	@Path("/{reviewId}")
	@Timed(name = "view-book-by-reviewId")
	public Response viewBookReview(@PathParam("isbn") LongParam isbn,
			@PathParam("reviewId") IntParam reviewId) {

		Book book = bookRepository.getBookByISBN(isbn.get());

		ReviewDto reviewResponse = null;
		List<Review> reviewList = book.getReviewList();

		List<Review> tempList = new ArrayList<Review>();
		for (Review reviewObj : reviewList) {

			if (reviewObj.getId() == reviewId.get())
				tempList.add(reviewObj);

		}
		reviewResponse = new ReviewDto(tempList);
		String location = "/books/" + book.getIsbn() + "/reviews/";

		HashMap<String, Object> map = new HashMap<String, Object>();

		Review review = reviewResponse.getReviewList().get(0);
		map.put("review", review);
		reviewResponse.addLink(new LinkDto("view-review", location
				+ reviewId.get(), "GET"));
		map.put("links", reviewResponse.getLinks());
		return Response.status(200).entity(map).build();
	}
}
