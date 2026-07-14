import React, { useEffect, useState } from 'react';
import { StyleSheet, View, Text, ScrollView, Pressable } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { StatCard } from '@/components/StatCard';
import { type, useTheme } from '@/theme';
import { useAuth } from '@/context/AuthContext';
import { subscribeClasses } from '@/services/classService';
import { MadrasaClass } from '@/types/models';

export function TeacherDashboardScreen({ navigation }: any) {
  const { colors } = useTheme();
  const { user, logout } = useAuth();
  const [myClasses, setMyClasses] = useState<MadrasaClass[]>([]);

  useEffect(() => {
    const unsub = subscribeClasses((all) => {
      setMyClasses(all.filter((c) => c.teacherId === user?.id));
    });
    return unsub;
  }, [user?.id]);

  const totalStudents = myClasses.reduce((sum, c) => sum + c.studentIds.length, 0);

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow="Teacher panel" title={`Assalamu alaikum, ${user?.name || ''}`} onLogout={logout} />
      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        <View style={styles.statGrid}>
          <StatCard label="My classes" value={myClasses.length} icon="school" accent={colors.palm} />
          <StatCard label="My students" value={totalStudents} icon="people" delay={90} accent={colors.goldDeep} />
        </View>

        <Text style={[type.label, { color: colors.textMuted, marginTop: 24, marginBottom: 12 }]}>TODAY</Text>
        <GlassCard delay={180} style={{ padding: 0 }}>
          <Pressable style={styles.row} onPress={() => navigation.navigate('Attendance')}>
            <Ionicons name="clipboard-outline" size={18} color={colors.palm} style={{ marginRight: 12 }} />
            <Text style={[type.bodyL, { flex: 1, color: colors.textPrimary }]}>Take attendance</Text>
            <Ionicons name="chevron-forward" size={18} color={colors.textMuted} />
          </Pressable>
          <View style={[styles.divider, { backgroundColor: colors.hairline }]} />
          <Pressable style={styles.row} onPress={() => navigation.navigate('LeaveRequest')}>
            <Ionicons name="calendar-outline" size={18} color={colors.statusPending} style={{ marginRight: 12 }} />
            <Text style={[type.bodyL, { flex: 1, color: colors.textPrimary }]}>Request leave</Text>
            <Ionicons name="chevron-forward" size={18} color={colors.textMuted} />
          </Pressable>
        </GlassCard>

        <Text style={[type.label, { color: colors.textMuted, marginTop: 24, marginBottom: 12 }]}>MY CLASSES</Text>
        {myClasses.map((c, i) => (
          <GlassCard key={c.id} delay={220 + i * 60} style={{ marginBottom: 10 }}>
            <Text style={[type.displayM, { color: colors.textPrimary }]}>{c.className}</Text>
            <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>
              {c.level} · Room {c.room} · {c.studentIds.length} students
            </Text>
          </GlassCard>
        ))}

        <View style={{ height: 100 }} />
      </ScrollView>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: 20, paddingTop: 20 },
  statGrid: { flexDirection: 'row', gap: 12 },
  row: { flexDirection: 'row', alignItems: 'center', padding: 16 },
  divider: { height: 1 },
});
