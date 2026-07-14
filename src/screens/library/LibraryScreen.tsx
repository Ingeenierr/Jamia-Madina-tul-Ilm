import React, { useEffect, useMemo, useState } from 'react';
import { StyleSheet, View, Text, FlatList, Pressable, Modal, KeyboardAvoidingView, Platform } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import Animated, { SlideInDown } from 'react-native-reanimated';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { AppTextInput } from '@/components/AppTextInput';
import { PrimaryButton } from '@/components/PrimaryButton';
import { StatusPill } from '@/components/StatusPill';
import { EmptyState } from '@/components/EmptyState';
import { SkeletonList } from '@/components/SkeletonList';
import { useToast } from '@/components/Toast';
import { type, useTheme } from '@/theme';
import { subscribeBooks, addBook } from '@/services/libraryService';
import { Book } from '@/types/models';

export function LibraryScreen({ navigation }: any) {
  const { colors } = useTheme();
  const { show } = useToast();
  const [books, setBooks] = useState<Book[] | null>(null);
  const [query, setQuery] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ title: '', author: '', category: '', isbn: '', totalCopies: '1' });

  useEffect(() => subscribeBooks(setBooks), []);

  const filtered = useMemo(() => {
    if (!books) return [];
    if (!query.trim()) return books;
    const q = query.toLowerCase();
    return books.filter((b) => b.title.toLowerCase().includes(q) || b.author.toLowerCase().includes(q));
  }, [books, query]);

  const handleAdd = async () => {
    const copies = parseInt(form.totalCopies, 10);
    if (!form.title.trim()) {
      show('Please enter a title.', 'error');
      return;
    }
    if (!copies || copies < 1) {
      show('Total copies must be at least 1.', 'error');
      return;
    }
    setSaving(true);
    try {
      await addBook({
        title: form.title.trim(),
        author: form.author.trim() || 'Unknown',
        category: form.category.trim() || 'General',
        isbn: form.isbn.trim() || undefined,
        totalCopies: copies,
      });
      show('Book added to the library.', 'success');
      setForm({ title: '', author: '', category: '', isbn: '', totalCopies: '1' });
      setModalOpen(false);
    } catch (e: any) {
      show(e.message ?? 'Could not add this book.', 'error');
    } finally {
      setSaving(false);
    }
  };

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow={books ? `${books.length} titles` : 'Loading'} title="Library" />

      <View style={styles.searchWrap}>
        <AppTextInput label="Search" icon="search-outline" placeholder="Search by title or author" value={query} onChangeText={setQuery} />
      </View>

      {books === null ? (
        <SkeletonList rows={5} />
      ) : (
        <FlatList
          data={filtered}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.content}
          ListEmptyComponent={<EmptyState icon="library-outline" title="No books yet" subtitle="Tap + to add your first title." />}
          renderItem={({ item, index }) => (
            <Pressable onPress={() => navigation.navigate('BookDetail', { bookId: item.id })}>
              <GlassCard delay={Math.min(index, 10) * 40} style={styles.row}>
                <View style={[styles.icon, { backgroundColor: colors.goldSoft }]}>
                  <Ionicons name="book-outline" size={18} color={colors.goldDeep} />
                </View>
                <View style={{ flex: 1 }}>
                  <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>{item.title}</Text>
                  <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>{item.author} · {item.category}</Text>
                </View>
                <StatusPill
                  label={`${item.availableCopies}/${item.totalCopies}`}
                  tone={item.availableCopies > 0 ? 'approved' : 'rejected'}
                />
              </GlassCard>
            </Pressable>
          )}
        />
      )}

      <Pressable style={[styles.fab, { backgroundColor: colors.gold, shadowColor: colors.shadow }]} onPress={() => setModalOpen(true)}>
        <Ionicons name="add" size={26} color={colors.inkGreen} />
      </Pressable>

      <Modal visible={modalOpen} transparent animationType="none" onRequestClose={() => setModalOpen(false)}>
        <View style={styles.modalOverlay}>
          <Pressable style={StyleSheet.absoluteFill} onPress={() => setModalOpen(false)} />
          <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
            <Animated.View entering={SlideInDown.duration(320).springify().damping(18)}>
              <GlassCard style={styles.modalCard}>
                <Text style={[type.displayM, { color: colors.textPrimary, marginBottom: 16 }]}>Add a book</Text>
                <AppTextInput label="Title" value={form.title} onChangeText={(v) => setForm((f) => ({ ...f, title: v }))} placeholder="Book title" />
                <AppTextInput label="Author" value={form.author} onChangeText={(v) => setForm((f) => ({ ...f, author: v }))} />
                <View style={styles.twoCol}>
                  <AppTextInput label="Category" value={form.category} onChangeText={(v) => setForm((f) => ({ ...f, category: v }))} style={{ flex: 1 }} />
                  <AppTextInput label="Copies" value={form.totalCopies} onChangeText={(v) => setForm((f) => ({ ...f, totalCopies: v }))} keyboardType="number-pad" style={{ flex: 1 }} />
                </View>
                <AppTextInput label="ISBN (optional)" value={form.isbn} onChangeText={(v) => setForm((f) => ({ ...f, isbn: v }))} />
                <PrimaryButton label="Add book" onPress={handleAdd} loading={saving} />
                <PrimaryButton label="Cancel" variant="ghost" onPress={() => setModalOpen(false)} style={{ marginTop: 4 }} />
              </GlassCard>
            </Animated.View>
          </KeyboardAvoidingView>
        </View>
      </Modal>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  searchWrap: { paddingHorizontal: 20, paddingTop: 16 },
  content: { paddingHorizontal: 20, paddingBottom: 120 },
  row: { flexDirection: 'row', alignItems: 'center', gap: 12, marginBottom: 10 },
  icon: { width: 40, height: 40, borderRadius: 13, alignItems: 'center', justifyContent: 'center' },
  fab: {
    position: 'absolute',
    right: 20,
    bottom: 100,
    width: 56,
    height: 56,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.3,
    shadowRadius: 14,
    elevation: 8,
  },
  modalOverlay: { flex: 1, justifyContent: 'flex-end', backgroundColor: 'rgba(0,0,0,0.4)' },
  modalCard: { margin: 16, marginBottom: 24 },
  twoCol: { flexDirection: 'row', gap: 12 },
});
