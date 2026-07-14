import React, { useEffect, useState } from 'react';
import { StyleSheet, FlatList, View, Text, Pressable } from 'react-native';
import { ref, onValue, off, update } from 'firebase/database';
import { db } from '@/services/firebase';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { StatusPill } from '@/components/StatusPill';
import { EmptyState } from '@/components/EmptyState';
import { radius, type, useTheme } from '@/theme';
import { LeaveRequest } from '@/types/models';

const TONE_FOR: Record<LeaveRequest['status'], 'approved' | 'pending' | 'rejected'> = {
  APPROVED: 'approved',
  PENDING: 'pending',
  REJECTED: 'rejected',
};

export function LeaveRequestsAdminScreen() {
  const { colors } = useTheme();
  const [requests, setRequests] = useState<LeaveRequest[]>([]);

  useEffect(() => {
    const reqRef = ref(db, 'leaveRequests');
    const listener = onValue(reqRef, (snap) => {
      const val = snap.val() as Record<string, LeaveRequest> | null;
      const list = val ? Object.values(val) : [];
      list.sort((a, b) => b.timestamp - a.timestamp);
      setRequests(list);
    });
    return () => off(reqRef, 'value', listener);
  }, []);

  const decide = (req: LeaveRequest, status: 'APPROVED' | 'REJECTED') =>
    update(ref(db, `leaveRequests/${req.id}`), { status });

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow={`${requests.filter((r) => r.status === 'PENDING').length} pending`} title="Leave requests" />
      <FlatList
        data={requests}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.content}
        ListEmptyComponent={<EmptyState icon="calendar-outline" title="No leave requests" />}
        renderItem={({ item, index }) => (
          <GlassCard delay={Math.min(index, 10) * 50} style={styles.card}>
            <View style={styles.headRow}>
              <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>{item.teacherName}</Text>
              <StatusPill label={item.status} tone={TONE_FOR[item.status]} />
            </View>
            <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 4 }]}>{item.date}</Text>
            <Text style={[type.bodyM, { color: colors.textPrimary, marginTop: 8 }]}>{item.reason}</Text>
            {item.status === 'PENDING' && (
              <View style={styles.actionRow}>
                <Pressable style={[styles.actionBtn, { backgroundColor: colors.statusApproved }]} onPress={() => decide(item, 'APPROVED')}>
                  <Text style={[type.bodyM, { color: colors.textOnDark, fontWeight: '700' }]}>Approve</Text>
                </Pressable>
                <Pressable
                  style={[styles.actionBtn, { backgroundColor: 'transparent', borderWidth: 1.5, borderColor: colors.statusRejected }]}
                  onPress={() => decide(item, 'REJECTED')}
                >
                  <Text style={[type.bodyM, { color: colors.statusRejected, fontWeight: '700' }]}>Reject</Text>
                </Pressable>
              </View>
            )}
          </GlassCard>
        )}
      />
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: 20, paddingTop: 16, paddingBottom: 120 },
  card: { marginBottom: 10 },
  headRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  actionRow: { flexDirection: 'row', gap: 10, marginTop: 14 },
  actionBtn: { flex: 1, height: 40, borderRadius: radius.sm, alignItems: 'center', justifyContent: 'center' },
});
