import React, { useEffect, useMemo, useState } from 'react';
import { StyleSheet, View, Text, FlatList, Pressable, Modal, KeyboardAvoidingView, Platform } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import Animated, { SlideInDown } from 'react-native-reanimated';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { AppTextInput } from '@/components/AppTextInput';
import { PrimaryButton } from '@/components/PrimaryButton';
import { EmptyState } from '@/components/EmptyState';
import { SkeletonList } from '@/components/SkeletonList';
import { MonthStepper } from '@/components/MonthStepper';
import { useToast } from '@/components/Toast';
import { radius, type, useTheme } from '@/theme';
import { subscribeCanteenForMonth, addCanteenEntry, deleteCanteenEntry } from '@/services/canteenService';
import { formatCurrency, currentMonthKey } from '@/utils/format';
import { CanteenEntry, CanteenEntryType } from '@/types/models';

const CATEGORIES: Record<CanteenEntryType, string[]> = {
  INCOME: ['Snack sales', 'Meal sales', 'Other'],
  EXPENSE: ['Supplies', 'Utilities', 'Staff', 'Other'],
};

export function CanteenScreen() {
  const { colors } = useTheme();
  const { show } = useToast();
  const [month, setMonth] = useState(currentMonthKey());
  const [entries, setEntries] = useState<CanteenEntry[] | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ type: 'INCOME' as CanteenEntryType, category: CATEGORIES.INCOME[0], amount: '', note: '' });

  useEffect(() => {
    setEntries(null);
    const unsub = subscribeCanteenForMonth(month, setEntries);
    return unsub;
  }, [month]);

  const net = useMemo(() => {
    if (!entries) return { income: 0, expense: 0, net: 0 };
    const income = entries.filter((e) => e.type === 'INCOME').reduce((s, e) => s + e.amount, 0);
    const expense = entries.filter((e) => e.type === 'EXPENSE').reduce((s, e) => s + e.amount, 0);
    return { income, expense, net: income - expense };
  }, [entries]);

  const setType = (t: CanteenEntryType) => setForm((f) => ({ ...f, type: t, category: CATEGORIES[t][0] }));

  const handleAdd = async () => {
    const amountNum = parseFloat(form.amount);
    if (!amountNum || amountNum <= 0) {
      show('Please enter a valid amount.', 'error');
      return;
    }
    setSaving(true);
    try {
      await addCanteenEntry({
        type: form.type,
        category: form.category,
        amount: amountNum,
        note: form.note.trim() || undefined,
        date: new Date().toISOString().slice(0, 10),
      });
      show('Entry added.', 'success');
      setForm({ type: 'INCOME', category: CATEGORIES.INCOME[0], amount: '', note: '' });
      setModalOpen(false);
    } catch (e: any) {
      show(e.message ?? 'Could not save this entry.', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteCanteenEntry(id);
      show('Entry removed.', 'success');
    } catch (e: any) {
      show(e.message ?? 'Could not remove this entry.', 'error');
    }
  };

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow="Income & expenses" title="Canteen ledger" right={<MonthStepper month={month} onChange={setMonth} />} />

      {entries === null ? (
        <SkeletonList rows={5} />
      ) : (
        <FlatList
          data={entries}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.content}
          ListHeaderComponent={
            <GlassCard style={{ marginBottom: 16 }}>
              <View style={styles.totalsRow}>
                <View style={styles.totalItem}>
                  <Text style={[type.ledgerNumber, { color: colors.statusApproved }]}>{formatCurrency(net.income)}</Text>
                  <Text style={[type.bodyS, { color: colors.textMuted }]}>Income</Text>
                </View>
                <View style={styles.totalItem}>
                  <Text style={[type.ledgerNumber, { color: colors.statusRejected }]}>{formatCurrency(net.expense)}</Text>
                  <Text style={[type.bodyS, { color: colors.textMuted }]}>Expense</Text>
                </View>
                <View style={styles.totalItem}>
                  <Text style={[type.ledgerNumber, { color: net.net >= 0 ? colors.statusApproved : colors.statusRejected }]}>
                    {formatCurrency(net.net)}
                  </Text>
                  <Text style={[type.bodyS, { color: colors.textMuted }]}>Net</Text>
                </View>
              </View>
            </GlassCard>
          }
          ListEmptyComponent={<EmptyState icon="restaurant-outline" title="No entries this month" subtitle="Tap + to log income or an expense." />}
          renderItem={({ item, index }) => (
            <GlassCard delay={Math.min(index, 10) * 40} style={styles.row}>
              <View
                style={[
                  styles.typeIcon,
                  { backgroundColor: item.type === 'INCOME' ? colors.statusApproved + '1F' : colors.statusRejected + '1F' },
                ]}
              >
                <Ionicons
                  name={item.type === 'INCOME' ? 'arrow-down-circle-outline' : 'arrow-up-circle-outline'}
                  size={18}
                  color={item.type === 'INCOME' ? colors.statusApproved : colors.statusRejected}
                />
              </View>
              <View style={{ flex: 1 }}>
                <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>{item.category}</Text>
                <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>
                  {item.date}
                  {item.note ? ` · ${item.note}` : ''}
                </Text>
              </View>
              <Text style={[type.ledgerNumber, { color: item.type === 'INCOME' ? colors.statusApproved : colors.statusRejected }]}>
                {item.type === 'EXPENSE' ? '-' : ''}
                {formatCurrency(item.amount)}
              </Text>
              <Pressable onPress={() => handleDelete(item.id)} hitSlop={10} style={{ marginLeft: 8 }}>
                <Ionicons name="trash-outline" size={16} color={colors.textMuted} />
              </Pressable>
            </GlassCard>
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
                <Text style={[type.displayM, { color: colors.textPrimary, marginBottom: 16 }]}>New ledger entry</Text>

                <View style={styles.typeToggle}>
                  {(['INCOME', 'EXPENSE'] as CanteenEntryType[]).map((t) => {
                    const active = form.type === t;
                    return (
                      <Pressable
                        key={t}
                        onPress={() => setType(t)}
                        style={[
                          styles.typeToggleChip,
                          { borderColor: active ? colors.gold : colors.hairline },
                          active && { backgroundColor: colors.goldSoft },
                        ]}
                      >
                        <Text style={[type.bodyM, { color: active ? colors.goldDeep : colors.textSecondary, fontWeight: active ? '700' : '400' }]}>
                          {t === 'INCOME' ? 'Income' : 'Expense'}
                        </Text>
                      </Pressable>
                    );
                  })}
                </View>

                <View style={styles.typeRow}>
                  {CATEGORIES[form.type].map((c) => {
                    const active = form.category === c;
                    return (
                      <Pressable
                        key={c}
                        onPress={() => setForm((f) => ({ ...f, category: c }))}
                        style={[styles.typeChip, { borderColor: active ? colors.gold : colors.hairline }, active && { backgroundColor: colors.goldSoft }]}
                      >
                        <Text style={[type.bodyS, { color: active ? colors.goldDeep : colors.textSecondary }]}>{c}</Text>
                      </Pressable>
                    );
                  })}
                </View>

                <AppTextInput label="Amount (Rs)" value={form.amount} onChangeText={(v) => setForm((f) => ({ ...f, amount: v }))} keyboardType="decimal-pad" />
                <AppTextInput label="Note (optional)" value={form.note} onChangeText={(v) => setForm((f) => ({ ...f, note: v }))} />
                <PrimaryButton label="Save entry" onPress={handleAdd} loading={saving} />
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
  content: { paddingHorizontal: 20, paddingTop: 16, paddingBottom: 120 },
  totalsRow: { flexDirection: 'row', justifyContent: 'space-around' },
  totalItem: { alignItems: 'center' },
  row: { flexDirection: 'row', alignItems: 'center', gap: 10, marginBottom: 10 },
  typeIcon: { width: 34, height: 34, borderRadius: 11, alignItems: 'center', justifyContent: 'center' },
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
  typeToggle: { flexDirection: 'row', gap: 10, marginBottom: 14 },
  typeToggleChip: { flex: 1, paddingVertical: 12, borderRadius: radius.md, borderWidth: 1.5, alignItems: 'center' },
  typeRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 16 },
  typeChip: { paddingHorizontal: 12, paddingVertical: 8, borderRadius: 999, borderWidth: 1.5 },
});
