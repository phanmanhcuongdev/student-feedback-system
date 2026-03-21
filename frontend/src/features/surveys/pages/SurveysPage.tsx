import { useEffect, useMemo, useState } from "react";
import SurveyHero from "../components/SurveyHero";
import SurveyFilterTabs from "../components/SurveyFilterTabs";
import MainHeader from "../../../components/layout/MainHeader";
import SurveyGrid from "../components/SurveyGrid";
import SurveyCardSkeleton from "../components/SurveyCardSkeleton";
import SurveyEmptyState from "../components/SurveyEmptyState";
import { getAllSurveys } from "../../../api/surveyApi";
import type { Survey } from "../../../types/survey";
import Footer from "../../../components/layout/MainFooter";

type SurveyFilter = "ALL" | "OPEN" | "CLOSED";

export default function SurveysPage() {
    const [surveys, setSurveys] = useState<Survey[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [filter, setFilter] = useState<SurveyFilter>("ALL");

    useEffect(() => {
        const fetchSurveys = async () => {
            try {
                setLoading(true);
                setError("");

                const data = await getAllSurveys();
                setSurveys(data);
            } catch (err) {
                console.error(err);
                setError("Không thể tải danh sách khảo sát.");
            } finally {
                setLoading(false);
            }
        };

        fetchSurveys();
    }, []);

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
                        <SurveyFilterTabs value={filter} onChange={setFilter}/>
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

            <Footer/>
        </>
    );
}