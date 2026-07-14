import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { onAuthStateChanged } from 'firebase/auth';
import { ref, onValue, off } from 'firebase/database';
import { auth, db } from '@/services/firebase';
import { AppUser } from '@/types/models';
import * as authService from '@/services/authService';

interface AuthContextValue {
  user: AppUser | null;
  initializing: boolean;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  signUp: (params: { name: string; email: string; password: string; role: 'ADMIN' | 'TEACHER' }) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AppUser | null>(null);
  const [initializing, setInitializing] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const unsub = onAuthStateChanged(auth, (fbUser) => {
      if (!fbUser) {
        setUser(null);
        setInitializing(false);
        return;
      }
      const profileRef = ref(db, `users/${fbUser.uid}`);
      const listener = onValue(profileRef, (snap) => {
        const profile = snap.val() as AppUser | null;
        setUser(profile ? { ...profile, id: fbUser.uid } : null);
        setInitializing(false);
      });
      return () => off(profileRef, 'value', listener);
    });
    return unsub;
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      initializing,
      error,
      clearError: () => setError(null),
      login: async (email, password) => {
        setError(null);
        try {
          await authService.login(email, password);
        } catch (e: any) {
          setError(e.message ?? 'Could not sign in.');
          throw e;
        }
      },
      signUp: async (params) => {
        setError(null);
        try {
          await authService.signUp(params);
        } catch (e: any) {
          setError(e.message ?? 'Could not create account.');
          throw e;
        }
      },
      logout: async () => {
        await authService.signOut(user?.id);
      },
    }),
    [user, initializing, error]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
