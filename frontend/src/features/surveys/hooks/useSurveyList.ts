import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../../api/apiError";
import { getAllSurveys } from "../../../api/surveyApi";
import type { Survey } from "../../../types/survey";

export function useSurveyList() {
    const [surveys, setSurveys] = useState<Survey[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function fetchSurveys() {
            try {
                setLoading(true);
                setError("");
                setSurveys(await getAllSurveys());
            } catch (error) {
                setError(getApiErrorMessage(error, "Khong the tai danh sach khao sat."));
            } finally {
                setLoading(false);
            }
        }

        fetchSurveys();
    }, []);

    return {
        surveys,
        loading,
        error,
    };
}
