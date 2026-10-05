import React from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../../components/common/Button';
import { HiOutlineExclamationCircle, HiOutlineHome } from 'react-icons/hi2';

export default function NotFoundPage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="max-w-md w-full text-center bg-white p-8 rounded-2xl border border-slate-200 shadow-sm">
        <div className="w-16 h-16 rounded-2xl bg-slate-100 flex items-center justify-center text-slate-400 mx-auto mb-4">
          <HiOutlineExclamationCircle className="w-8 h-8" />
        </div>
        <h2 className="text-2xl font-bold text-slate-800">404 - Page Not Found</h2>
        <p className="text-sm text-slate-500 mt-2 mb-6">
          The requested route does not exist or may have been relocated.
        </p>
        <Button variant="primary" onClick={() => navigate('/login')} icon={HiOutlineHome}>
          Return to Portal
        </Button>
      </div>
    </div>
  );
}
