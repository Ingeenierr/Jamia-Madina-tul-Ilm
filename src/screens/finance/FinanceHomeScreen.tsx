import React, { useEffect, useState } from 'react';
import { StyleSheet, ScrollView, View, Text, Pressable } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { ref, get, query, orderByChild, equalTo, startAt, endAt } from 'firebase/database';
import { db } from '@/services/firebase';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { SkeletonList } from '@/components/SkeletonList';
import { type, useTheme } from '@/theme';
import { useToast } from '@/components/Toast';
import { currentMonthKey, formatCurrency, friendlyMonth } from '@/utils/format';
import { Donation, PayrollRecord, FeePayment, CanteenEntry } from '@/types/models';

interface Summary {
  donationsTotal: number;
  payrollPaid: number;
  payrollUnpaid: number;
  feesCollected: number;
  feesOutstanding: number;
  canteenNet: number;
}

const MODULES: { key: string; label: string; icon: keyof typeof Ionicons.glyphMap; route: string; accent: 'palm' | 'gold' | 'rejected' | 'pending' }[] = [
  { key: 'donations', label: 'Donations', icon: 'heart-outline', route: 'Donations', accent: 'rejected' },
  { key: 'payroll', label: 'Teacher payroll', icon: 'cash-outline', route: 'Payroll', accent: 'gold' },
  { key: 'fees', label: 'Student fees', icon: 'wallet-outline', route: 'Fees', accent: 'palm' },
  { key: 'canteen', label: 'Canteen ledger', icon: 'restaurant-outline', route: 'Canteen', accent: 'pending' },
];

export function FinanceHomeScreen({ navigation }: any) {
  const { colors } = useTheme();
  const { show } = useToast();
  const [summary, setSummary] = useState<Summary | null>(null);
  const [loading, setLoading] = useState(true);
  const month = currentMonthKey();

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const [donationsSnap, payrollSnap, feesSnap, canteenSnap] = await Promise.all([
          get(ref(db, 'donations')),
          get(query(ref(db, 'payroll'), orderByChild('month'), equalTo(month))),
          get(query(ref(db, 'fees'), orderByChild('month'), equalTo(month))),
          get(query(ref(db, 'canteenLedger'), orderByChild('date'), startAt(`${month}-01`), endAt(`${month}-31`))),
        ]);

        const donations = Object.values((donationsSnap.val() as Record<string, Donation> | null) || {});
        const payroll = Object.values((payrollSnap.val() as Record<string, PayrollRecord> | null) || {});
        const fees = Object.values((feesSnap.val() as Record<string, FeePayment> | null) || {});
        const canteen = Object.values((canteenSnap.val() as Record<string, CanteenEntry> | null) || {});

        if (cancelled) return;
        setSummary({
          donationsTotal: donations
            .filter((d) => d.date.startsWith(month))
            .reduce((sum, d) => sum + d.amount, 0),
          payrollPaid: payroll.filter((p) => p.status === 'PAID').reduce((sum, p) => sum + p.amount, 0),
          payrollUnpaid: payroll.filter((p) => p.status !== 'PAID').reduce((sum, p) => sum + p.amount, 0),
          feesCollected: fees.reduce((sum, f) => sum + f.amountPaid, 0),
          feesOutstanding: fees.reduce((sum, f) => sum + Math.max(0, f.amountDue - f.amountPaid), 0),
          canteenNet: canteen.reduce((sum, c) => sum + (c.type === 'INCOME' ? c.amount : -c.amount), 0),
        });
      } catch (e: any) {
        if (!cancelled) show(e.message ?? 'Could not load finance summary.', 'error');
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [month]);

  const accentColor = (a: 'palm' | 'gold' | 'rejected' | 'pending') =>
    a === 'palm' ? colors.palm : a === 'gold' ? colors.goldDeep : a === 'rejected' ? colors.statusRejected : colors.statusPending;

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow={friendlyMonth(month)} title="Finance" />
      {loading ? (
        <SkeletonList rows={3} />
      ) : (
        <ScrollView contentContainerStyle={styles.content}>
          {summary && (
            <GlassCard glow style={{ marginBottom: 20 }}>
              <Text style={[type.label, { color: colors.textMuted, marginBottom: 14 }]}>THIS MONTH AT A GLANCE</Text>
              <View style={styles.grid}>
                <SummaryStat label="Donations" value={summary.donationsTotal} color={colors.statusRejected} />
                <SummaryStat label="Fees collected" value={summary.feesCollected} color={colors.palm} />
                <SummaryStat label="Fees outstanding" value={summary.feesOutstanding} color={colors.statusPending} />
                <SummaryStat label="Payroll paid" value={summary.payrollPaid} color={colors.goldDeep} />
                <SummaryStat label="Payroll pending" value={summary.payrollUnpaid} color={colors.statusPending} />
                <SummaryStat label="Canteen net" value={summary.canteenNet} color={summary.canteenNet >= 0 ? colors.statusApproved : colors.statusRejected} />
              </View>
            </GlassCard>
          )}

          <Text style={[type.label, { color: colors.textMuted, marginBottom: 12 }]}>MODULES</Text>
          <GlassCard style={{ padding: 0 }}>
            {MODULES.map((m, i) => (
              <Pressable
                key={m.key}
                onPress={() => navigation.navigate(m.route)}
                style={[styles.row, i < MODULES.length - 1 && { borderBottomWidth: 1, borderBottomColor: colors.hairline }]}
              >
                <View style={[styles.icon, { backgroundColor: accentColor(m.accent) + '1F' }]}>
                  <Ionicons name={m.icon} size={18} color={accentColor(m.accent)} />
                </View>
                <Text style={[type.bodyL, { flex: 1, color: colors.textPrimary }]}>{m.label}</Text>
                <Ionicons name="chevron-forward" size={18} color={colors.textMuted} />
              </Pressable>
            ))}
          </GlassCard>
          <View style={{ height: 100 }} />
        </ScrollView>
      )}
    </GradientBackground>
  );
}

function SummaryStat({ label, value, color }: { label: string; value: number; color: string }) {
  const { colors } = useTheme();
  return (
    <View style={styles.statItem}>
      <Text style={[type.ledgerNumber, { color }]}>{formatCurrency(value)}</Text>
      <Text style={[type.bodyS, { color: colors.textMuted, marginTop: 2 }]}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  content: { padding: 20 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 16 },
  statItem: { width: '45%' },
  row: { flexDirection: 'row', alignItems: 'center', gap: 12, padding: 16 },
  icon: { width: 36, height: 36, borderRadius: 11, alignItems: 'center', justifyContent: 'center' },
});
