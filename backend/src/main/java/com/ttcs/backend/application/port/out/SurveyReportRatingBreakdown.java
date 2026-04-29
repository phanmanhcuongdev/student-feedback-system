package com.ttcs.backend.application.port.out;

public class SurveyReportRatingBreakdown {

    private final Integer rating;
    private final long count;

    public SurveyReportRatingBreakdown(Integer rating, long count) {
        this.rating = rating;
        this.count = count;
    }

    public Integer rating() { return rating; }
    public long count() { return count; }

    public Integer getRating() { return rating; }
    public long getCount() { return count; }
}
