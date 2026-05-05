package com.ttcs.backend.application.port.out;

public class SummaryStatistics {

    private final long targetedCount;
    private final long openedCount;
    private final long submittedCount;
    private final double responseRate;
    private final long totalQuestions;
    private final long ratingQuestionCount;
    private final long textQuestionCount;
    private final long commentCount;
    private final Double averageRating;

    public SummaryStatistics(
            long targetedCount,
            long openedCount,
            long submittedCount,
            double responseRate,
            long totalQuestions,
            long ratingQuestionCount,
            long textQuestionCount,
            long commentCount,
            Double averageRating
    ) {
        this.targetedCount = targetedCount;
        this.openedCount = openedCount;
        this.submittedCount = submittedCount;
        this.responseRate = responseRate;
        this.totalQuestions = totalQuestions;
        this.ratingQuestionCount = ratingQuestionCount;
        this.textQuestionCount = textQuestionCount;
        this.commentCount = commentCount;
        this.averageRating = averageRating;
    }

    public long targetedCount() { return targetedCount; }
    public long openedCount() { return openedCount; }
    public long submittedCount() { return submittedCount; }
    public double responseRate() { return responseRate; }
    public long totalQuestions() { return totalQuestions; }
    public long ratingQuestionCount() { return ratingQuestionCount; }
    public long textQuestionCount() { return textQuestionCount; }
    public long commentCount() { return commentCount; }
    public Double averageRating() { return averageRating; }

    public long getTargetedCount() { return targetedCount; }
    public long getOpenedCount() { return openedCount; }
    public long getSubmittedCount() { return submittedCount; }
    public double getResponseRate() { return responseRate; }
    public long getTotalQuestions() { return totalQuestions; }
    public long getRatingQuestionCount() { return ratingQuestionCount; }
    public long getTextQuestionCount() { return textQuestionCount; }
    public long getCommentCount() { return commentCount; }
    public Double getAverageRating() { return averageRating; }
}
