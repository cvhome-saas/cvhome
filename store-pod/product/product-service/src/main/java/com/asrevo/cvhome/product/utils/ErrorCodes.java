package com.asrevo.cvhome.product.utils;

import com.asrevo.cvhome.commons.utils.ErrorCode;

public class ErrorCodes {
    public static final ErrorCode single_product_should_not_have_sub_product = new ErrorCode("0", "single product should not have sub product");
    public static final ErrorCode one_or_more_sub_product_not_from_this_store = new ErrorCode("0", "one or more sub product not from this store");
    public static final ErrorCode product_not_exist = new ErrorCode("0", "product not exist");
    public static final ErrorCode product_details_not_created_yet = new ErrorCode("0", "product details not created yet");
    public static final ErrorCode one_of_your_sub_products_not_have_product_details_yet = new ErrorCode("0", "one of your sub products not have product details yet");
    public static final ErrorCode parent_category_not_exist = new ErrorCode("0", "parent category not exist");
    public static final ErrorCode category_not_exist = new ErrorCode("0", "category not exist");
    public static final ErrorCode product_details_should_not_be_null = new ErrorCode("0", "product details shouldn't be null");
    public static final ErrorCode details_should_not_be_null = new ErrorCode("0", "details should not be null");
    public static final ErrorCode ltr_should_be_true_or_false = new ErrorCode("0", "ltr should be true or false");
    public static final ErrorCode name_should_be_not_empty = new ErrorCode("0", "name should be not empty");
    public static final ErrorCode short_description_should_be_not_empty = new ErrorCode("0", "short description should be not empty");
    public static final ErrorCode description_entries_value_should_be_not_empty = new ErrorCode("0", "description entries value should be not empty");
    public static final ErrorCode imageLinks_should_not_be_null = new ErrorCode("0", "imageLinks should not be null");
    public static final ErrorCode imageLinks_should_have_at_least_image = new ErrorCode("0", "imageLinks should have at least image");
}
