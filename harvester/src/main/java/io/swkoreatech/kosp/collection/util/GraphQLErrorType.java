package io.swkoreatech.kosp.collection.util;

/**
 * Classifies GraphQL response errors into actionable categories.
 * 
 * PARTIAL: Data is present despite errors (e.g., SERVICE_UNAVAILABLE on specific fields).
 *          Safe to continue processing with available data.
 * 
 * RETRYABLE: Total error (data absent) but worth retrying (rate limit, timeout, network).
 *            Same query may succeed on retry.
 * 
 * NON_RETRYABLE: Total error (data absent) that won't be fixed by retry.
 *                Example: "Something went wrong" from GitHub (10-second timeout on huge repos).
 */
public enum GraphQLErrorType {
    PARTIAL,
    RETRYABLE,
    NON_RETRYABLE
}
