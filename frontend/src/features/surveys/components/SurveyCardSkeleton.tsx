import SurveyCardSkeletonItem from './SurveyCardSkeletonItem';

export default function SurveyCardSkeleton() {
    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">

            {[1, 2, 3].map((_, i) => (
                <SurveyCardSkeletonItem key={i} />
            ))}

        </div>
    );
}