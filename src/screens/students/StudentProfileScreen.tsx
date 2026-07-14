import React, { useEffect, useState } from 'react';
import { StyleSheet, View, Text, ScrollView, Alert } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { AppTextInput } from '@/components/AppTextInput';
import { PrimaryButton } from '@/components/PrimaryButton';
import { type, useTheme } from '@/theme';
import { addStudent, updateStudent, deleteStudent } from '@/services/studentService';
import { fetchAttendanceRange, summarizeForStudent, dateKeyDaysAgo, StudentAttendanceStats } from '@/services/attendanceService';
import { ref, get } from 'firebase/database';
import { db } from '@/services/firebase';
import { Student } from '@/types/models';

const STATS_RANGE_DAYS = 30;

/**
 * Doubles as "add student" (studentId param is null) and "edit student"
 * (studentId provided) — mirrors StudentProfileScreen.kt's dual role in
 * the original app.
 */
export function StudentProfileScreen({ route, navigation }: any) {
  const { colors } = useTheme();
  const studentId: string | null = route.params?.studentId ?? null;
  const [loading, setLoading] = useState(!!studentId);
  const [saving, setSaving] = useState(false);
  const [stats, setStats] = useState<StudentAttendanceStats | null>(null);
  const [form, setForm] = useState({
    fullName: '',
    age: '',
    guardianName: '',
    contactNumber: '',
    cnic: '',
    address: '',
    dob: '',
    classId: '',
  });

  useEffect(() => {
    if (!studentId) return;
    (async () => {
      const snap = await get(ref(db, `students/${studentId}`));
      const s = snap.val() as Student | null;
      if (s) {
        setForm({
          fullName: s.fullName,
          age: String(s.age ?? ''),
          guardianName: s.guardianName,
          contactNumber: s.contactNumber,
          cnic: s.cnic,
          address: s.address,
          dob: s.dob,
          classId: s.classId,
        });
      }
      setLoading(false);
    })();
  }, [studentId]);

  useEffect(() => {
    if (!studentId) return;
    (async () => {
      const range = await fetchAttendanceRange(dateKeyDaysAgo(STATS_RANGE_DAYS - 1), dateKeyDaysAgo(0));
      setStats(summarizeForStudent(range, studentId));
    })();
  }, [studentId]);

  const update = (key: keyof typeof form) => (value: string) => setForm((f) => ({ ...f, [key]: value }));

  const handleSave = async () => {
    if (!form.fullName.trim()) {
      Alert.alert('Name required', 'Please enter the student\u2019s full name.');
      return;
    }
    setSaving(true);
    try {
      const payload = {
        fullName: form.fullName.trim(),
        age: parseInt(form.age, 10) || 0,
        guardianName: form.guardianName.trim(),
        contactNumber: form.contactNumber.trim(),
        cnic: form.cnic.trim(),
        address: form.address.trim(),
        dob: form.dob.trim(),
        classId: form.classId.trim(),
      };
      if (studentId) {
        await updateStudent(studentId, payload);
      } else {
        await addStudent({ ...payload, active: true });
      }
      navigation.goBack();
    } catch (e: any) {
      Alert.alert('Could not save', e.message ?? 'Please try again.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = () => {
    if (!studentId) return;
    Alert.alert('Remove student', 'This will permanently delete this student record.', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Delete',
        style: 'destructive',
        onPress: async () => {
          await deleteStudent(studentId);
          navigation.goBack();
        },
      },
    ]);
  };

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow={studentId ? 'Edit record' : 'New enrollment'} title={studentId ? form.fullName || 'Student' : 'Add student'} />
      <ScrollView contentContainerStyle={styles.content}>
        {studentId && stats && stats.markedDays > 0 && (
          <GlassCard style={{ marginBottom: 16 }}>
            <Text style={[type.label, { color: colors.textMuted, marginBottom: 12 }]}>
              ATTENDANCE — LAST {STATS_RANGE_DAYS} DAYS
            </Text>
            <View style={styles.statsRow}>
              <View style={styles.statCol}>
                <Text style={[type.ledgerNumber, { color: colors.statusApproved }]}>{stats.presentDays}</Text>
                <Text style={[type.bodyS, { color: colors.textMuted }]}>Present</Text>
              </View>
              <View style={styles.statCol}>
                <Text style={[type.ledgerNumber, { color: colors.statusRejected }]}>{stats.absentDays}</Text>
                <Text style={[type.bodyS, { color: colors.textMuted }]}>Absent</Text>
              </View>
              <View style={styles.statCol}>
                <Text style={[type.ledgerNumber, { color: colors.statusPending }]}>{stats.leaveDays}</Text>
                <Text style={[type.bodyS, { color: colors.textMuted }]}>Leave</Text>
              </View>
              <View style={styles.statCol}>
                <Text style={[type.ledgerNumber, { color: colors.gold }]}>{stats.percentage}%</Text>
                <Text style={[type.bodyS, { color: colors.textMuted }]}>Rate</Text>
              </View>
            </View>
          </GlassCard>
        )}

        <GlassCard>
          <AppTextInput label="Full name" value={form.fullName} onChangeText={update('fullName')} placeholder="Student's full name" />
          <View style={styles.twoCol}>
            <AppTextInput label="Age" value={form.age} onChangeText={update('age')} keyboardType="number-pad" style={{ flex: 1 }} />
            <AppTextInput label="Class ID" value={form.classId} onChangeText={update('classId')} style={{ flex: 1 }} />
          </View>
          <AppTextInput label="Guardian name" value={form.guardianName} onChangeText={update('guardianName')} />
          <AppTextInput label="Contact number" value={form.contactNumber} onChangeText={update('contactNumber')} keyboardType="phone-pad" />
          <AppTextInput label="CNIC / ID" value={form.cnic} onChangeText={update('cnic')} />
          <AppTextInput label="Date of birth" value={form.dob} onChangeText={update('dob')} placeholder="YYYY-MM-DD" />
          <AppTextInput label="Address" value={form.address} onChangeText={update('address')} multiline style={{ height: 80 }} />

          <PrimaryButton label={studentId ? 'Save changes' : 'Enroll student'} onPress={handleSave} loading={saving} />
          {studentId && (
            <PrimaryButton label="Delete student" variant="ghost" onPress={handleDelete} style={{ marginTop: 4 }} />
          )}
        </GlassCard>
        <View style={{ height: 60 }} />
      </ScrollView>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  content: { padding: 20 },
  twoCol: { flexDirection: 'row', gap: 12 },
  statsRow: { flexDirection: 'row', justifyContent: 'space-between' },
  statCol: { alignItems: 'center', flex: 1 },
});
