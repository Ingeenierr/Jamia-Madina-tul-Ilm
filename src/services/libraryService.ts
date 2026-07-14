import { ref, onValue, push, set, update, remove, off, runTransaction, get } from 'firebase/database';
import { db } from './firebase';
import { Book, BookLending } from '@/types/models';

const BOOKS_PATH = 'books';
const LENDINGS_PATH = 'lendings';

export function subscribeBooks(onData: (books: Book[]) => void): () => void {
  const r = ref(db, BOOKS_PATH);
  const listener = onValue(
    r,
    (snap) => {
      const val = snap.val() as Record<string, Book> | null;
      const list = val ? Object.values(val) : [];
      list.sort((a, b) => a.title.localeCompare(b.title));
      onData(list);
    },
    (error) => {
      console.error('subscribeBooks failed:', error);
      onData([]);
    }
  );
  return () => off(r, 'value', listener);
}

export async function addBook(book: Omit<Book, 'id' | 'availableCopies'>): Promise<string> {
  const newRef = push(ref(db, BOOKS_PATH));
  const id = newRef.key as string;
  await set(newRef, { ...book, id, availableCopies: book.totalCopies });
  return id;
}

export async function updateBook(id: string, patch: Partial<Book>): Promise<void> {
  await update(ref(db, `${BOOKS_PATH}/${id}`), patch);
}

export async function deleteBook(id: string): Promise<void> {
  await remove(ref(db, `${BOOKS_PATH}/${id}`));
}

export function subscribeLendingsForBook(bookId: string, onData: (lendings: BookLending[]) => void): () => void {
  const r = ref(db, LENDINGS_PATH);
  const listener = onValue(
    r,
    (snap) => {
      const val = snap.val() as Record<string, BookLending> | null;
      const list = val ? Object.values(val).filter((l) => l.bookId === bookId) : [];
      list.sort((a, b) => b.borrowedOn - a.borrowedOn);
      onData(list);
    },
    (error) => {
      console.error('subscribeLendingsForBook failed:', error);
      onData([]);
    }
  );
  return () => off(r, 'value', listener);
}

export class LibraryError extends Error {}

/**
 * Uses a Firebase transaction on availableCopies so two people lending the
 * last copy at the same moment can't both succeed — the second write is
 * re-run against the fresh value and correctly rejected once stock hits 0.
 */
export async function lendBook(
  book: Book,
  studentId: string,
  studentName: string,
  dueDate: string
): Promise<void> {
  const availabilityRef = ref(db, `${BOOKS_PATH}/${book.id}/availableCopies`);
  const result = await runTransaction(availabilityRef, (current: number | null) => {
    const currentVal = current ?? book.totalCopies;
    if (currentVal <= 0) return; // abort transaction, stock is 0
    return currentVal - 1;
  });

  if (!result.committed) {
    throw new LibraryError('No copies available right now — someone may have just borrowed the last one.');
  }

  const newRef = push(ref(db, LENDINGS_PATH));
  const record: BookLending = {
    id: newRef.key as string,
    bookId: book.id,
    bookTitle: book.title,
    studentId,
    studentName,
    borrowedOn: Date.now(),
    dueDate,
    status: 'BORROWED',
  };
  await set(newRef, record);
}

export async function returnBook(lending: BookLending): Promise<void> {
  await update(ref(db, `${LENDINGS_PATH}/${lending.id}`), { status: 'RETURNED', returnedOn: Date.now() });
  const availabilityRef = ref(db, `${BOOKS_PATH}/${lending.bookId}/availableCopies`);
  await runTransaction(availabilityRef, (current: number | null) => (current ?? 0) + 1);
}
