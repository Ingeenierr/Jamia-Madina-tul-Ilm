import { ref, onValue, push, set, update, off, get, query, orderByChild, equalTo } from 'firebase/database';
import { db } from './firebase';
import { FeePayment } from '@/types/models';

const PATH = 'fees';

export function subscribeFeesForMonth(month: string, onData: (records: FeePayment[]) => void): () => void {
  const monthQuery = query(ref(db, PATH), orderByChild('month'), equalTo(month));
  const listener = onValue(
    monthQuery,
    (snap) => {
      const val = snap.val() as Record<string, FeePayment> | null;
      const list = val ? Object.values(val) : [];
      list.sort((a, b) => a.studentName.localeCompare(b.studentName));
      onData(list);
    },
    (error) => {
      console.error('subscribeFeesForMonth failed:', error);
      onData([]);
    }
  );
  return () => off(monthQuery, 'value', listener);
}

/** Ensures a fee row exists for every active student for the given month. */
export async function ensureFeeRowsForMonth(
  month: string,
  students: { id: string; fullName: string }[],
  defaultAmount: number
): Promise<void> {
  const existingSnap = await get(query(ref(db, PATH), orderByChild('month'), equalTo(month)));
  const existing = (existingSnap.val() as Record<string, FeePayment> | null) || {};
  const existingStudentIds = new Set(Object.values(existing).map((r) => r.studentId));

  const writes = students
    .filter((s) => !existingStudentIds.has(s.id))
    .map((s) => {
      const newRef = push(ref(db, PATH));
      const record: FeePayment = {
        id: newRef.key as string,
        studentId: s.id,
        studentName: s.fullName,
        month,
        amountDue: defaultAmount,
        amountPaid: 0,
        status: 'UNPAID',
      };
      return set(newRef, record);
    });

  await Promise.all(writes);
}

export async function recordFeePayment(id: string, amountPaid: number, amountDue: number): Promise<void> {
  const status = amountPaid >= amountDue ? 'PAID' : amountPaid > 0 ? 'PARTIAL' : 'UNPAID';
  await update(ref(db, `${PATH}/${id}`), { amountPaid, status, paidOn: Date.now() });
}
