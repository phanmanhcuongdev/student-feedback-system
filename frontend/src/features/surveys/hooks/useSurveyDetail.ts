import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../../api/apiError";
import { getSurveyDetail } from "../../../api/surveyApi";
import type { SurveyDetail } from "../../../types/surveyDetail";

export function useSurveyDetail(surveyId: number) {
    const [survey, setSurvey] = useState<SurveyDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function fetchSurveyDetail() {
            try {
                setLoading(true);
                setError(null);
                setSurvey(await getSurveyDetail(surveyId));
            } catch (error) {
                setError(getApiErrorMessage(error, "Failed to load survey detail"));
            } finally {
                setLoading(false);
            }
        }

        if (Number.isNaN(surveyId)) {
            setLoading(false);
            setError("Invalid survey id");
            return;
        }

        fetchSurveyDetail();
    }, [surveyId]);

    return {
        survey,
        loading,
        error,
    };
}
