package edu.sjsu.cmpe.library.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.sjsu.cmpe.library.domain.Review;

@JsonPropertyOrder({"reviews", "links"})
public class ReviewDto extends LinksDto {
    private List<Review> reviewList;
   // private Review review;
		
    /**
     * @param book
     */
    
   /* public ReviewDto() {
	super();
	  }
    public ReviewDto(Review review){
    	super();
    	this.review = review;
    }*/

    public ReviewDto(List<Review> reviewList) {
		super();
		this.reviewList = reviewList;
	}
	public List<Review> getReviewList() {
		return reviewList;
	}
	public void setReviewList(List<Review> reviewList) {
		this.reviewList = reviewList;
	}
	/*public Review getReview() {
		return review;
	}
	public void setReview(Review review) {
		this.review = review;
	}*/

	
	
}