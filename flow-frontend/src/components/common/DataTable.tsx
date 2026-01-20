import { type ReactNode } from 'react';

export interface Column<T> {
    key: string;
    header: string;
    render?: (item: T) => ReactNode;
    className?: string;
}

interface DataTableProps<T> {
    data: T[];
    columns: Column<T>[];
    keyField: keyof T | ((item: T) => string);
    isLoading?: boolean;
    emptyMessage?: string;
    onRowClick?: (item: T) => void;
}

export function DataTable<T>({
    data,
    columns,
    keyField,
    isLoading = false,
    emptyMessage = 'No data available',
    onRowClick
}: DataTableProps<T>) {

    const getRowKey = (item: T): string => {
        if (typeof keyField === 'function') {
            return keyField(item);
        }
        return String(item[keyField]);
    };

    return (
        <div className="overflow-x-auto bg-base-100 rounded-box shadow-sm border border-base-200">
            <table className="table w-full">
                <thead>
                    <tr className="bg-base-200/50 text-base-content/70">
                        {columns.map((col) => (
                            <th key={col.key} className={col.className}>{col.header}</th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {isLoading ? (
                        <tr>
                            <td colSpan={columns.length} className="text-center py-8">
                                <span className="loading loading-spinner loading-lg text-primary"></span>
                            </td>
                        </tr>
                    ) : data.length === 0 ? (
                        <tr>
                            <td colSpan={columns.length} className="text-center py-8 text-base-content/50">
                                {emptyMessage}
                            </td>
                        </tr>
                    ) : (
                        data.map((item) => (
                            <tr
                                key={getRowKey(item)}
                                className={`hover:bg-base-200/30 transition-colors ${onRowClick ? 'cursor-pointer' : ''}`}
                                onClick={() => onRowClick && onRowClick(item)}
                            >
                                {columns.map((col) => (
                                    <td key={col.key} className={col.className}>
                                        {col.render ? col.render(item) : (item as any)[col.key]}
                                    </td>
                                ))}
                            </tr>
                        ))
                    )}
                </tbody>
            </table>
        </div>
    );
}
