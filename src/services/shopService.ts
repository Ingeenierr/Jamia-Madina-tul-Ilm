import { ref, onValue, push, set, update, remove, off, runTransaction, query, orderByChild, startAt, endAt } from 'firebase/database';
import { db } from './firebase';
import { ShopItem, ShopSale } from '@/types/models';

const ITEMS_PATH = 'shopItems';
const SALES_PATH = 'shopSales';

export function subscribeShopItems(onData: (items: ShopItem[]) => void): () => void {
  const r = ref(db, ITEMS_PATH);
  const listener = onValue(
    r,
    (snap) => {
      const val = snap.val() as Record<string, ShopItem> | null;
      const list = val ? Object.values(val) : [];
      list.sort((a, b) => a.name.localeCompare(b.name));
      onData(list);
    },
    (error) => {
      console.error('subscribeShopItems failed:', error);
      onData([]);
    }
  );
  return () => off(r, 'value', listener);
}

export async function addShopItem(item: Omit<ShopItem, 'id'>): Promise<string> {
  const newRef = push(ref(db, ITEMS_PATH));
  const id = newRef.key as string;
  await set(newRef, { ...item, id });
  return id;
}

export async function updateShopItem(id: string, patch: Partial<ShopItem>): Promise<void> {
  await update(ref(db, `${ITEMS_PATH}/${id}`), patch);
}

export async function deleteShopItem(id: string): Promise<void> {
  await remove(ref(db, `${ITEMS_PATH}/${id}`));
}

export class ShopError extends Error {}

/**
 * Same stock-safe pattern as the library: a transaction on `stock` means
 * two quick taps (or two staff members selling at once) can never oversell
 * past zero — the second attempt is re-evaluated against the real current
 * value and rejected with a clear error instead of silently going negative.
 */
export async function recordSale(item: ShopItem, quantity: number, studentId?: string): Promise<void> {
  if (quantity <= 0) throw new ShopError('Quantity must be at least 1.');

  const stockRef = ref(db, `${ITEMS_PATH}/${item.id}/stock`);
  const result = await runTransaction(stockRef, (current: number | null) => {
    const currentVal = current ?? item.stock;
    if (currentVal < quantity) return; // abort — not enough stock
    return currentVal - quantity;
  });

  if (!result.committed) {
    throw new ShopError(`Only ${result.snapshot.val() ?? 0} left in stock — lower the quantity.`);
  }

  const dateKey = new Date().toISOString().slice(0, 10);
  const newRef = push(ref(db, SALES_PATH));
  const sale: ShopSale = {
    id: newRef.key as string,
    itemId: item.id,
    itemName: item.name,
    quantity,
    unitPrice: item.price,
    totalAmount: item.price * quantity,
    studentId,
    date: dateKey,
    timestamp: Date.now(),
  };
  await set(newRef, sale);
}

export function subscribeSalesForMonth(monthPrefix: string, onData: (sales: ShopSale[]) => void): () => void {
  const monthQuery = query(
    ref(db, SALES_PATH),
    orderByChild('date'),
    startAt(`${monthPrefix}-01`),
    endAt(`${monthPrefix}-31`)
  );
  const listener = onValue(
    monthQuery,
    (snap) => {
      const val = snap.val() as Record<string, ShopSale> | null;
      const list = val ? Object.values(val) : [];
      list.sort((a, b) => b.timestamp - a.timestamp);
      onData(list);
    },
    (error) => {
      console.error('subscribeSalesForMonth failed:', error);
      onData([]);
    }
  );
  return () => off(monthQuery, 'value', listener);
}
