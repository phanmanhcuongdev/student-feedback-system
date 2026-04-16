import type { ReactNode } from "react";

export type DataTableColumn<T> = {
    key: string;
    header: string;
    className?: string;
    render: (item: T) => ReactNode;
};

type DataTableProps<T> = {
    columns: DataTableColumn<T>[];
    items: T[];
    getRowKey: (item: T) => string | number;
};

export default function DataTable<T>({ columns, items, getRowKey }: DataTableProps<T>) {
    return (
        <div className="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-sm">
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-slate-200">
                    <thead className="bg-slate-50">
                        <tr>
                            {columns.map((column) => (
                                <th
                                    key={column.key}
                                    className={`px-4 py-3 text-left text-[11px] font-bold uppercase tracking-[0.16em] text-slate-500 lg:px-5 ${column.className ?? ""}`}
                                >
                                    {column.header}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {items.map((item) => (
                            <tr key={getRowKey(item)} className="align-top transition hover:bg-slate-50/60">
                                {columns.map((column) => (
                                    <td key={column.key} className={`px-4 py-3.5 text-sm text-slate-700 lg:px-5 ${column.className ?? ""}`}>
                                        {column.render(item)}
                                    </td>
                                ))}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
