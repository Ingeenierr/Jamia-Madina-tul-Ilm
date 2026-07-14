import React, { useEffect, useState } from 'react';
import { StyleSheet, ScrollView, View, Text, Pressable } from 'react-native';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { WeeklyAttendanceChart } from '@/components/WeeklyAttendanceChart';
import { EmptyState } from '@/components/EmptyState';
import { type, useTheme } from '@/theme';
import {
  fetchAttendanceRange,
  summarizeByDay,
  dateKeyDaysAgo,
  DayAttendanceSummary,
} from '@/services/attendanceService';

const RANGE_DAYS = 14;

export function AttendanceHistoryScreen({ navigation }: any) {
  const { colors } = useTheme();
  const [days, setDays] = useState<DayAttendanceSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      const start = dateKeyDaysAgo(RANGE_DAYS - 1);
      const end = dateKeyDaysAgo(0);
      const range = await fetchAttendanceRange(start, end);
      setDays(summarizeByDay(range));
      setLoading(false);
    })();
  }, []);

  const last7 = days.slice(-7);

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow="Last 14 days" title="Attendance history" />
      <ScrollView contentContainerStyle={styles.content}>
        {!loading && last7.length > 0 && (
          <GlassCard style={{ marginBottom: 16, alignItems: 'center' }}>
            <Text style={[type.label, { color: colors.textMuted, alignSelf: 'flex-start', marginBottom: 12 }]}>
              PRESENT RATE — LAST 7 DAYS
            </Text>
            <WeeklyAttendanceChart days={last7} />
          </GlassCard>
        )}

        {!loading && days.length === 0 && (
          <EmptyState icon="bar-chart-outline" title="No attendance recorded yet" subtitle="Mark today's attendance to start building history." />
        )}

        {[...days].reverse().map((d, i) => (
          <Pressable key={d.date} onPress={() => navigation.navigate('Attendance', { date: d.date })}>
            <GlassCard delay={Math.min(i, 10) * 40} style={styles.dayRow}>
              <View style={{ flex: 1 }}>
                <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>
                  {new Date(d.date + 'T00:00:00').toLocaleDateString(undefined, { weekday: 'long', month: 'short', day: 'numeric' })}
                </Text>
                <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>
                  {d.total} student{d.total === 1 ? '' : 's'} marked
                </Text>
              </View>
              <View style={styles.countGroup}>
                <Text style={[type.ledgerNumber, { color: colors.statusApproved }]}>{d.present}</Text>
                <Text style={[type.bodyS, { color: colors.textMuted }]}>present</Text>
              </View>
            </GlassCard>
          </Pressable>
        ))}
        <View style={{ height: 40 }} />
      </ScrollView>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  content: { padding: 20, paddingBottom: 100 },
  dayRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 10 },
  countGroup: { alignItems: 'center', minWidth: 56 },
});
