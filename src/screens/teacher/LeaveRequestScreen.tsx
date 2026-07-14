import React, { useState } from 'react';
import { StyleSheet, ScrollView, Alert } from 'react-native';
import { push, ref, set } from 'firebase/database';
import { db } from '@/services/firebase';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { AppTextInput } from '@/components/AppTextInput';
import { PrimaryButton } from '@/components/PrimaryButton';
import { useAuth } from '@/context/AuthContext';
import { LeaveRequest } from '@/types/models';

export function LeaveRequestScreen({ navigation }: any) {
  const { user } = useAuth();
  const [reason, setReason] = useState('');
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [saving, setSaving] = useState(false);

  const handleSubmit = async () => {
    if (!reason.trim() || !user) return;
    setSaving(true);
    try {
      const newRef = push(ref(db, 'leaveRequests'));
      const record: LeaveRequest = {
        id: newRef.key as string,
        teacherId: user.id,
        teacherName: user.name,
        reason: reason.trim(),
        date,
        timestamp: Date.now(),
        status: 'PENDING',
      };
      await set(newRef, record);
      navigation.goBack();
    } catch (e: any) {
      Alert.alert('Could not submit', e.message ?? 'Please try again.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow="Time off" title="Request leave" />
      <ScrollView contentContainerStyle={styles.content}>
        <GlassCard>
          <AppTextInput label="Date" value={date} onChangeText={setDate} placeholder="YYYY-MM-DD" />
          <AppTextInput label="Reason" value={reason} onChangeText={setReason} multiline style={{ height: 100 }} placeholder="Reason for leave" />
          <PrimaryButton label="Submit request" onPress={handleSubmit} loading={saving} disabled={!reason.trim()} />
        </GlassCard>
      </ScrollView>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({ content: { padding: 20 } });
