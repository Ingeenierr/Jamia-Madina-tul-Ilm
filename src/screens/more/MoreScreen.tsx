import React from 'react';
import { StyleSheet, View, Text, ScrollView, Pressable } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { radius, type, useTheme, ThemeMode } from '@/theme';
import { useAuth } from '@/context/AuthContext';

const ADMIN_ONLY_LINKS: { label: string; icon: keyof typeof Ionicons.glyphMap; route: string }[] = [
  { label: 'Finance', icon: 'wallet-outline', route: 'Finance' },
  { label: 'Library', icon: 'library-outline', route: 'Library' },
  { label: 'Shop', icon: 'storefront-outline', route: 'Shop' },
];

const SHARED_LINKS: { label: string; icon: keyof typeof Ionicons.glyphMap; route: string }[] = [
  { label: 'Search records', icon: 'search-outline', route: 'Search' },
];

const MODE_OPTIONS: { key: ThemeMode; label: string; icon: keyof typeof Ionicons.glyphMap }[] = [
  { key: 'light', label: 'Light', icon: 'sunny-outline' },
  { key: 'dark', label: 'Dark', icon: 'moon-outline' },
  { key: 'system', label: 'System', icon: 'phone-portrait-outline' },
];

export function MoreScreen({ navigation }: any) {
  const { user, logout } = useAuth();
  const { colors, mode, setMode } = useTheme();
  const links = user?.role === 'ADMIN' ? [...ADMIN_ONLY_LINKS, ...SHARED_LINKS] : SHARED_LINKS;

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow={user?.role === 'ADMIN' ? 'Administrator' : 'Teacher'} title={user?.name ?? 'More'} />
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={[type.label, { color: colors.textMuted, marginBottom: 12 }]}>APPEARANCE</Text>
        <GlassCard style={{ marginBottom: 24 }}>
          <View style={styles.modeRow}>
            {MODE_OPTIONS.map((opt) => {
              const active = mode === opt.key;
              return (
                <Pressable
                  key={opt.key}
                  onPress={() => setMode(opt.key)}
                  style={[
                    styles.modeChip,
                    { borderColor: active ? colors.gold : colors.hairline },
                    active && { backgroundColor: colors.goldSoft },
                  ]}
                >
                  <Ionicons name={opt.icon} size={18} color={active ? colors.goldDeep : colors.textMuted} />
                  <Text style={[type.bodyS, { color: active ? colors.goldDeep : colors.textSecondary, marginTop: 6, fontWeight: active ? '700' : '400' }]}>
                    {opt.label}
                  </Text>
                </Pressable>
              );
            })}
          </View>
        </GlassCard>

        <Text style={[type.label, { color: colors.textMuted, marginBottom: 12 }]}>MODULES</Text>
        <GlassCard style={{ padding: 0 }}>
          {links.map((link, i) => (
            <Pressable
              key={link.label}
              onPress={() =>
                link.route === 'Finance'
                  ? navigation.navigate('Home', { screen: 'Finance' })
                  : navigation.navigate(link.route)
              }
              style={[styles.row, i < links.length - 1 && { borderBottomWidth: 1, borderBottomColor: colors.hairline }]}
            >
              <Ionicons name={link.icon} size={18} color={colors.goldDeep} style={{ marginRight: 12 }} />
              <Text style={[type.bodyL, { flex: 1, color: colors.textPrimary }]}>{link.label}</Text>
              <Ionicons name="chevron-forward" size={18} color={colors.textMuted} />
            </Pressable>
          ))}
        </GlassCard>

        <Pressable style={styles.logoutRow} onPress={logout}>
          <Ionicons name="log-out-outline" size={18} color={colors.statusRejected} />
          <Text style={[type.bodyL, { color: colors.statusRejected, marginLeft: 10 }]}>Sign out</Text>
        </Pressable>
      </ScrollView>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  content: { padding: 20, paddingBottom: 120 },
  modeRow: { flexDirection: 'row', gap: 10 },
  modeChip: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: radius.md,
    borderWidth: 1.5,
    alignItems: 'center',
  },
  row: { flexDirection: 'row', alignItems: 'center', padding: 16 },
  logoutRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', marginTop: 24, padding: 14 },
});
