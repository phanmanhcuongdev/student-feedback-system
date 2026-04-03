import { useMemo, useState } from "react";
import Footer from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
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
        <>
            <MainHeader />

            <main className="max-w-screen-xl mx-auto px-6 py-10">
                <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
                    <SurveyHero />

                    <div className="flex items-center gap-3">
                        <SurveyFilterTabs value={filter} onChange={setFilter} />
                    </div>
                </div>

                {error && (
                    <p className="mb-6 text-sm font-medium text-red-500">
                        {error}
                    </p>
                )}

                <div className="mt-10">
                    {loading ? (
                        <SurveyCardSkeleton />
                    ) : filteredSurveys.length === 0 ? (
                        <SurveyEmptyState />
                    ) : (
                        <SurveyGrid surveys={filteredSurveys} />
                    )}
                </div>
            </main>

            <Footer />
        </>
    );
}
