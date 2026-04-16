import type { ReactNode } from "react";

type ResponsiveDataListProps<T> = {
    items: T[];
    getKey: (item: T) => string | number;
    renderItem: (item: T) => ReactNode;
};

export default function ResponsiveDataList<T>({
    items,
    getKey,
    renderItem,
}: ResponsiveDataListProps<T>) {
    return (
        <div className="grid gap-4 lg:hidden">
            {items.map((item) => (
                <div key={getKey(item)}>{renderItem(item)}</div>
            ))}
        </div>
    );
}
