package kr.ac.koreatech.sw.kosp.domain.github.collection.step;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractTaskletStep implements StepProvider {

    protected final JobRepository jobRepository;
    protected final PlatformTransactionManager transactionManager;

    @Override
    public Step getStep() {
        return new StepBuilder(getStepName(), jobRepository)
            .tasklet((contribution, chunkContext) -> {
                Long userId = chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobParameters()
                    .getLong("userId");
                execute(userId, chunkContext);
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    protected abstract void execute(Long userId, org.springframework.batch.core.scope.context.ChunkContext chunkContext);
}
