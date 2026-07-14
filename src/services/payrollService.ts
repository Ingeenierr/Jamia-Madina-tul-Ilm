import { ref, onValue, push, set, update, off, get, query, orderByChild, equalTo } from 'firebase/database';
import { db } from './firebase';
import { PayrollRecord } from '@/types/models';

const PATH = 'payroll';

export function subscribePayrollForMonth(month: string, onData: (records: PayrollRecord[]) => void): () => void {
  const monthQuery = query(ref(db, PATH), orderByChild('month'), equalTo(month));
  const listener = onValue(
    monthQuery,
    (snap) => {
      const val = snap.val() as Record<string, PayrollRecord> | null;
      const list = val ? Object.values(val) : [];
      list.sort((a, b) => a.teacherName.localeCompare(b.teacherName));
      onData(list);
    },
    (error) => {
      console.error('subscribePayrollForMonth failed:', error);
      onData([]);
    }
  );
  return () => off(monthQuery, 'value', listener);
}

/** Ensures a PENDING payroll row exists for every teacher for the given month, without duplicating rows on repeat calls. */
export async function ensurePayrollRowsForMonth(
  month: string,
  teachers: { id: string; name: string; salary: number }[]
): Promise<void> {
  const existingSnap = await get(query(ref(db, PATH), orderByChild('month'), equalTo(month)));
  const existing = (existingSnap.val() as Record<string, PayrollRecord> | null) || {};
  const existingTeacherIds = new Set(Object.values(existing).map((r) => r.teacherId));

  const writes = teachers
    .filter((t) => !existingTeacherIds.has(t.id))
    .map((t) => {
      const newRef = push(ref(db, PATH));
      const record: PayrollRecord = {
        id: newRef.key as string,
        teacherId: t.id,
        teacherName: t.name,
        amount: t.salary,
        month,
        status: 'UNPAID',
      };
      return set(newRef, record);
    });

  await Promise.all(writes);
}

export async function markPayrollPaid(id: string): Promise<void> {
  await update(ref(db, `${PATH}/${id}`), { status: 'PAID', paidOn: Date.now() });
}

export async function markPayrollUnpaid(id: string): Promise<void> {
  await update(ref(db, `${PATH}/${id}`), { status: 'UNPAID', paidOn: null });
}
