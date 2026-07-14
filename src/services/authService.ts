import {
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signOut as fbSignOut,
} from 'firebase/auth';
import { ref, get, set, update } from 'firebase/database';
import { auth, db } from './firebase';
import { AppUser, UserRole } from '@/types/models';

export class AuthError extends Error {}

export async function login(email: string, password: string): Promise<AppUser> {
  const cred = await signInWithEmailAndPassword(auth, email.trim(), password);
  const snap = await get(ref(db, `users/${cred.user.uid}`));
  if (!snap.exists()) throw new AuthError('No profile found for this account.');
  const profile = snap.val() as AppUser;
  if (profile.status === 'DISAPPROVED') {
    await fbSignOut(auth);
    throw new AuthError('Your account was not approved by the admin.');
  }
  if (profile.status === 'PENDING') {
    await fbSignOut(auth);
    throw new AuthError('Your account is awaiting admin approval.');
  }
  await update(ref(db, `users/${cred.user.uid}`), { online: true, lastSeen: Date.now() });
  return { ...profile, id: cred.user.uid };
}

export async function signUp(params: {
  name: string;
  email: string;
  password: string;
  role: UserRole;
}): Promise<void> {
  const cred = await createUserWithEmailAndPassword(auth, params.email.trim(), params.password);
  const newUser: AppUser = {
    id: cred.user.uid,
    name: params.name.trim(),
    email: params.email.trim(),
    role: params.role,
    // Admins still gate teacher accounts before they can log in — mirrors
    // the original UserStatus.PENDING flow in ApprovalScreen.kt.
    status: params.role === 'TEACHER' ? 'PENDING' : 'APPROVED',
  };
  await set(ref(db, `users/${cred.user.uid}`), newUser);
  await fbSignOut(auth);
}

export async function signOut(uid?: string): Promise<void> {
  if (uid) {
    await update(ref(db, `users/${uid}`), { online: false, lastSeen: Date.now() });
  }
  await fbSignOut(auth);
}
