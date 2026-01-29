package io.swkoreatech.kosp.collection.step;

/**
 * ExecutionContext key constants for Spring Batch step data exchange.
 *
 * <p>This interface defines standardized keys used to store and retrieve data
 * from Spring Batch's ExecutionContext across step transitions. Using constants
 * prevents magic strings and ensures consistency throughout the batch job pipeline.
 *
 * <p>Usage pattern:
 * <pre>
 * // Writing to context
 * context.putString(StepContextKeys.GITHUB_LOGIN, login);
 * context.putString(StepContextKeys.GITHUB_TOKEN, token);
 *
 * // Reading from context
 * String login = context.getString(StepContextKeys.GITHUB_LOGIN);
 * </pre>
 *
 * @see org.springframework.batch.item.ExecutionContext
 */
public interface StepContextKeys {

    String GITHUB_LOGIN = "githubLogin";

    String GITHUB_TOKEN = "githubToken";

    String GITHUB_NODE_ID = "githubNodeId";

    String DISCOVERED_REPOS = "discoveredRepos";
}
