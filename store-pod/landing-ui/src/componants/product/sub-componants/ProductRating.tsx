'use client'
import React from "react";
import StarRatings from "react-star-ratings";

export const ProductRating = ({ratingValue}) => {
    return <StarRatings
        rating={ratingValue}
        starRatedColor="#ffa900"
        starDimension="17px"
        starSpacing="1px"
        numberOfStars={5}
        name='view-rating'/>;
};
