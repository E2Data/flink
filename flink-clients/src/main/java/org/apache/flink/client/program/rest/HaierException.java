package org.apache.flink.client.program.rest;

public class HaierException extends RuntimeException {

    private final Exception e;

    private final java.nio.file.Path jobGraphFile;

    public HaierException(Exception e, java.nio.file.Path jobGraphFile) {
        super(e.getMessage());
        this.e = e;
        this.jobGraphFile = jobGraphFile;
    }

    public java.nio.file.Path getJobGraphFile() {
        return this.jobGraphFile;
    }

    public String getMessage() {
        return this.e.getMessage();
    }

}
