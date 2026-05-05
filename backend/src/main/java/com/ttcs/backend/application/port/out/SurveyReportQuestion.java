package com.ttcs.backend.application.port.out;

import java.util.List;

public class SurveyReportQuestion {

    private final Integer id;
    private final String content;
    private final String type;
    private final long responseCount;
    private final Double averageRating;
    private final List<SurveyReportRatingBreakdown> ratingBreakdown;
    private final List<String> comments;

    public SurveyReportQuestion(
            Integer id,
            String content,
            String type,
            long responseCount,
            Double averageRating,
            List<SurveyReportRatingBreakdown> ratingBreakdown,
            List<String> comments
    ) {
        this.id = id;
        this.content = content;
        this.type = type;
        this.responseCount = responseCount;
        this.averageRating = averageRating;
        this.ratingBreakdown = ratingBreakdown == null ? List.of() : List.copyOf(ratingBreakdown);
        this.comments = comments == null ? List.of() : List.copyOf(comments);
    }

    public Integer id() { return id; }
    public String content() { return content; }
    public String type() { return type; }
    public long responseCount() { return responseCount; }
    public Double averageRating() { return averageRating; }
    public List<SurveyReportRatingBreakdown> ratingBreakdown() { return ratingBreakdown; }
    public List<String> comments() { return comments; }

    public Integer getId() { return id; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public long getResponseCount() { return responseCount; }
    public Double getAverageRating() { return averageRating; }
    public List<SurveyReportRatingBreakdown> getRatingBreakdown() { return ratingBreakdown; }
    public List<String> getComments() { return comments; }
}
