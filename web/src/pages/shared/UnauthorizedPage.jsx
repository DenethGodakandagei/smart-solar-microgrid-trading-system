import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import Button from '../../components/common/Button';
import { HiOutlineShieldExclamation, HiOutlineArrowLeft } from 'react-icons/hi2';
import { ROLES } from '../../utils/constants';

export default function UnauthorizedPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const handleReturn = () => {
    if (user?.role === ROLES.BACKOFFICE) {
      navigate('/backoffice/dashboard');
    } else if (user?.role === ROLES.GRID_OPERATOR) {
      navigate('/operator/dashboard');
    } else {
      navigate('/login');
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="max-w-md w-full text-center bg-white p-8 rounded-2xl border border-slate-200 shadow-sm">
        <div className="w-16 h-16 rounded-2xl bg-amber-50 text-amber-600 border border-amber-100 flex items-center justify-center mx-auto mb-4">
          <HiOutlineShieldExclamation className="w-8 h-8" />
        </div>
        <h2 className="text-2xl font-bold text-slate-800">403 - Access Restricted</h2>
        <p className="text-sm text-slate-500 mt-2 mb-2">
          Your current account role (<strong className="text-slate-700">{user?.role || 'Unknown'}</strong>) does not have authorization to view this resource.
        </p>
        <p className="text-xs text-slate-400 mb-6">
          Authorization permissions are enforced by the central C# Web API.
        </p>
        <Button variant="primary" onClick={handleReturn} icon={HiOutlineArrowLeft}>
          Back to Authorized Dashboard
        </Button>
      </div>
    </div>
  );
}
