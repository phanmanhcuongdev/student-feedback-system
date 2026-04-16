import type { ReactNode } from "react";
import SectionCard from "./SectionCard";

type FormSectionProps = {
    title: string;
    description?: string;
    children: ReactNode;
};

export default function FormSection({ title, description, children }: FormSectionProps) {
    return (
        <SectionCard title={title} description={description}>
            <div className="grid gap-5">
                {children}
            </div>
        </SectionCard>
    );
}
