// Mirrors app/src/main/java/com/jamia/madinatulilm/data/* from the original
// Kotlin app 1:1, so the Realtime Database schema doesn't need to change.

export type UserRole = 'ADMIN' | 'TEACHER';
export type UserStatus = 'PENDING' | 'APPROVED' | 'DISAPPROVED';

export interface AppUser {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  status: UserStatus;
  lastSeen?: number | null;
  online?: boolean;
  typingIn?: string | null;
}

export interface Student {
  id: string;
  fullName: string;
  age: number;
  guardianName: string;
  contactNumber: string;
  cnic: string;
  address: string;
  dob: string;
  classId: string;
  active: boolean;
  timestamp: number;
  photoUrl?: string;
}

export interface Teacher {
  id: string;
  name: string;
  qualifications: string;
  contactInfo: string;
  cnic: string;
  address: string;
  dob: string;
  salary: number;
  email: string;
  classIds: string[];
  isPaid: boolean;
  photoUrl?: string;
}

export interface MadrasaClass {
  id: string;
  className: string;
  level: string;
  room: string;
  teacherId: string;
  studentIds: string[];
}

export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LEAVE';

export interface AttendanceRecord {
  id: string;
  studentId: string;
  date: string; // yyyy-MM-dd
  attendance: Record<string, AttendanceStatus>; // classId -> status
}

export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface LeaveRequest {
  id: string;
  teacherId: string;
  teacherName: string;
  reason: string;
  date: string;
  timestamp: number;
  status: LeaveStatus;
}

export interface Donation {
  id: string;
  donorName: string;
  studentId?: string;
  amount: number;
  date: string;
  type: string;
  note?: string;
}

export type PayrollStatus = 'PAID' | 'UNPAID';

export interface PayrollRecord {
  id: string;
  teacherId: string;
  teacherName: string;
  amount: number;
  month: string; // yyyy-MM
  status: PayrollStatus;
  paidOn?: number; // epoch ms
}

export type CanteenEntryType = 'INCOME' | 'EXPENSE';

export interface CanteenEntry {
  id: string;
  type: CanteenEntryType;
  category: string;
  amount: number;
  date: string; // yyyy-MM-dd
  note?: string;
  timestamp: number;
}

export type FeeStatus = 'PAID' | 'UNPAID' | 'PARTIAL';

export interface FeePayment {
  id: string;
  studentId: string;
  studentName: string;
  month: string; // yyyy-MM
  amountDue: number;
  amountPaid: number;
  status: FeeStatus;
  paidOn?: number;
}

export interface Book {
  id: string;
  title: string;
  author: string;
  isbn?: string;
  category: string;
  totalCopies: number;
  availableCopies: number;
}

export type LendingStatus = 'BORROWED' | 'RETURNED' | 'OVERDUE';

export interface BookLending {
  id: string;
  bookId: string;
  bookTitle: string;
  studentId: string;
  studentName: string;
  borrowedOn: number;
  dueDate: string; // yyyy-MM-dd
  returnedOn?: number;
  status: LendingStatus;
}

export interface ShopItem {
  id: string;
  name: string;
  category: string;
  price: number;
  stock: number;
}

export interface ShopSale {
  id: string;
  itemId: string;
  itemName: string;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  studentId?: string;
  date: string; // yyyy-MM-dd
  timestamp: number;
}
