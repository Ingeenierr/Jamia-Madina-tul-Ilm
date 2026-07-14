import { ref, onValue, set, off, get, query, orderByKey, startAt, endAt } from 'firebase/database';
import { db } from './firebase';
import { AttendanceRecord, AttendanceStatus } from '@/types/models';

const ATTENDANCE_PATH = 'attendance';

/**
 * Schema: attendance/{yyyy-MM-dd}/{studentId}/{classId} = status
 *
 * This is the fix for the bug in the original Kotlin app, where a single
 * flat `attendance` collection was queried with
 * `orderByChild("date").equalTo(date)` and matched back to a record by
 * `studentId` alone. Because the listener for each date was never removed
 * when the date changed, stale listeners could overwrite the wrong day's
 * record. Here, every date is its own top-level key — writing to
 * attendance/2026-07-11/... can never touch attendance/2026-07-10/...,
 * so there's no query, no cross-date matching, and nothing to race.
 */

export function subscribeAttendanceForDate(
  date: string,
  onData: (records: Record<string, Record<string, AttendanceStatus>>) => void
): () => void {
  const dateRef = ref(db, `${ATTENDANCE_PATH}/${date}`);
  const listener = onValue(dateRef, (snap) => {
    onData((snap.val() as Record<string, Record<string, AttendanceStatus>>) || {});
  });
  return () => off(dateRef, 'value', listener);
}

export async function markAttendance(
  date: string,
  studentId: string,
  classId: string,
  status: AttendanceStatus
): Promise<void> {
  await set(ref(db, `${ATTENDANCE_PATH}/${date}/${studentId}/${classId}`), status);
}

export async function getAttendanceForDate(
  date: string
): Promise<Record<string, Record<string, AttendanceStatus>>> {
  const snap = await get(ref(db, `${ATTENDANCE_PATH}/${date}`));
  return (snap.val() as Record<string, Record<string, AttendanceStatus>>) || {};
}

/**
 * Fetch every date's attendance between two yyyy-MM-dd keys (inclusive).
 * Used for weekly charts and the attendance history screen. Cheap because
 * it's a single ranged read on the top-level date keys, not N queries.
 */
export async function fetchAttendanceRange(
  startDate: string,
  endDateInclusive: string
): Promise<Record<string, Record<string, Record<string, AttendanceStatus>>>> {
  const rangeQuery = query(ref(db, ATTENDANCE_PATH), orderByKey(), startAt(startDate), endAt(endDateInclusive));
  const snap = await get(rangeQuery);
  return (snap.val() as Record<string, Record<string, Record<string, AttendanceStatus>>>) || {};
}

export interface DayAttendanceSummary {
  date: string;
  present: number;
  absent: number;
  leave: number;
  total: number;
}

/** Collapses a range of raw records into one summary row per day, for charts/history lists. */
export function summarizeByDay(
  range: Record<string, Record<string, Record<string, AttendanceStatus>>>
): DayAttendanceSummary[] {
  return Object.entries(range)
    .map(([date, byStudent]) => {
      let present = 0;
      let absent = 0;
      let leave = 0;
      Object.values(byStudent).forEach((byClass) => {
        const statuses = Object.values(byClass);
        // A student counts once per day: present if present in any class,
        // else leave if any leave, else absent if any absent recorded.
        if (statuses.includes('PRESENT')) present += 1;
        else if (statuses.includes('LEAVE')) leave += 1;
        else if (statuses.includes('ABSENT')) absent += 1;
      });
      return { date, present, absent, leave, total: present + absent + leave };
    })
    .sort((a, b) => a.date.localeCompare(b.date));
}

export interface StudentAttendanceStats {
  presentDays: number;
  absentDays: number;
  leaveDays: number;
  markedDays: number;
  percentage: number; // present / markedDays * 100
}

/** Per-student stats over a fetched range, for the student profile screen. */
export function summarizeForStudent(
  range: Record<string, Record<string, Record<string, AttendanceStatus>>>,
  studentId: string
): StudentAttendanceStats {
  let presentDays = 0;
  let absentDays = 0;
  let leaveDays = 0;

  Object.values(range).forEach((byStudent) => {
    const byClass = byStudent[studentId];
    if (!byClass) return;
    const statuses = Object.values(byClass);
    if (statuses.includes('PRESENT')) presentDays += 1;
    else if (statuses.includes('LEAVE')) leaveDays += 1;
    else if (statuses.includes('ABSENT')) absentDays += 1;
  });

  const markedDays = presentDays + absentDays + leaveDays;
  return {
    presentDays,
    absentDays,
    leaveDays,
    markedDays,
    percentage: markedDays > 0 ? Math.round((presentDays / markedDays) * 100) : 0,
  };
}

export function dateKeyDaysAgo(daysAgo: number): string {
  const d = new Date();
  d.setDate(d.getDate() - daysAgo);
  return d.toISOString().slice(0, 10);
}
