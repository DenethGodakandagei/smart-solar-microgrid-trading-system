import React from 'react';

export default function StatsCard({
  title,
  value,
  icon: Icon,
  subtitle,
  trend,
  color = 'blue',
  className = '',
  onClick,
}) {
  const colorThemes = {
    blue: {
      bg: 'bg-blue-50',
      text: 'text-blue-600',
      border: 'border-blue-100',
    },
    amber: {
      bg: 'bg-amber-50',
      text: 'text-amber-600',
      border: 'border-amber-100',
    },
    emerald: {
      bg: 'bg-emerald-50',
      text: 'text-emerald-600',
      border: 'border-emerald-100',
    },
    indigo: {
      bg: 'bg-indigo-50',
      text: 'text-indigo-600',
      border: 'border-indigo-100',
    },
    purple: {
      bg: 'bg-purple-50',
      text: 'text-purple-600',
      border: 'border-purple-100',
    },
    rose: {
      bg: 'bg-rose-50',
      text: 'text-rose-600',
      border: 'border-rose-100',
    },
  };

  const theme = colorThemes[color] || colorThemes.blue;

  return (
    <div
      onClick={onClick}
      className={`bg-white rounded-xl border border-slate-200/80 p-5 shadow-xs transition-all duration-200 ${
        onClick ? 'cursor-pointer hover:shadow-md hover:border-slate-300' : ''
      } ${className}`}
    >
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">{title}</p>
          <div className="flex items-baseline gap-2 mt-1">
            <h4 className="text-2xl font-bold text-slate-900">
              {value != null ? value : '—'}
            </h4>
            {trend && (
              <span className="text-xs font-semibold text-emerald-600 flex items-center">
                {trend}
              </span>
            )}
          </div>
          {subtitle && <p className="text-xs text-slate-500 mt-1">{subtitle}</p>}
        </div>

        {Icon && (
          <div className={`p-3 rounded-xl ${theme.bg} ${theme.text} ${theme.border} border shrink-0`}>
            <Icon className="w-6 h-6" />
          </div>
        )}
      </div>
    </div>
  );
}
