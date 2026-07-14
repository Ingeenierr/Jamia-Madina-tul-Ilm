import { ref, onValue, push, set, update, remove, off } from 'firebase/database';
import { db } from './firebase';
import { Student } from '@/types/models';

const STUDENTS_PATH = 'students';

export function subscribeStudents(onData: (students: Student[]) => void): () => void {
  const studentsRef = ref(db, STUDENTS_PATH);
  const listener = onValue(studentsRef, (snap) => {
    const val = snap.val() as Record<string, Student> | null;
    const list = val ? Object.values(val) : [];
    list.sort((a, b) => a.fullName.localeCompare(b.fullName));
    onData(list);
  });
  return () => off(studentsRef, 'value', listener);
}

export async function addStudent(student: Omit<Student, 'id' | 'timestamp'>): Promise<string> {
  const newRef = push(ref(db, STUDENTS_PATH));
  const id = newRef.key as string;
  const record: Student = { ...student, id, timestamp: Date.now() };
  await set(newRef, record);
  return id;
}

export async function updateStudent(id: string, patch: Partial<Student>): Promise<void> {
  await update(ref(db, `${STUDENTS_PATH}/${id}`), patch);
}

export async function deleteStudent(id: string): Promise<void> {
  await remove(ref(db, `${STUDENTS_PATH}/${id}`));
}

export async function setStudentActive(id: string, active: boolean): Promise<void> {
  await update(ref(db, `${STUDENTS_PATH}/${id}`), { active });
}
