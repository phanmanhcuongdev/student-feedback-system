import type { ReactNode } from "react";
import PageToolbar from "../ui/PageToolbar";

type DataToolbarProps = {
    filters?: ReactNode;
    actions?: ReactNode;
};

export default function DataToolbar({ filters, actions }: DataToolbarProps) {
    return <PageToolbar leading={filters} trailing={actions} />;
}
