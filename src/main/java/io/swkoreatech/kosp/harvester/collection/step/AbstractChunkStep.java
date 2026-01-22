package io.swkoreatech.kosp.harvester.collection.step;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractChunkStep<I, O> implements StepProvider {

    protected final JobRepository jobRepository;
    protected final PlatformTransactionManager transactionManager;

    @Override
    public Step getStep() {
        return new StepBuilder(getStepName(), jobRepository)
            .<I, O>chunk(getChunkSize(), transactionManager)
            .reader(getReader())
            .processor(getProcessor())
            .writer(getWriter())
            .faultTolerant()
            .retryLimit(getRetryLimit())
            .retry(RuntimeException.class)
            .skipLimit(getSkipLimit())
            .skip(Exception.class)
            .build();
    }

    protected abstract int getChunkSize();

    protected abstract ItemReader<I> getReader();

    protected abstract ItemProcessor<I, O> getProcessor();

    protected abstract ItemWriter<O> getWriter();

    protected int getRetryLimit() {
        return 3;
    }

    protected int getSkipLimit() {
        return 10;
    }
}
