import React, { useEffect, useMemo, useState } from 'react';
import { StyleSheet, View, Text, FlatList, Pressable, Modal, KeyboardAvoidingView, Platform } from 'react-native';
import Animated, { SlideInDown } from 'react-native-reanimated';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { StatusPill } from '@/components/StatusPill';
import { AppTextInput } from '@/components/AppTextInput';
import { PrimaryButton } from '@/components/PrimaryButton';
import { EmptyState } from '@/components/EmptyState';
import { SkeletonList } from '@/components/SkeletonList';
import { MonthStepper } from '@/components/MonthStepper';
import { useToast } from '@/components/Toast';
import { type, useTheme } from '@/theme';
import { subscribeStudents } from '@/services/studentService';
import { subscribeFeesForMonth, ensureFeeRowsForMonth, recordFeePayment } from '@/services/feeService';
import { formatCurrency, currentMonthKey } from '@/utils/format';
import { FeePayment, Student } from '@/types/models';

const DEFAULT_MONTHLY_FEE = 1500;

const TONE_FOR: Record<FeePayment['status'], 'approved' | 'pending' | 'rejected'> = {
  PAID: 'approved',
  PARTIAL: 'pending',
  UNPAID: 'rejected',
};

export function FeesScreen() {
  const { colors } = useTheme();
  const { show } = useToast();
  const [month, setMonth] = useState(currentMonthKey());
  const [students, setStudents] = useState<Student[]>([]);
  const [fees, setFees] = useState<FeePayment[] | null>(null);
  const [editing, setEditing] = useState<FeePayment | null>(null);
  const [amountInput, setAmountInput] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => subscribeStudents(setStudents), []);

  useEffect(() => {
    setFees(null);
    const unsub = subscribeFeesForMonth(month, setFees);
    return unsub;
  }, [month]);

  useEffect(() => {
    const activeStudents = students.filter((s) => s.active);
    if (activeStudents.length === 0 || fees === null) return;
    const existingIds = new Set(fees.map((f) => f.studentId));
    const missing = activeStudents.filter((s) => !existingIds.has(s.id));
    if (missing.length === 0) return;
    ensureFeeRowsForMonth(month, missing.map((s) => ({ id: s.id, fullName: s.fullName })), DEFAULT_MONTHLY_FEE).catch((e) =>
      show(e.message ?? 'Could not initialize fee records for this month.', 'error')
    );
  }, [students, fees, month]);

  const totals = useMemo(() => {
    const list = fees ?? [];
    return {
      collected: list.reduce((s, f) => s + f.amountPaid, 0),
      outstanding: list.reduce((s, f) => s + Math.max(0, f.amountDue - f.amountPaid), 0),
    };
  }, [fees]);

  const openEditor = (fee: FeePayment) => {
    setEditing(fee);
    setAmountInput(String(fee.amountPaid || ''));
  };

  const handleSavePayment = async () => {
    if (!editing) return;
    const amountNum = parseFloat(amountInput);
    if (isNaN(amountNum) || amountNum < 0) {
      show('Please enter a valid amount.', 'error');
      return;
    }
    setSaving(true);
    try {
      await recordFeePayment(editing.id, amountNum, editing.amountDue);
      show(`Payment recorded for ${editing.studentName}.`, 'success');
      setEditing(null);
    } catch (e: any) {
      show(e.message ?? 'Could not record this payment.', 'error');
    } finally {
      setSaving(false);
    }
  };

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow="Monthly tuition" title="Student fees" right={<MonthStepper month={month} onChange={setMonth} />} />

      {fees === null ? (
        <SkeletonList rows={5} />
      ) : (
        <FlatList
          data={fees}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.content}
          ListHeaderComponent={
            <GlassCard style={{ marginBottom: 16 }}>
              <View style={styles.totalsRow}>
                <View style={styles.totalItem}>
                  <Text style={[type.ledgerNumber, { color: colors.statusApproved }]}>{formatCurrency(totals.collected)}</Text>
                  <Text style={[type.bodyS, { color: colors.textMuted }]}>Collected</Text>
                </View>
                <View style={styles.totalItem}>
                  <Text style={[type.ledgerNumber, { color: colors.statusRejected }]}>{formatCurrency(totals.outstanding)}</Text>
                  <Text style={[type.bodyS, { color: colors.textMuted }]}>Outstanding</Text>
                </View>
              </View>
            </GlassCard>
          }
          ListEmptyComponent={<EmptyState icon="wallet-outline" title="No active students" subtitle="Enroll students to start tracking fees." />}
          renderItem={({ item, index }) => (
            <Pressable onPress={() => openEditor(item)}>
              <GlassCard delay={Math.min(index, 10) * 40} style={styles.row}>
                <View style={{ flex: 1 }}>
                  <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>{item.studentName}</Text>
                  <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>
                    {formatCurrency(item.amountPaid)} of {formatCurrency(item.amountDue)}
                  </Text>
                </View>
                <StatusPill label={item.status} tone={TONE_FOR[item.status]} />
              </GlassCard>
            </Pressable>
          )}
        />
      )}

      <Modal visible={!!editing} transparent animationType="none" onRequestClose={() => setEditing(null)}>
        <View style={styles.modalOverlay}>
          <Pressable style={StyleSheet.absoluteFill} onPress={() => setEditing(null)} />
          <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
            <Animated.View entering={SlideInDown.duration(320).springify().damping(18)}>
              <GlassCard style={styles.modalCard}>
                <Text style={[type.displayM, { color: colors.textPrimary, marginBottom: 4 }]}>{editing?.studentName}</Text>
                <Text style={[type.bodyS, { color: colors.textMuted, marginBottom: 16 }]}>
                  Due: {editing ? formatCurrency(editing.amountDue) : ''}
                </Text>
                <AppTextInput label="Amount paid (Rs)" value={amountInput} onChangeText={setAmountInput} keyboardType="decimal-pad" />
                <PrimaryButton label="Save payment" onPress={handleSavePayment} loading={saving} />
                <PrimaryButton label="Cancel" variant="ghost" onPress={() => setEditing(null)} style={{ marginTop: 4 }} />
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
  row: { flexDirection: 'row', alignItems: 'center', marginBottom: 10 },
  modalOverlay: { flex: 1, justifyContent: 'flex-end', backgroundColor: 'rgba(0,0,0,0.4)' },
  modalCard: { margin: 16, marginBottom: 24 },
});
