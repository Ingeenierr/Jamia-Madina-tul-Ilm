import { ref, onValue, push, set, update, remove, off } from 'firebase/database';
import { db } from './firebase';
import { Teacher } from '@/types/models';

const TEACHERS_PATH = 'teachers';

export function subscribeTeachers(onData: (teachers: Teacher[]) => void): () => void {
  const teachersRef = ref(db, TEACHERS_PATH);
  const listener = onValue(teachersRef, (snap) => {
    const val = snap.val() as Record<string, Teacher> | null;
    const list = val ? Object.values(val) : [];
    list.sort((a, b) => a.name.localeCompare(b.name));
    onData(list);
  });
  return () => off(teachersRef, 'value', listener);
}

export async function addTeacher(teacher: Omit<Teacher, 'id'>): Promise<string> {
  const newRef = push(ref(db, TEACHERS_PATH));
  const id = newRef.key as string;
  await set(newRef, { ...teacher, id });
  return id;
}

export async function updateTeacher(id: string, patch: Partial<Teacher>): Promise<void> {
  await update(ref(db, `${TEACHERS_PATH}/${id}`), patch);
}

export async function deleteTeacher(id: string): Promise<void> {
  await remove(ref(db, `${TEACHERS_PATH}/${id}`));
}
