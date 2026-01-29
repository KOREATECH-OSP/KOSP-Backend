package io.swkoreatech.kosp.job;

import java.util.List;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.collection.step.StepContextKeys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ContextValidationListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();
        ExecutionContext context = stepExecution.getJobExecution().getExecutionContext();
        
        validateConsumerStepPreconditions(stepName, context);
    }

    private void validateConsumerStepPreconditions(String stepName, ExecutionContext context) {
        if (isCommitMiningStep(stepName)) {
            validateCommitMiningStep(context);
            return;
        }
        
        if (isPullRequestMiningStep(stepName)) {
            validatePullRequestMiningStep(context);
            return;
        }
        
        if (isIssueMiningStep(stepName)) {
            validateIssueMiningStep(context);
        }
    }

    private boolean isCommitMiningStep(String stepName) {
        return "commitMiningStep".equals(stepName);
    }

    private boolean isPullRequestMiningStep(String stepName) {
        return "pullRequestMiningStep".equals(stepName);
    }

    private boolean isIssueMiningStep(String stepName) {
        return "issueMiningStep".equals(stepName);
    }

    private void validateCommitMiningStep(ExecutionContext context) {
        List<String> requiredKeys = List.of(
            StepContextKeys.GITHUB_TOKEN,
            StepContextKeys.GITHUB_NODE_ID,
            StepContextKeys.DISCOVERED_REPOS
        );
        validateRequiredKeys(requiredKeys, context, "commitMiningStep");
    }

    private void validatePullRequestMiningStep(ExecutionContext context) {
        List<String> requiredKeys = List.of(
            StepContextKeys.GITHUB_LOGIN,
            StepContextKeys.GITHUB_TOKEN
        );
        validateRequiredKeys(requiredKeys, context, "pullRequestMiningStep");
    }

    private void validateIssueMiningStep(ExecutionContext context) {
        List<String> requiredKeys = List.of(
            StepContextKeys.GITHUB_LOGIN,
            StepContextKeys.GITHUB_TOKEN
        );
        validateRequiredKeys(requiredKeys, context, "issueMiningStep");
    }

    private void validateRequiredKeys(
            List<String> requiredKeys, 
            ExecutionContext context, 
            String stepName) {
        for (String key : requiredKeys) {
            if (isMissingKey(key, context)) {
                String message = buildErrorMessage(key, stepName);
                log.error(message);
                throw new IllegalStateException(message);
            }
        }
    }

    private boolean isMissingKey(String key, ExecutionContext context) {
        return !context.containsKey(key);
    }

    private String buildErrorMessage(String key, String stepName) {
        return String.format(
            "Required context key '%s' is missing for step '%s'. " +
            "This indicates a problem in the preceding step or job configuration.",
            key,
            stepName
        );
    }
}
