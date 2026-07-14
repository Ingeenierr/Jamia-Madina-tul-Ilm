import { ref, onValue, push, set, update, remove, off } from 'firebase/database';
import { db } from './firebase';
import { MadrasaClass } from '@/types/models';

const CLASSES_PATH = 'classes';

export function subscribeClasses(onData: (classes: MadrasaClass[]) => void): () => void {
  const classesRef = ref(db, CLASSES_PATH);
  const listener = onValue(classesRef, (snap) => {
    const val = snap.val() as Record<string, MadrasaClass> | null;
    const list = val ? Object.values(val) : [];
    list.sort((a, b) => a.className.localeCompare(b.className));
    onData(list);
  });
  return () => off(classesRef, 'value', listener);
}

export async function addClass(cls: Omit<MadrasaClass, 'id'>): Promise<string> {
  const newRef = push(ref(db, CLASSES_PATH));
  const id = newRef.key as string;
  await set(newRef, { ...cls, id });
  return id;
}

export async function updateClass(id: string, patch: Partial<MadrasaClass>): Promise<void> {
  await update(ref(db, `${CLASSES_PATH}/${id}`), patch);
}

export async function deleteClass(id: string): Promise<void> {
  await remove(ref(db, `${CLASSES_PATH}/${id}`));
}
