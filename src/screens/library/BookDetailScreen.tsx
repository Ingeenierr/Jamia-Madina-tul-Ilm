import React, { useEffect, useMemo, useState } from 'react';
import { StyleSheet, View, Text, ScrollView, Pressable, Modal, KeyboardAvoidingView, Platform, FlatList } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import Animated, { SlideInDown } from 'react-native-reanimated';
import { ref, get } from 'firebase/database';
import { db } from '@/services/firebase';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { AppTextInput } from '@/components/AppTextInput';
import { PrimaryButton } from '@/components/PrimaryButton';
import { StatusPill } from '@/components/StatusPill';
import { EmptyState } from '@/components/EmptyState';
import { useToast } from '@/components/Toast';
import { type, useTheme } from '@/theme';
import { subscribeBooks, subscribeLendingsForBook, lendBook, returnBook } from '@/services/libraryService';
import { subscribeStudents } from '@/services/studentService';
import { Book, BookLending, Student } from '@/types/models';

function defaultDueDate(): string {
  const d = new Date();
  d.setDate(d.getDate() + 14);
  return d.toISOString().slice(0, 10);
}

export function BookDetailScreen({ route }: any) {
  const { colors } = useTheme();
  const { show } = useToast();
  const bookId: string = route.params.bookId;
  const [book, setBook] = useState<Book | null>(null);
  const [lendings, setLendings] = useState<BookLending[]>([]);
  const [students, setStudents] = useState<Student[]>([]);
  const [lendModalOpen, setLendModalOpen] = useState(false);
  const [studentQuery, setStudentQuery] = useState('');
  const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
  const [dueDate, setDueDate] = useState(defaultDueDate());
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const unsub = subscribeBooks((all) => {
      const found = all.find((b) => b.id === bookId) ?? null;
      setBook(found);
    });
    return unsub;
  }, [bookId]);

  useEffect(() => subscribeLendingsForBook(bookId, setLendings), [bookId]);
  useEffect(() => subscribeStudents(setStudents), []);

  const activeLending = lendings.find((l) => l.status === 'BORROWED');

  const studentResults = useMemo(() => {
    if (!studentQuery.trim()) return students.slice(0, 6);
    const q = studentQuery.toLowerCase();
    return students.filter((s) => s.fullName.toLowerCase().includes(q)).slice(0, 6);
  }, [students, studentQuery]);

  const handleLend = async () => {
    if (!book || !selectedStudent) {
      show('Please select a student first.', 'error');
      return;
    }
    setSaving(true);
    try {
      await lendBook(book, selectedStudent.id, selectedStudent.fullName, dueDate);
      show(`Lent to ${selectedStudent.fullName}.`, 'success');
      setLendModalOpen(false);
      setSelectedStudent(null);
      setStudentQuery('');
    } catch (e: any) {
      show(e.message ?? 'Could not complete this loan.', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleReturn = async (lending: BookLending) => {
    try {
      await returnBook(lending);
      show('Marked as returned.', 'success');
    } catch (e: any) {
      show(e.message ?? 'Could not mark this as returned.', 'error');
    }
  };

  if (!book) {
    return (
      <GradientBackground variant="paper">
        <ScreenHeader eyebrow="Library" title="Loading…" />
      </GradientBackground>
    );
  }

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow={book.category} title={book.title} />
      <ScrollView contentContainerStyle={styles.content}>
        <GlassCard glow>
          <Text style={[type.bodyM, { color: colors.textSecondary }]}>by {book.author}</Text>
          <View style={styles.statsRow}>
            <View style={styles.statItem}>
              <Text style={[type.ledgerNumber, { color: colors.textPrimary }]}>{book.totalCopies}</Text>
              <Text style={[type.bodyS, { color: colors.textMuted }]}>Total copies</Text>
            </View>
            <View style={styles.statItem}>
              <Text style={[type.ledgerNumber, { color: book.availableCopies > 0 ? colors.statusApproved : colors.statusRejected }]}>
                {book.availableCopies}
              </Text>
              <Text style={[type.bodyS, { color: colors.textMuted }]}>Available</Text>
            </View>
          </View>
          {book.isbn ? <Text style={[type.bodyS, { color: colors.textMuted, marginTop: 10 }]}>ISBN {book.isbn}</Text> : null}

          <PrimaryButton
            label={book.availableCopies > 0 ? 'Lend this book' : 'No copies available'}
            onPress={() => setLendModalOpen(true)}
            disabled={book.availableCopies <= 0}
            style={{ marginTop: 16 }}
          />
        </GlassCard>

        <Text style={[type.label, { color: colors.textMuted, marginTop: 24, marginBottom: 12 }]}>LENDING HISTORY</Text>
        {lendings.length === 0 ? (
          <EmptyState icon="time-outline" title="No lending history yet" />
        ) : (
          lendings.map((l, i) => (
            <GlassCard key={l.id} delay={Math.min(i, 10) * 40} style={styles.lendRow}>
              <View style={{ flex: 1 }}>
                <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>{l.studentName}</Text>
                <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>
                  Due {l.dueDate}
                  {l.returnedOn ? ` · Returned ${new Date(l.returnedOn).toLocaleDateString()}` : ''}
                </Text>
              </View>
              {l.status === 'BORROWED' ? (
                <Pressable onPress={() => handleReturn(l)} style={[styles.returnBtn, { borderColor: colors.statusApproved }]}>
                  <Text style={[type.bodyS, { color: colors.statusApproved, fontWeight: '700' }]}>Mark returned</Text>
                </Pressable>
              ) : (
                <StatusPill label="Returned" tone="approved" />
              )}
            </GlassCard>
          ))
        )}
        <View style={{ height: 60 }} />
      </ScrollView>

      <Modal visible={lendModalOpen} transparent animationType="none" onRequestClose={() => setLendModalOpen(false)}>
        <View style={styles.modalOverlay}>
          <Pressable style={StyleSheet.absoluteFill} onPress={() => setLendModalOpen(false)} />
          <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
            <Animated.View entering={SlideInDown.duration(320).springify().damping(18)}>
              <GlassCard style={styles.modalCard}>
                <Text style={[type.displayM, { color: colors.textPrimary, marginBottom: 16 }]}>Lend "{book.title}"</Text>
                <AppTextInput
                  label="Student"
                  icon="search-outline"
                  value={selectedStudent ? selectedStudent.fullName : studentQuery}
                  onChangeText={(v) => {
                    setSelectedStudent(null);
                    setStudentQuery(v);
                  }}
                  placeholder="Search student by name"
                />
                {!selectedStudent && studentQuery.trim().length > 0 && (
                  <View style={[styles.suggestions, { borderColor: colors.hairline }]}>
                    {studentResults.map((s) => (
                      <Pressable key={s.id} onPress={() => setSelectedStudent(s)} style={styles.suggestionRow}>
                        <Text style={[type.bodyM, { color: colors.textPrimary }]}>{s.fullName}</Text>
                      </Pressable>
                    ))}
                    {studentResults.length === 0 && (
                      <Text style={[type.bodyS, { color: colors.textMuted, padding: 12 }]}>No matching students</Text>
                    )}
                  </View>
                )}
                <AppTextInput label="Due date" value={dueDate} onChangeText={setDueDate} placeholder="YYYY-MM-DD" />
                <PrimaryButton label="Confirm loan" onPress={handleLend} loading={saving} disabled={!selectedStudent} />
                <PrimaryButton label="Cancel" variant="ghost" onPress={() => setLendModalOpen(false)} style={{ marginTop: 4 }} />
              </GlassCard>
            </Animated.View>
          </KeyboardAvoidingView>
        </View>
      </Modal>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  content: { padding: 20 },
  statsRow: { flexDirection: 'row', gap: 24, marginTop: 12 },
  statItem: { alignItems: 'flex-start' },
  lendRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 10 },
  returnBtn: { paddingHorizontal: 12, paddingVertical: 8, borderRadius: 10, borderWidth: 1.5 },
  modalOverlay: { flex: 1, justifyContent: 'flex-end', backgroundColor: 'rgba(0,0,0,0.4)' },
  modalCard: { margin: 16, marginBottom: 24 },
  suggestions: { borderWidth: 1, borderRadius: 12, marginTop: -8, marginBottom: 16, overflow: 'hidden' },
  suggestionRow: { padding: 12 },
});
