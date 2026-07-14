import React, { useEffect, useMemo, useState } from 'react';
import { StyleSheet, View, Text, FlatList } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { AppTextInput } from '@/components/AppTextInput';
import { GlassCard } from '@/components/GlassCard';
import { StatusPill } from '@/components/StatusPill';
import { EmptyState } from '@/components/EmptyState';
import { type, useTheme } from '@/theme';
import { subscribeTeachers } from '@/services/teacherService';
import { Teacher } from '@/types/models';

export function TeachersListScreen() {
  const { colors } = useTheme();
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [query, setQuery] = useState('');

  useEffect(() => subscribeTeachers(setTeachers), []);

  const filtered = useMemo(() => {
    if (!query.trim()) return teachers;
    const q = query.toLowerCase();
    return teachers.filter((t) => t.name.toLowerCase().includes(q));
  }, [teachers, query]);

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow={`${teachers.length} on staff`} title="Teachers" />
      <View style={styles.searchWrap}>
        <AppTextInput label="Search" icon="search-outline" placeholder="Search teachers" value={query} onChangeText={setQuery} />
      </View>
      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.content}
        ListEmptyComponent={<EmptyState icon="person-outline" title="No teachers yet" />}
        renderItem={({ item, index }) => (
          <GlassCard delay={Math.min(index, 10) * 40} style={styles.card}>
            <View style={styles.row}>
              <View style={[styles.avatar, { backgroundColor: colors.goldSoft }]}>
                <Ionicons name="person" size={18} color={colors.goldDeep} />
              </View>
              <View style={{ flex: 1 }}>
                <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>{item.name}</Text>
                <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>{item.qualifications || 'No qualifications listed'}</Text>
              </View>
              <StatusPill label={item.isPaid ? 'Paid' : 'Unpaid'} tone={item.isPaid ? 'approved' : 'pending'} />
            </View>
          </GlassCard>
        )}
      />
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  searchWrap: { paddingHorizontal: 20, paddingTop: 16 },
  content: { paddingHorizontal: 20, paddingBottom: 120 },
  card: { marginBottom: 10 },
  row: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  avatar: { width: 40, height: 40, borderRadius: 13, alignItems: 'center', justifyContent: 'center' },
});
