import { ref, onValue, push, set, remove, off, query, orderByChild, startAt, endAt } from 'firebase/database';
import { db } from './firebase';
import { CanteenEntry } from '@/types/models';

const PATH = 'canteenLedger';

export function subscribeCanteenForMonth(monthPrefix: string, onData: (entries: CanteenEntry[]) => void): () => void {
  // Dates are yyyy-MM-dd, so a startAt/endAt range on the "date" index
  // cleanly captures a whole month without needing a separate month field.
  const monthQuery = query(
    ref(db, PATH),
    orderByChild('date'),
    startAt(`${monthPrefix}-01`),
    endAt(`${monthPrefix}-31`)
  );
  const listener = onValue(
    monthQuery,
    (snap) => {
      const val = snap.val() as Record<string, CanteenEntry> | null;
      const list = val ? Object.values(val) : [];
      list.sort((a, b) => b.timestamp - a.timestamp);
      onData(list);
    },
    (error) => {
      console.error('subscribeCanteenForMonth failed:', error);
      onData([]);
    }
  );
  return () => off(monthQuery, 'value', listener);
}

export async function addCanteenEntry(entry: Omit<CanteenEntry, 'id' | 'timestamp'>): Promise<string> {
  const newRef = push(ref(db, PATH));
  const id = newRef.key as string;
  await set(newRef, { ...entry, id, timestamp: Date.now() });
  return id;
}

export async function deleteCanteenEntry(id: string): Promise<void> {
  await remove(ref(db, `${PATH}/${id}`));
}
