import { useMemo, useState } from "react";
import ErrorState from "../../../components/ui/ErrorState";
import SurveyCardSkeleton from "../components/SurveyCardSkeleton";
import SurveyEmptyState from "../components/SurveyEmptyState";
import SurveyFilterTabs, { type SurveyFilter } from "../components/SurveyFilterTabs";
import SurveyGrid from "../components/SurveyGrid";
import SurveyHero from "../components/SurveyHero";
import { useSurveyList } from "../hooks/useSurveyList";

export default function SurveysPage() {
    const [filter, setFilter] = useState<SurveyFilter>("ALL");
    const { surveys, loading, error } = useSurveyList();

    const filteredSurveys = useMemo(() => {
        if (filter === "ALL") return surveys;
        return surveys.filter((survey) => survey.status === filter);
    }, [surveys, filter]);

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
                    ) : filteredSurveys.length === 0 ? (
                        <SurveyEmptyState />
                    ) : (
                        <SurveyGrid surveys={filteredSurveys} />
                    )}
                </div>
            </div>
        </main>
    );
}
