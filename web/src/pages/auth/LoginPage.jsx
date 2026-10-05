import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { ROLES } from '../../utils/constants';
import Input from '../../components/common/Input';
import Button from '../../components/common/Button';
import ErrorAlert from '../../components/common/ErrorAlert';
import {
  HiOutlineSun,
  HiOutlineUser,
  HiOutlineLockClosed,
  HiOutlineShieldCheck,
  HiOutlineBolt,
} from 'react-icons/hi2';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    if (!username.trim() || !password) {
      setErrorMessage('Please provide both username and password.');
      return;
    }

    setIsLoading(true);
    try {
      // Login API call — returns authenticated user object with role determined by server
      const user = await login({ username: username.trim(), password });

      // Navigate based on server-provided role
      if (user?.role === ROLES.BACKOFFICE) {
        navigate('/backoffice/dashboard', { replace: true });
      } else if (user?.role === ROLES.GRID_OPERATOR) {
        navigate('/operator/dashboard', { replace: true });
      } else {
        // Fallback or unexpected role
        setErrorMessage('Unrecognized role received from authorization server.');
      }
    } catch (err) {
      const serverMessage =
        err?.response?.data?.message ||
        err?.response?.data?.title ||
        err?.message ||
        'Authentication failed. Please verify your credentials.';
      setErrorMessage(serverMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 flex flex-col justify-center py-12 px-4 sm:px-6 lg:px-8 relative overflow-hidden">
      {/* Decorative background glow */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-amber-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-10 right-10 w-80 h-80 bg-blue-500/10 rounded-full blur-3xl pointer-events-none" />

      <div className="sm:mx-auto sm:w-full sm:max-w-md relative z-10 text-center">
        {/* Brand Icon */}
        <div className="mx-auto w-14 h-14 rounded-2xl bg-gradient-to-tr from-amber-500 to-yellow-400 flex items-center justify-center text-slate-950 shadow-xl shadow-amber-500/25 mb-4">
          <HiOutlineSun className="w-8 h-8" />
        </div>
        <h2 className="text-2xl font-bold tracking-tight text-white sm:text-3xl">
          Smart Solar Microgrid
        </h2>
        <p className="mt-2 text-sm text-slate-400">
          Enterprise Trading & Microgrid Management Portal
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="bg-white py-8 px-6 sm:px-10 shadow-2xl rounded-2xl border border-slate-100">
          <div className="mb-6">
            <h3 className="text-lg font-semibold text-slate-900">Sign in to your account</h3>
            <p className="text-xs text-slate-500 mt-1">
              Role permissions are centrally validated by the ASP.NET Web API.
            </p>
          </div>

          {errorMessage && (
            <ErrorAlert
              title="Authentication Error"
              message={errorMessage}
              onClose={() => setErrorMessage('')}
              className="mb-6"
            />
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <Input
              label="Username or Email"
              type="text"
              name="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="e.g. admin or operator@grid.lk"
              icon={HiOutlineUser}
              required
              autoFocus
            />

            <Input
              label="Password"
              type="password"
              name="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              icon={HiOutlineLockClosed}
              required
            />

            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full mt-2"
              isLoading={isLoading}
            >
              Sign In
            </Button>
          </form>

          {/* Quick Demo Access Bar */}
          <div className="mt-6 pt-5 border-t border-slate-100">
            <span className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider block mb-2 text-center">
              Quick Test Accounts
            </span>
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => {
                  setUsername('admin@gmail.com');
                  setPassword('admin123');
                }}
                className="px-2.5 py-1.5 rounded-lg text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 border border-slate-200 text-center transition"
              >
                Backoffice
                <span className="block text-[10px] text-slate-500 font-normal">admin@gmail.com</span>
              </button>

              <button
                type="button"
                onClick={() => {
                  setUsername('rashi@gmail.com');
                  setPassword('operator123');
                }}
                className="px-2.5 py-1.5 rounded-lg text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 border border-slate-200 text-center transition"
              >
                Grid Operator
                <span className="block text-[10px] text-slate-500 font-normal">rashi@gmail.com</span>
              </button>
            </div>

            <div className="text-[11px] text-slate-500 flex items-center gap-1.5 justify-center mt-3">
              <HiOutlineShieldCheck className="w-4 h-4 text-emerald-600 shrink-0" />
              <span>Directs automatically to <strong>Backoffice</strong> or <strong>Grid Operator</strong></span>
            </div>
          </div>
        </div>

        <p className="mt-6 text-center text-xs text-slate-500">
          Enterprise Application Development • University Project
        </p>
      </div>
    </div>
  );
}
