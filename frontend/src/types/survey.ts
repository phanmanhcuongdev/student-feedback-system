export interface Survey {
    id: number;
    title: string;
    description: string;
    startDate: string;
    endDate: string;
    createdBy: number;
    status: "OPEN" | "CLOSED" | "NOT_OPEN";
}