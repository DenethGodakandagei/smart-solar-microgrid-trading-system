import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { ROLES } from '../../utils/constants';
import {
  HiOutlineHome,
  HiOutlineUsers,
  HiOutlineClipboardDocumentCheck,
  HiOutlineUserGroup,
  HiOutlineCube,
  HiOutlineCalendarDays,
  HiOutlineBolt,
  HiOutlineClock,
  HiOutlineSun,
} from 'react-icons/hi2';

export default function Sidebar({ isOpen, onClose }) {
  const { user } = useAuth();
  const role = user?.role;

  const backofficeItems = [
    { label: 'Dashboard', path: '/backoffice/dashboard', icon: HiOutlineHome },
    { label: 'User Accounts', path: '/backoffice/users', icon: HiOutlineUsers },
    { label: 'Pending Activations', path: '/backoffice/pending-activations', icon: HiOutlineClipboardDocumentCheck },
    { label: 'Prosumer Registry', path: '/backoffice/prosumers', icon: HiOutlineUserGroup },
    { label: 'Microgrid Nodes', path: '/backoffice/nodes', icon: HiOutlineCube },
    { label: 'Trading Oversight', path: '/backoffice/bookings', icon: HiOutlineCalendarDays },
  ];

  const operatorItems = [
    { label: 'Dashboard', path: '/operator/dashboard', icon: HiOutlineHome },
    { label: 'Energy Bookings', path: '/operator/bookings', icon: HiOutlineCalendarDays },
    { label: 'Battery Storage Slots', path: '/operator/slots', icon: HiOutlineBolt },
    { label: 'Trading History', path: '/operator/history', icon: HiOutlineClock },
  ];

  const items = role === ROLES.BACKOFFICE ? backofficeItems : operatorItems;

  return (
    <>
      {/* Mobile backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/50 backdrop-blur-xs lg:hidden"
          onClick={onClose}
        />
      )}

      <aside
        className={`fixed top-0 bottom-0 left-0 z-40 w-64 bg-slate-900 text-slate-300 flex flex-col transition-transform duration-200 ease-in-out border-r border-slate-800 ${
          isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        {/* Brand / Logo */}
        <div className="h-16 px-6 flex items-center gap-3 border-b border-slate-800 shrink-0">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-amber-500 to-yellow-400 flex items-center justify-center text-slate-950 shadow-md shadow-amber-500/20 shrink-0">
            <HiOutlineSun className="w-5 h-5 font-bold" />
          </div>
          <div className="overflow-hidden">
            <h1 className="text-sm font-bold text-white tracking-tight truncate">
              Smart Solar Microgrid
            </h1>
            <p className="text-[11px] font-medium text-amber-400 uppercase tracking-wider">
              {role === ROLES.BACKOFFICE ? 'Backoffice' : 'Grid Operator'}
            </p>
          </div>
        </div>

        {/* Navigation Items */}
        <div className="flex-1 px-3 py-4 overflow-y-auto space-y-1">
          <div className="px-3 pb-2 text-[10px] font-bold uppercase tracking-wider text-slate-500">
            Navigation
          </div>
          {items.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150 ${
                    isActive
                      ? 'bg-blue-600/20 text-blue-400 font-semibold border-l-2 border-blue-500'
                      : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/60'
                  }`
                }
              >
                <Icon className="w-5 h-5 shrink-0" />
                <span className="truncate">{item.label}</span>
              </NavLink>
            );
          })}
        </div>

        {/* Architecture Note at bottom */}
        <div className="p-4 border-t border-slate-800/80 bg-slate-950/40 text-center">
          <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-md bg-slate-800/80 text-[11px] font-medium text-slate-400 border border-slate-700/50">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
            Connected to C# Web API
          </div>
        </div>
      </aside>
    </>
  );
}
