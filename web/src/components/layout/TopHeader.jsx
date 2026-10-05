import React from 'react';
import { useAuth } from '../../hooks/useAuth';
import {
  HiBars3,
  HiOutlineArrowLeftOnRectangle,
  HiOutlineUserCircle,
} from 'react-icons/hi2';

export default function TopHeader({ onToggleSidebar }) {
  const { user, logout } = useAuth();

  return (
    <header className="sticky top-0 z-30 h-16 bg-white border-b border-slate-200/80 px-4 sm:px-6 flex items-center justify-between shadow-2xs">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onToggleSidebar}
          className="lg:hidden p-2 rounded-lg text-slate-500 hover:text-slate-700 hover:bg-slate-100 transition-colors"
          aria-label="Open sidebar"
        >
          <HiBars3 className="w-6 h-6" />
        </button>

        <div className="hidden sm:block">
          <span className="text-xs font-semibold px-2.5 py-1 rounded-md bg-slate-100 text-slate-600 border border-slate-200">
            Client Layer • RESTful Axios Integration
          </span>
        </div>
      </div>

      {/* User Info & Actions */}
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-3 pl-3 border-l border-slate-200">
          <div className="w-8 h-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-xs">
            {user?.name ? user.name.charAt(0).toUpperCase() : <HiOutlineUserCircle className="w-5 h-5" />}
          </div>
          <div className="hidden md:block text-left">
            <div className="text-xs font-semibold text-slate-800 leading-tight">
              {user?.name || user?.username || 'Authenticated User'}
            </div>
            <div className="text-[11px] font-medium text-slate-500">
              Role: <span className="text-blue-600 font-semibold">{user?.role || 'Guest'}</span>
            </div>
          </div>
        </div>

        <button
          type="button"
          onClick={logout}
          title="Sign out of system"
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-slate-600 hover:text-red-700 hover:bg-red-50 border border-transparent hover:border-red-200 transition-all duration-150"
        >
          <HiOutlineArrowLeftOnRectangle className="w-4 h-4" />
          <span className="hidden sm:inline">Logout</span>
        </button>
      </div>
    </header>
  );
}
