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
import { useToast } from '@/components/Toast';
import { type, useTheme } from '@/theme';
import { subscribeDonations, addDonation, deleteDonation } from '@/services/donationService';
import { formatCurrency, currentMonthKey } from '@/utils/format';
import { Donation } from '@/types/models';

const DONATION_TYPES = ['Sadaqah', 'Zakat', 'General', 'In-kind'];

export function DonationsScreen() {
  const { colors } = useTheme();
  const { show } = useToast();
  const [donations, setDonations] = useState<Donation[] | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ donorName: '', amount: '', type: DONATION_TYPES[0], note: '' });

  useEffect(() => {
    const unsub = subscribeDonations(setDonations);
    return unsub;
  }, []);

  const monthTotal = useMemo(() => {
    if (!donations) return 0;
    const month = currentMonthKey();
    return donations.filter((d) => d.date.startsWith(month)).reduce((sum, d) => sum + d.amount, 0);
  }, [donations]);

  const resetForm = () => setForm({ donorName: '', amount: '', type: DONATION_TYPES[0], note: '' });

  const handleAdd = async () => {
    const amountNum = parseFloat(form.amount);
    if (!form.donorName.trim()) {
      show('Please enter the donor name.', 'error');
      return;
    }
    if (!amountNum || amountNum <= 0) {
      show('Please enter a valid amount.', 'error');
      return;
    }
    setSaving(true);
    try {
      await addDonation({
        donorName: form.donorName.trim(),
        amount: amountNum,
        type: form.type,
        note: form.note.trim() || undefined,
        date: new Date().toISOString().slice(0, 10),
      });
      show('Donation recorded.', 'success');
      resetForm();
      setModalOpen(false);
    } catch (e: any) {
      show(e.message ?? 'Could not save the donation. Check your connection and try again.', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteDonation(id);
      show('Donation removed.', 'success');
    } catch (e: any) {
      show(e.message ?? 'Could not remove this entry.', 'error');
    }
  };

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow={`${formatCurrency(monthTotal)} this month`} title="Donations" />

      {donations === null ? (
        <SkeletonList rows={5} />
      ) : (
        <FlatList
          data={donations}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.content}
          ListEmptyComponent={<EmptyState icon="heart-outline" title="No donations recorded yet" subtitle="Tap + to log the first one." />}
          renderItem={({ item, index }) => (
            <GlassCard delay={Math.min(index, 10) * 40} style={styles.row}>
              <View style={{ flex: 1 }}>
                <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>{item.donorName}</Text>
                <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>
                  {item.type} · {item.date}
                  {item.note ? ` · ${item.note}` : ''}
                </Text>
              </View>
              <Text style={[type.ledgerNumber, { color: colors.statusApproved }]}>{formatCurrency(item.amount)}</Text>
              <Pressable onPress={() => handleDelete(item.id)} hitSlop={10} style={{ marginLeft: 10 }}>
                <Ionicons name="trash-outline" size={18} color={colors.textMuted} />
              </Pressable>
            </GlassCard>
          )}
        />
      )}

      <Pressable style={[styles.fab, { backgroundColor: colors.gold, shadowColor: colors.shadow }]} onPress={() => setModalOpen(true)}>
        <Ionicons name="add" size={26} color={colors.inkGreen} />
      </Pressable>

      <Modal visible={modalOpen} animationType="none" transparent onRequestClose={() => setModalOpen(false)}>
        <View style={styles.modalOverlay}>
          <Pressable style={StyleSheet.absoluteFill} onPress={() => setModalOpen(false)} />
          <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
            <Animated.View entering={SlideInDown.duration(320).springify().damping(18)}>
              <GlassCard style={styles.modalCard}>
                <Text style={[type.displayM, { color: colors.textPrimary, marginBottom: 16 }]}>New donation</Text>
                <AppTextInput label="Donor name" value={form.donorName} onChangeText={(v) => setForm((f) => ({ ...f, donorName: v }))} placeholder="Who's donating?" />
                <AppTextInput label="Amount (Rs)" value={form.amount} onChangeText={(v) => setForm((f) => ({ ...f, amount: v }))} keyboardType="decimal-pad" placeholder="0" />
                <View style={styles.typeRow}>
                  {DONATION_TYPES.map((t) => {
                    const active = form.type === t;
                    return (
                      <Pressable
                        key={t}
                        onPress={() => setForm((f) => ({ ...f, type: t }))}
                        style={[styles.typeChip, { borderColor: active ? colors.gold : colors.hairline }, active && { backgroundColor: colors.goldSoft }]}
                      >
                        <Text style={[type.bodyS, { color: active ? colors.goldDeep : colors.textSecondary }]}>{t}</Text>
                      </Pressable>
                    );
                  })}
                </View>
                <AppTextInput label="Note (optional)" value={form.note} onChangeText={(v) => setForm((f) => ({ ...f, note: v }))} placeholder="Any detail worth remembering" />
                <PrimaryButton label="Save donation" onPress={handleAdd} loading={saving} />
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
  row: { flexDirection: 'row', alignItems: 'center', marginBottom: 10 },
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
  typeRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 16 },
  typeChip: { paddingHorizontal: 12, paddingVertical: 8, borderRadius: 999, borderWidth: 1.5 },
});
