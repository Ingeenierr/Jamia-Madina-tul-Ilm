import React, { useEffect, useMemo, useState } from 'react';
import { StyleSheet, View, Text, FlatList, Pressable } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { EmptyState } from '@/components/EmptyState';
import { radius, type, useTheme } from '@/theme';
import { subscribeStudents } from '@/services/studentService';
import { subscribeClasses } from '@/services/classService';
import { subscribeAttendanceForDate, markAttendance } from '@/services/attendanceService';
import { Student, MadrasaClass, AttendanceStatus } from '@/types/models';

function toDateKey(d: Date) {
  return d.toISOString().slice(0, 10);
}
function isToday(dateKey: string) {
  return dateKey === toDateKey(new Date());
}
function friendlyDate(dateKey: string) {
  const d = new Date(dateKey + 'T00:00:00');
  if (isToday(dateKey)) return 'Today';
  return d.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' });
}

/**
 * Shows and marks attendance for a single date (defaults to today, or
 * route.params.date for revisiting history). Because the underlying
 * schema partitions by date at the top level, stepping between days here
 * can never corrupt another day's record — see attendanceService.ts.
 */
export function AttendanceScreen({ route }: any) {
  const { colors } = useTheme();
  const classIdFilter: string | undefined = route?.params?.classId;
  const [dateKey, setDateKey] = useState<string>(route?.params?.date ?? toDateKey(new Date()));
  const [students, setStudents] = useState<Student[]>([]);
  const [classes, setClasses] = useState<MadrasaClass[]>([]);
  const [records, setRecords] = useState<Record<string, Record<string, AttendanceStatus>>>({});

  useEffect(() => subscribeStudents(setStudents), []);
  useEffect(() => subscribeClasses(setClasses), []);
  useEffect(() => subscribeAttendanceForDate(dateKey, setRecords), [dateKey]);

  const roster = useMemo(
    () => (classIdFilter ? students.filter((s) => s.classId === classIdFilter) : students),
    [students, classIdFilter]
  );

  const markedCount = useMemo(() => Object.keys(records).length, [records]);

  const classNameFor = (classId: string) => classes.find((c) => c.id === classId)?.className ?? 'Unassigned';

  const handleMark = (student: Student, status: AttendanceStatus) => {
    const classId = student.classId || 'unassigned';
    markAttendance(dateKey, student.id, classId, status);
  };

  const shiftDate = (deltaDays: number) => {
    const d = new Date(dateKey + 'T00:00:00');
    d.setDate(d.getDate() + deltaDays);
    const next = toDateKey(d);
    if (next > toDateKey(new Date())) return; // no marking future dates
    setDateKey(next);
  };

  const statusOptions: { key: AttendanceStatus; label: string; color: string }[] = [
    { key: 'PRESENT', label: 'P', color: colors.statusApproved },
    { key: 'ABSENT', label: 'A', color: colors.statusRejected },
    { key: 'LEAVE', label: 'L', color: colors.statusPending },
  ];

  return (
    <GradientBackground variant="paper">
      <ScreenHeader
        eyebrow={`${markedCount}/${roster.length} marked`}
        title="Attendance"
        right={
          <View style={styles.dateNav}>
            <Pressable onPress={() => shiftDate(-1)} hitSlop={8} style={styles.dateBtn}>
              <Ionicons name="chevron-back" size={16} color={colors.textOnDark} />
            </Pressable>
            <Text style={[type.bodyS, { color: colors.textOnDark, minWidth: 68, textAlign: 'center' }]}>{friendlyDate(dateKey)}</Text>
            <Pressable onPress={() => shiftDate(1)} hitSlop={8} style={styles.dateBtn} disabled={isToday(dateKey)}>
              <Ionicons name="chevron-forward" size={16} color={isToday(dateKey) ? 'rgba(252,245,229,0.3)' : colors.textOnDark} />
            </Pressable>
          </View>
        }
      />
      <FlatList
        data={roster}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.content}
        ListEmptyComponent={<EmptyState icon="clipboard-outline" title="No students to mark" subtitle="Assign students to a class first." />}
        renderItem={({ item, index }) => {
          const current = records[item.id]?.[item.classId || 'unassigned'];
          return (
            <GlassCard delay={Math.min(index, 10) * 40} style={styles.row}>
              <View style={styles.rowTop}>
                <View style={{ flex: 1 }}>
                  <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>{item.fullName}</Text>
                  <Text style={[type.bodyS, { color: colors.textSecondary }]}>{classNameFor(item.classId)}</Text>
                </View>
              </View>
              <View style={styles.statusRow}>
                {statusOptions.map((opt) => {
                  const active = current === opt.key;
                  return (
                    <Pressable
                      key={opt.key}
                      onPress={() => handleMark(item, opt.key)}
                      style={[
                        styles.statusBtn,
                        { borderColor: opt.color },
                        active && { backgroundColor: opt.color },
                      ]}
                    >
                      <Text style={[type.bodyM, { color: active ? colors.textOnDark : opt.color, fontWeight: '700' }]}>
                        {opt.label}
                      </Text>
                    </Pressable>
                  );
                })}
              </View>
            </GlassCard>
          );
        }}
      />
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: 20, paddingTop: 16, paddingBottom: 120 },
  row: { marginBottom: 10 },
  rowTop: { flexDirection: 'row', marginBottom: 12 },
  statusRow: { flexDirection: 'row', gap: 10 },
  statusBtn: {
    flex: 1,
    height: 38,
    borderRadius: radius.sm,
    borderWidth: 1.5,
    alignItems: 'center',
    justifyContent: 'center',
  },
  dateNav: { flexDirection: 'row', alignItems: 'center', gap: 2 },
  dateBtn: { width: 26, height: 26, alignItems: 'center', justifyContent: 'center' },
});
