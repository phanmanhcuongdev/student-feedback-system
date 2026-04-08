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

            <main className="min-h-screen bg-[linear-gradient(180deg,#f4f8ff_0%,#eef3f8_44%,#f7fafc_100%)]">
                <div className="mx-auto max-w-screen-xl px-6 py-10">
                <div className="mb-12 flex flex-col gap-6 md:flex-row md:items-end md:justify-between">
                    <SurveyHero />

                    <div className="flex items-center gap-3">
                        <SurveyFilterTabs value={filter} onChange={setFilter} />
                    </div>
                </div>

                {error && (
                    <p className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-600">
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
                </div>
            </main>

            <Footer />
        </>
    );
}
