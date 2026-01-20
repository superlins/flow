import React from 'react';

export type StatusType = 'success' | 'warning' | 'error' | 'info' | 'neutral';

interface StatusBadgeProps {
    status: string;
    type?: StatusType;
    className?: string;
}

const getStatusType = (status: string): StatusType => {
    const s = status.toLowerCase();
    if (['enabled', 'active', 'success', 'completed', 'online'].includes(s)) return 'success';
    if (['disabled', 'inactive', 'paused', 'pending'].includes(s)) return 'warning';
    if (['error', 'failed', 'offline', 'archived'].includes(s)) return 'error';
    if (['running', 'executing', 'processing'].includes(s)) return 'info';
    return 'neutral';
};

const getBadgeClass = (type: StatusType) => {
    switch (type) {
        case 'success': return 'badge-success text-success-content';
        case 'warning': return 'badge-warning text-warning-content';
        case 'error': return 'badge-error text-error-content';
        case 'info': return 'badge-info text-info-content';
        default: return 'badge-ghost';
    }
};

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, type, className = '' }) => {
    const resolvedType = type || getStatusType(status);

    return (
        <div className={`badge ${getBadgeClass(resolvedType)} gap-2 ${className}`}>
            {status}
        </div>
    );
};
