import React, { useEffect, useMemo, useState } from 'react';
import { StyleSheet, View, Text, FlatList, Pressable } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import Animated, { FadeInRight } from 'react-native-reanimated';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { AppTextInput } from '@/components/AppTextInput';
import { GlassCard } from '@/components/GlassCard';
import { StatusPill } from '@/components/StatusPill';
import { EmptyState } from '@/components/EmptyState';
import { type, useTheme } from '@/theme';
import { subscribeStudents } from '@/services/studentService';
import { Student } from '@/types/models';

function initials(name: string) {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join('');
}

export function StudentsListScreen({ navigation }: any) {
  const { colors } = useTheme();
  const [students, setStudents] = useState<Student[]>([]);
  const [query, setQuery] = useState('');

  useEffect(() => subscribeStudents(setStudents), []);

  const filtered = useMemo(() => {
    if (!query.trim()) return students;
    const q = query.toLowerCase();
    return students.filter(
      (s) => s.fullName.toLowerCase().includes(q) || s.guardianName.toLowerCase().includes(q)
    );
  }, [students, query]);

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow={`${students.length} enrolled`} title="Students" />

      <View style={styles.searchWrap}>
        <AppTextInput
          label="Search"
          icon="search-outline"
          placeholder="Search by student or guardian"
          value={query}
          onChangeText={setQuery}
        />
      </View>

      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.listContent}
        ListEmptyComponent={
          <EmptyState icon="people-outline" title="No students yet" subtitle="Tap the + button to enroll your first student." />
        }
        renderItem={({ item, index }) => (
          <Animated.View entering={FadeInRight.delay(Math.min(index, 8) * 40).duration(320)}>
            <Pressable onPress={() => navigation.navigate('StudentProfile', { studentId: item.id })}>
              <GlassCard style={styles.row} delay={0}>
                <View style={styles.rowInner}>
                  <View style={[styles.avatar, { backgroundColor: colors.goldSoft }]}>
                    <Text style={[styles.avatarText, { color: colors.goldDeep }]}>{initials(item.fullName)}</Text>
                  </View>
                  <View style={{ flex: 1 }}>
                    <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>{item.fullName}</Text>
                    <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>
                      Guardian: {item.guardianName || '—'}
                    </Text>
                  </View>
                  <StatusPill label={item.active ? 'Active' : 'Inactive'} tone={item.active ? 'approved' : 'neutral'} />
                </View>
              </GlassCard>
            </Pressable>
          </Animated.View>
        )}
      />

      <Pressable style={[styles.fab, { backgroundColor: colors.gold, shadowColor: colors.shadow }]} onPress={() => navigation.navigate('StudentProfile', { studentId: null })}>
        <Ionicons name="add" size={26} color={colors.inkGreen} />
      </Pressable>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  searchWrap: { paddingHorizontal: 20, paddingTop: 16 },
  listContent: { paddingHorizontal: 20, paddingBottom: 120 },
  row: { marginBottom: 10 },
  rowInner: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: { fontWeight: '700' },
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
});
