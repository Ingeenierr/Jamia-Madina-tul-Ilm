import { ref, onValue, push, set, remove, off } from 'firebase/database';
import { db } from './firebase';
import { Donation } from '@/types/models';

const PATH = 'donations';

export function subscribeDonations(onData: (donations: Donation[]) => void): () => void {
  const r = ref(db, PATH);
  const listener = onValue(
    r,
    (snap) => {
      const val = snap.val() as Record<string, Donation> | null;
      const list = val ? Object.values(val) : [];
      list.sort((a, b) => b.date.localeCompare(a.date));
      onData(list);
    },
    (error) => {
      console.error('subscribeDonations failed:', error);
      onData([]);
    }
  );
  return () => off(r, 'value', listener);
}

export async function addDonation(donation: Omit<Donation, 'id'>): Promise<string> {
  const newRef = push(ref(db, PATH));
  const id = newRef.key as string;
  await set(newRef, { ...donation, id });
  return id;
}

export async function deleteDonation(id: string): Promise<void> {
  await remove(ref(db, `${PATH}/${id}`));
}
