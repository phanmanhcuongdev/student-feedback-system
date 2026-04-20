package com.ttcs.backend.adapter.out.persistence.reporting;

public final class ReportingSqlFragments {

    private ReportingSqlFragments() {
    }

    public static String runtimeStatus(String surveyAlias) {
        return """
                CASE
                    WHEN %1$s.lifecycle_state = 'DRAFT' THEN 'NOT_OPEN'
                    WHEN %1$s.lifecycle_state IN ('CLOSED', 'ARCHIVED') THEN 'CLOSED'
                    WHEN %1$s.start_date IS NOT NULL AND %1$s.start_date > GETDATE() THEN 'NOT_OPEN'
                    WHEN %1$s.end_date IS NOT NULL AND %1$s.end_date < GETDATE() THEN 'CLOSED'
                    ELSE 'OPEN'
                END
                """.formatted(surveyAlias);
    }

    public static String recipientResponseRate(String recipientStatsAlias) {
        return """
                CASE
                    WHEN COALESCE(%1$s.targeted_count, 0) = 0 THEN 0
                    ELSE (COALESCE(%1$s.submitted_count, 0) * 100.0) / %1$s.targeted_count
                END
                """.formatted(recipientStatsAlias);
    }

    public static String recipientStatsJoin(String surveyAlias, String recipientStatsAlias) {
        return """
                LEFT JOIN (
                    SELECT
                        sr.survey_id,
                        COUNT(*) AS targeted_count,
                        SUM(CASE WHEN sr.opened_at IS NOT NULL THEN 1 ELSE 0 END) AS opened_count,
                        SUM(CASE WHEN sr.submitted_at IS NOT NULL THEN 1 ELSE 0 END) AS submitted_count
                    FROM Survey_Recipient sr
                    GROUP BY sr.survey_id
                ) %2$s ON %2$s.survey_id = %1$s.survey_id
                """.formatted(surveyAlias, recipientStatsAlias);
    }
}
