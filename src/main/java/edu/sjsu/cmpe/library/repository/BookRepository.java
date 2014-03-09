package edu.sjsu.cmpe.library.repository;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.Valid;

import edu.sjsu.cmpe.library.domain.Author;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Review;

public class BookRepository implements BookRepositoryInterface {
	/** In-memory map to store books. (Key, Value) -> (ISBN, Book) */
	private final ConcurrentHashMap<Long, Book> bookInMemoryMap;

	/** Never access this key directly; instead use generateISBNKey() */
	private long isbnKey;
	private int authorId;
	private int reviewId;

	public BookRepository(ConcurrentHashMap<Long, Book> bookMap) {
		/* checkNotNull(bookMap, "bookMap must not be null for BookRepository"); */
		bookInMemoryMap = bookMap;
		isbnKey = 0;
		authorId = 0;
		reviewId = 0;
	}

	/**
	 * This should be called if and only if you are adding new books to the
	 * repository.
	 * 
	 * @return a new incremental ISBN number
	 */
	private final Long generateISBNKey() {
		// increment existing isbnKey and return the new value
		return Long.valueOf(++isbnKey);
	}

	private final int generateAuthorId() {
		// increment existing isbnKey and return the new value
		return ++authorId;
	}

	private final int generateReviewId() {

		return ++reviewId;
	}

	/**
	 * This will auto-generate unique ISBN for new books.
	 */
	@Override
	public Book saveBook(Book newBook) {
		/* checkNotNull(newBook, "newBook instance must not be null"); */
		// Generate new ISBN
		Long isbn = generateISBNKey();
		newBook.setIsbn(isbn);
		// TODO: create and associate other fields such as author
		List<Author> authorList = newBook.getAuthorList();
		if (authorList != null && !authorList.isEmpty()) {
			for (Author authorObject : authorList) {
				saveAuthor(authorObject);
			}
		}

		// Finally, save the new book into the map
		bookInMemoryMap.putIfAbsent(isbn, newBook);

		return newBook;
	}

	private void saveAuthor(@Valid Author author) {
		author.setId(generateAuthorId());
	}

	@Override
	public void deleteBook(Long isbn) {

		bookInMemoryMap.remove(isbn);

	}

	@Override
	public Book updateBook(Long isbn, String status) {

		Book bookToUpdate = getBookByISBN(isbn);

		bookToUpdate.setStatus(status);
		bookInMemoryMap.put(isbn, bookToUpdate);
		return bookToUpdate;

	}

	/**
	 * @see edu.sjsu.cmpe.library.repository.BookRepositoryInterface#getBookByISBN(java.lang.Long)
	 */
	@Override
	public Book getBookByISBN(Long isbn) {
		checkArgument(isbn > 0,
				"ISBN was %s but expected greater than zero value", isbn);
		return bookInMemoryMap.get(isbn);
	}

	@Override
	public Review createReview(Long isbn, Review review) {

		review.setId(generateReviewId());
		/*
		 * List<Review> reviewList = new ArrayList<Review>();
		 * 
		 * reviewList.add(review);
		 */

		Book bookReviewToUpdate = bookInMemoryMap.get(isbn);
		if (bookReviewToUpdate.getReviewList() == null) {
			List<Review> reviewList = new ArrayList<Review>();
			reviewList.add(review);
			bookReviewToUpdate.setReviewList(reviewList);
		} else {
			List<Review> reviewList = bookReviewToUpdate.getReviewList();
			reviewList.add(review);
			bookReviewToUpdate.setReviewList(reviewList);
		}
		return review;

	}

}
