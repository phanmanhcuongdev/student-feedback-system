import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { getAllSurveys } from "../../../api/surveyApi";
import { getApiErrorMessage } from "../../../api/apiError";
import PaginationControls from "../../../components/data-view/PaginationControls";
import ErrorState from "../../../components/ui/ErrorState";
import SurveyCardSkeleton from "../components/SurveyCardSkeleton";
import SurveyEmptyState from "../components/SurveyEmptyState";
import SurveyFilterTabs, { type SurveyFilter } from "../components/SurveyFilterTabs";
import SurveyGrid from "../components/SurveyGrid";
import SurveyHero from "../components/SurveyHero";
import type { Survey } from "../../../types/survey";

export default function SurveysPage() {
    const { t } = useTranslation(["surveys"]);
    const [filter, setFilter] = useState<SurveyFilter>("ALL");
    const [surveys, setSurveys] = useState<Survey[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        async function loadSurveys() {
            try {
                setLoading(true);
                setError("");
                const response = await getAllSurveys({
                    status: filter === "ALL" ? undefined : filter,
                    page,
                    size: 12,
                    sortBy: "endDate",
                    sortDir: "asc",
                });
                if (response.items.length === 0 && response.totalPages > 0 && page >= response.totalPages) {
                    setPage(response.totalPages - 1);
                    return;
                }
                setSurveys(response.items);
                setTotalPages(response.totalPages);
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, t("surveys:surveys.errors.loadList")));
            } finally {
                setLoading(false);
            }
        }

        void loadSurveys();
    }, [filter, page, t]);

    useEffect(() => {
        setPage(0);
    }, [filter]);

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-xl px-6 py-10">
                <div className="mb-10 flex flex-col gap-6 md:flex-row md:items-end md:justify-between">
                    <SurveyHero />

                    <div className="flex items-center gap-3">
                        <SurveyFilterTabs value={filter} onChange={setFilter} />
                    </div>
                </div>

                {error ? <ErrorState description={error} /> : null}

                <div className="mt-8">
                    {loading ? (
                        <SurveyCardSkeleton />
                    ) : surveys.length === 0 ? (
                        <SurveyEmptyState />
                    ) : (
                        <div className="space-y-6">
                            <SurveyGrid surveys={surveys} />
                            <PaginationControls
                                page={page + 1}
                                pageCount={Math.max(totalPages, 1)}
                                onPageChange={(nextPage) => setPage(nextPage - 1)}
                            />
                        </div>
                    )}
                </div>
            </div>
        </main>
    );
}
