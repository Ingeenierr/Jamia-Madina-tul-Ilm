import React, { useEffect, useState } from 'react';
import { StyleSheet, View, Text, ScrollView, Pressable } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { StatCard } from '@/components/StatCard';
import { GlassCard } from '@/components/GlassCard';
import { WeeklyAttendanceChart } from '@/components/WeeklyAttendanceChart';
import { type, useTheme } from '@/theme';
import { useAuth } from '@/context/AuthContext';
import { subscribeStudents } from '@/services/studentService';
import { subscribeTeachers } from '@/services/teacherService';
import { subscribeClasses } from '@/services/classService';
import {
  subscribeAttendanceForDate,
  fetchAttendanceRange,
  summarizeByDay,
  dateKeyDaysAgo,
  DayAttendanceSummary,
} from '@/services/attendanceService';
import { Student, Teacher, MadrasaClass } from '@/types/models';

const TODAY = dateKeyDaysAgo(0);

export function AdminDashboardScreen({ navigation }: any) {
  const { colors } = useTheme();
  const { user, logout } = useAuth();
  const [students, setStudents] = useState<Student[]>([]);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [classes, setClasses] = useState<MadrasaClass[]>([]);
  const [presentToday, setPresentToday] = useState(0);
  const [weekly, setWeekly] = useState<DayAttendanceSummary[]>([]);

  useEffect(() => {
    const u1 = subscribeStudents(setStudents);
    const u2 = subscribeTeachers(setTeachers);
    const u3 = subscribeClasses(setClasses);
    const u4 = subscribeAttendanceForDate(TODAY, (recs) => {
      const count = Object.values(recs).filter((byClass) => Object.values(byClass).some((s) => s === 'PRESENT')).length;
      setPresentToday(count);
    });
    return () => {
      u1();
      u2();
      u3();
      u4();
    };
  }, []);

  useEffect(() => {
    (async () => {
      const range = await fetchAttendanceRange(dateKeyDaysAgo(6), dateKeyDaysAgo(0));
      setWeekly(summarizeByDay(range));
    })();
  }, [presentToday]);

  const quickActions: { label: string; icon: keyof typeof Ionicons.glyphMap; route: string; accent: string }[] = [
    { label: 'Students', icon: 'people-outline', route: 'Students', accent: colors.palm },
    { label: 'Teachers', icon: 'person-outline', route: 'Teachers', accent: colors.goldDeep },
    { label: 'Approvals', icon: 'checkmark-done-outline', route: 'Approvals', accent: colors.statusApproved },
    { label: 'Leave requests', icon: 'calendar-outline', route: 'Leaves', accent: colors.statusPending },
    { label: 'Classes', icon: 'school-outline', route: 'Classes', accent: colors.inkGreenLight },
    { label: 'Attendance history', icon: 'bar-chart-outline', route: 'AttendanceHistory', accent: colors.gold },
    { label: 'Finance', icon: 'wallet-outline', route: 'Finance', accent: colors.statusRejected },
  ];

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow="Admin panel" title={`Welcome, ${user?.name || 'Administrator'}`} onLogout={logout} />

      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        <View style={styles.statGrid}>
          <StatCard label="Students" value={students.length} icon="people" delay={0} accent={colors.palm} onPress={() => navigation.navigate('Students')} />
          <StatCard label="Teachers" value={teachers.length} icon="person" delay={90} accent={colors.goldDeep} onPress={() => navigation.navigate('Teachers')} />
        </View>
        <View style={styles.statGrid}>
          <StatCard label="Classes" value={classes.length} icon="school" delay={180} accent={colors.inkGreenLight} onPress={() => navigation.navigate('Classes')} />
          <StatCard label="Present today" value={presentToday} icon="checkmark-circle" delay={270} accent={colors.statusApproved} onPress={() => navigation.navigate('Attendance')} />
        </View>

        {weekly.length > 0 && (
          <Pressable onPress={() => navigation.navigate('AttendanceHistory')}>
            <GlassCard delay={340} style={{ marginTop: 12 }}>
              <View style={styles.chartHead}>
                <Text style={[type.label, { color: colors.textMuted }]}>PRESENT RATE — LAST 7 DAYS</Text>
                <Ionicons name="chevron-forward" size={16} color={colors.textMuted} />
              </View>
              <WeeklyAttendanceChart days={weekly} />
            </GlassCard>
          </Pressable>
        )}

        <Text style={[type.label, { color: colors.textMuted, marginTop: 28, marginBottom: 12 }]}>QUICK ACTIONS</Text>
        <GlassCard delay={400} style={{ padding: 0 }}>
          {quickActions.map((action, i) => (
            <Pressable
              key={action.label}
              onPress={() => navigation.navigate(action.route)}
              style={[styles.actionRow, i < quickActions.length - 1 && { borderBottomWidth: 1, borderBottomColor: colors.hairline }]}
            >
              <View style={[styles.actionIcon, { backgroundColor: action.accent + '1F' }]}>
                <Ionicons name={action.icon} size={18} color={action.accent} />
              </View>
              <Text style={[type.bodyL, { color: colors.textPrimary, flex: 1 }]}>{action.label}</Text>
              <Ionicons name="chevron-forward" size={18} color={colors.textMuted} />
            </Pressable>
          ))}
        </GlassCard>

        <View style={{ height: 100 }} />
      </ScrollView>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: 20, paddingTop: 20 },
  statGrid: { flexDirection: 'row', gap: 12, marginBottom: 12 },
  chartHead: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  actionRow: { flexDirection: 'row', alignItems: 'center', gap: 12, padding: 16 },
  actionIcon: { width: 36, height: 36, borderRadius: 11, alignItems: 'center', justifyContent: 'center' },
});
