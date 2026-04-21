import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { getApiErrorMessage } from "../../../api/apiError";
import { getSurveyDetail } from "../../../api/surveyApi";
import type { SurveyDetail } from "../../../types/surveyDetail";

export function useSurveyDetail(surveyId: number) {
    const { t } = useTranslation(["surveys"]);
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
                setError(getApiErrorMessage(error, t("surveys:surveys.errors.loadDetail")));
            } finally {
                setLoading(false);
            }
        }

        if (Number.isNaN(surveyId)) {
            setLoading(false);
            setError(t("surveys:surveys.errors.invalidId"));
            return;
        }

        fetchSurveyDetail();
    }, [surveyId, t]);

    return {
        survey,
        loading,
        error,
    };
}
