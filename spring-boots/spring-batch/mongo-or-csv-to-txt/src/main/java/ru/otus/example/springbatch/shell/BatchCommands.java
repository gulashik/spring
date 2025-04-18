package ru.otus.example.springbatch.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.example.springbatch.config.AppProps;

import java.util.Properties;

import static ru.otus.example.springbatch.config.JobConfig.IMPORT_USER_JOB_NAME;
import static ru.otus.example.springbatch.config.StepConfig.INPUT_FILE_NAME;
import static ru.otus.example.springbatch.config.StepConfig.OUTPUT_FILE_NAME;

@RequiredArgsConstructor
@ShellComponent
public class BatchCommands {

    private final AppProps appProps;
    private final Job importUserJob;

    // todo для ручного запуска и получения информации о Job-ах
    private final JobLauncher jobLauncher;
    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;

    //http://localhost:8080/h2-console/


    @ShellMethod(value = "startMigrationJobWithJobLauncher", key = "sm-jl")
    public void startMigrationJobWithJobLauncher() throws Exception {
        // todo JobLauncher
        JobExecution execution = jobLauncher.run(
            importUserJob,
            new JobParametersBuilder()
                .addString(INPUT_FILE_NAME, appProps.getInputFile())
                .addString(OUTPUT_FILE_NAME, appProps.getOutputFile())
                .toJobParameters()
        );

        System.out.println(execution);
    }

    @ShellMethod(value = "startMigrationJobWithJobOperator", key = "sm-jo")
    public void startMigrationJobWithJobOperator() throws Exception {

        Properties properties = new Properties();
        properties.put(INPUT_FILE_NAME, appProps.getInputFile());
        properties.put(OUTPUT_FILE_NAME, appProps.getOutputFile());

        // todo JobOperator
        Long executionId = jobOperator.start(IMPORT_USER_JOB_NAME, properties);
        System.out.println(jobOperator.getSummary(executionId));
    }

    @ShellMethod(value = "showInfo", key = "i")
    public void showInfo() {
        // todo JobExplorer
        System.out.println(jobExplorer.getJobNames());
        System.out.println(jobExplorer.getLastJobInstance(IMPORT_USER_JOB_NAME));
    }
}
