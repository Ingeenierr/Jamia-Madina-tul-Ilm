import React, { useEffect, useState } from 'react';
import { StyleSheet, FlatList, View, Text, Pressable, Alert } from 'react-native';
import { ref, onValue, off, update } from 'firebase/database';
import { db } from '@/services/firebase';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { StatusPill } from '@/components/StatusPill';
import { EmptyState } from '@/components/EmptyState';
import { radius, type, useTheme } from '@/theme';
import { AppUser } from '@/types/models';

export function ApprovalScreen() {
  const { colors } = useTheme();
  const [pending, setPending] = useState<AppUser[]>([]);

  useEffect(() => {
    const usersRef = ref(db, 'users');
    const listener = onValue(usersRef, (snap) => {
      const val = snap.val() as Record<string, AppUser> | null;
      const list = val ? Object.values(val) : [];
      setPending(list.filter((u) => u.status === 'PENDING'));
    });
    return () => off(usersRef, 'value', listener);
  }, []);

  const decide = async (u: AppUser, status: 'APPROVED' | 'DISAPPROVED') => {
    try {
      await update(ref(db, `users/${u.id}`), { status });
    } catch (e: any) {
      Alert.alert('Could not update', e.message ?? 'Please try again.');
    }
  };

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow={`${pending.length} awaiting review`} title="Approvals" />
      <FlatList
        data={pending}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.content}
        ListEmptyComponent={<EmptyState icon="checkmark-done-outline" title="All caught up" subtitle="No pending teacher accounts to review." />}
        renderItem={({ item, index }) => (
          <GlassCard delay={Math.min(index, 10) * 50} style={styles.card}>
            <View style={styles.headRow}>
              <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>{item.name}</Text>
              <StatusPill label="Pending" tone="pending" />
            </View>
            <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>{item.email}</Text>
            <View style={styles.actionRow}>
              <Pressable style={[styles.actionBtn, { backgroundColor: colors.statusApproved }]} onPress={() => decide(item, 'APPROVED')}>
                <Text style={[type.bodyM, { color: colors.textOnDark, fontWeight: '700' }]}>Approve</Text>
              </Pressable>
              <Pressable
                style={[styles.actionBtn, { backgroundColor: 'transparent', borderWidth: 1.5, borderColor: colors.statusRejected }]}
                onPress={() => decide(item, 'DISAPPROVED')}
              >
                <Text style={[type.bodyM, { color: colors.statusRejected, fontWeight: '700' }]}>Reject</Text>
              </Pressable>
            </View>
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
