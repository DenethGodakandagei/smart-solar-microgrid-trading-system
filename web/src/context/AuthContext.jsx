import { createContext, useState, useEffect, useCallback } from 'react';
import { login as apiLogin, logout as apiLogout, getCurrentUser } from '../services/authService';

export const AuthContext = createContext(null);

/**
 * AuthProvider
 * Wraps the app and provides authentication state + methods.
 *
 * State shape:
 *   user: { id, name, email, role } | null
 *   token: string | null
 *   isAuthenticated: boolean
 *   isLoading: boolean  (true during initial token validation)
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(() => localStorage.getItem('authToken'));
  const [isLoading, setIsLoading] = useState(true);

  const isAuthenticated = !!user && !!token;

  // On mount, validate the stored token by calling the API
  useEffect(() => {
    const validateToken = async () => {
      const storedToken = localStorage.getItem('authToken');
      if (!storedToken) {
        setIsLoading(false);
        return;
      }

      try {
        const userData = await getCurrentUser();
        setUser(userData);
        setToken(storedToken);
      } catch {
        // Token is invalid or expired — clean up
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUser');
        setUser(null);
        setToken(null);
      } finally {
        setIsLoading(false);
      }
    };

    validateToken();
  }, []);

  /**
   * login — sends credentials to the API, stores token and user
   * @param {{ username: string, password: string }} credentials
   * @returns {object} The authenticated user object (contains role for routing)
   */
  const login = useCallback(async (credentials) => {
    const data = await apiLogin(credentials);

    // Expected API response shape: { token, user: { id, name, email, role } }
    const { token: newToken, user: userData } = data;

    localStorage.setItem('authToken', newToken);
    localStorage.setItem('authUser', JSON.stringify(userData));
    setToken(newToken);
    setUser(userData);

    return userData;
  }, []);

  /**
   * logout — calls API logout, clears all auth state
   */
  const logout = useCallback(async () => {
    try {
      await apiLogout();
    } catch {
      // Even if the API call fails, clear local state
    } finally {
      localStorage.removeItem('authToken');
      localStorage.removeItem('authUser');
      setToken(null);
      setUser(null);
    }
  }, []);

  const value = {
    user,
    token,
    isAuthenticated,
    isLoading,
    login,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}
