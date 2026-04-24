import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { getApiErrorMessage } from "../../../api/apiError";
import { getAllSurveys } from "../../../api/surveyApi";
import type { Survey } from "../../../types/survey";

export function useSurveyList() {
    const { i18n, t } = useTranslation(["surveys"]);
    const [surveys, setSurveys] = useState<Survey[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function fetchSurveys() {
            try {
                setLoading(true);
                setError("");
                const response = await getAllSurveys({ page: 0, size: 12, sortBy: "endDate", sortDir: "asc" });
                setSurveys(response.items);
            } catch (error) {
                setError(getApiErrorMessage(error, t("surveys:surveys.errors.loadList")));
            } finally {
                setLoading(false);
            }
        }

        void fetchSurveys();
    }, [i18n.resolvedLanguage, t]);

    return {
        surveys,
        loading,
        error,
    };
}
