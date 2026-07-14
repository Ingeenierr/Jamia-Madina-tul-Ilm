import React, { useEffect, useMemo, useState } from 'react';
import { StyleSheet, View, Text, FlatList, Pressable } from 'react-native';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { StatusPill } from '@/components/StatusPill';
import { EmptyState } from '@/components/EmptyState';
import { SkeletonList } from '@/components/SkeletonList';
import { MonthStepper } from '@/components/MonthStepper';
import { useToast } from '@/components/Toast';
import { radius, type, useTheme } from '@/theme';
import { subscribeTeachers } from '@/services/teacherService';
import { subscribePayrollForMonth, ensurePayrollRowsForMonth, markPayrollPaid, markPayrollUnpaid } from '@/services/payrollService';
import { formatCurrency, currentMonthKey } from '@/utils/format';
import { PayrollRecord, Teacher } from '@/types/models';

export function PayrollScreen() {
  const { colors } = useTheme();
  const { show } = useToast();
  const [month, setMonth] = useState(currentMonthKey());
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [records, setRecords] = useState<PayrollRecord[] | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);

  useEffect(() => subscribeTeachers(setTeachers), []);

  useEffect(() => {
    setRecords(null);
    const unsub = subscribePayrollForMonth(month, setRecords);
    return unsub;
  }, [month]);

  // Auto-create a payroll row for any teacher missing one this month, once
  // both the teacher list and existing records have loaded.
  useEffect(() => {
    if (teachers.length === 0 || records === null) return;
    const existingIds = new Set(records.map((r) => r.teacherId));
    const missing = teachers.filter((t) => !existingIds.has(t.id));
    if (missing.length === 0) return;
    ensurePayrollRowsForMonth(month, missing.map((t) => ({ id: t.id, name: t.name, salary: t.salary }))).catch((e) =>
      show(e.message ?? 'Could not initialize payroll for this month.', 'error')
    );
  }, [teachers, records, month]);

  const totals = useMemo(() => {
    const list = records ?? [];
    return {
      paid: list.filter((r) => r.status === 'PAID').reduce((s, r) => s + r.amount, 0),
      unpaid: list.filter((r) => r.status !== 'PAID').reduce((s, r) => s + r.amount, 0),
    };
  }, [records]);

  const toggle = async (record: PayrollRecord) => {
    setBusyId(record.id);
    try {
      if (record.status === 'PAID') {
        await markPayrollUnpaid(record.id);
        show(`Marked ${record.teacherName} unpaid.`, 'info');
      } else {
        await markPayrollPaid(record.id);
        show(`${record.teacherName} marked paid.`, 'success');
      }
    } catch (e: any) {
      show(e.message ?? 'Could not update payroll status.', 'error');
    } finally {
      setBusyId(null);
    }
  };

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow="Teacher salaries" title="Payroll" right={<MonthStepper month={month} onChange={setMonth} />} />

      {records === null ? (
        <SkeletonList rows={5} />
      ) : (
        <FlatList
          data={records}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.content}
          ListHeaderComponent={
            <GlassCard style={{ marginBottom: 16 }}>
              <View style={styles.totalsRow}>
                <View style={styles.totalItem}>
                  <Text style={[type.ledgerNumber, { color: colors.statusApproved }]}>{formatCurrency(totals.paid)}</Text>
                  <Text style={[type.bodyS, { color: colors.textMuted }]}>Paid</Text>
                </View>
                <View style={styles.totalItem}>
                  <Text style={[type.ledgerNumber, { color: colors.statusPending }]}>{formatCurrency(totals.unpaid)}</Text>
                  <Text style={[type.bodyS, { color: colors.textMuted }]}>Pending</Text>
                </View>
              </View>
            </GlassCard>
          }
          ListEmptyComponent={<EmptyState icon="cash-outline" title="No teachers on payroll" subtitle="Add teachers first from the Teachers tab." />}
          renderItem={({ item, index }) => (
            <GlassCard delay={Math.min(index, 10) * 40} style={styles.row}>
              <View style={{ flex: 1 }}>
                <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>{item.teacherName}</Text>
                <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>{formatCurrency(item.amount)}</Text>
              </View>
              <StatusPill label={item.status === 'PAID' ? 'Paid' : 'Unpaid'} tone={item.status === 'PAID' ? 'approved' : 'pending'} />
              <Pressable
                onPress={() => toggle(item)}
                disabled={busyId === item.id}
                style={[
                  styles.toggleBtn,
                  { borderColor: item.status === 'PAID' ? colors.statusRejected : colors.statusApproved },
                ]}
              >
                <Text style={[type.bodyS, { color: item.status === 'PAID' ? colors.statusRejected : colors.statusApproved, fontWeight: '700' }]}>
                  {item.status === 'PAID' ? 'Undo' : 'Pay'}
                </Text>
              </Pressable>
            </GlassCard>
          )}
        />
      )}
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: 20, paddingTop: 16, paddingBottom: 120 },
  totalsRow: { flexDirection: 'row', justifyContent: 'space-around' },
  totalItem: { alignItems: 'center' },
  row: { flexDirection: 'row', alignItems: 'center', gap: 10, marginBottom: 10 },
  toggleBtn: { paddingHorizontal: 12, paddingVertical: 8, borderRadius: radius.sm, borderWidth: 1.5 },
});
