import SurveyCard from "./SurveyCard";
import type { Survey } from "../../../types/survey";

type SurveyGridProps = {
    surveys: Survey[];
};

export default function SurveyGrid({ surveys} : SurveyGridProps) {
    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">

            {surveys.map((survey) => (
                <SurveyCard key={survey.id} survey={survey} />
            ))}

        </div>
    );
}