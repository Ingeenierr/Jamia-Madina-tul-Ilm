import React, { useEffect, useState } from 'react';
import { StyleSheet, FlatList, View, Text } from 'react-native';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { EmptyState } from '@/components/EmptyState';
import { type, useTheme } from '@/theme';
import { subscribeClasses } from '@/services/classService';
import { subscribeTeachers } from '@/services/teacherService';
import { MadrasaClass, Teacher } from '@/types/models';

export function ClassesScreen() {
  const { colors } = useTheme();
  const [classes, setClasses] = useState<MadrasaClass[]>([]);
  const [teachers, setTeachers] = useState<Teacher[]>([]);

  useEffect(() => subscribeClasses(setClasses), []);
  useEffect(() => subscribeTeachers(setTeachers), []);

  const teacherName = (id: string) => teachers.find((t) => t.id === id)?.name ?? 'Unassigned';

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow={`${classes.length} classes`} title="Classes" />
      <FlatList
        data={classes}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.content}
        ListEmptyComponent={<EmptyState icon="school-outline" title="No classes yet" />}
        renderItem={({ item, index }) => (
          <GlassCard delay={Math.min(index, 10) * 50} style={styles.card}>
            <View style={styles.headRow}>
              <Text style={[type.displayM, { color: colors.textPrimary }]}>{item.className}</Text>
              <View style={[styles.roomBadge, { backgroundColor: colors.goldSoft }]}>
                <Text style={[type.bodyS, { color: colors.goldDeep, fontWeight: '700' }]}>Room {item.room}</Text>
              </View>
            </View>
            <Text style={[type.bodyM, { color: colors.textSecondary, marginTop: 4 }]}>{item.level}</Text>
            <View style={[styles.divider, { backgroundColor: colors.hairline }]} />
            <View style={styles.metaRow}>
              <Text style={[type.bodyS, { color: colors.textSecondary }]}>Teacher: {teacherName(item.teacherId)}</Text>
              <Text style={[type.bodyS, { color: colors.textSecondary }]}>{item.studentIds.length} students</Text>
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
  roomBadge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 999 },
  divider: { height: 1, marginVertical: 10 },
  metaRow: { flexDirection: 'row', justifyContent: 'space-between' },
});
