import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { radius, type, useTheme } from '@/theme';

type Tone = 'approved' | 'pending' | 'rejected' | 'neutral';

export function StatusPill({ label, tone = 'neutral' }: { label: string; tone?: Tone }) {
  const { colors } = useTheme();
  const toneMap: Record<Tone, { bg: string; fg: string }> = {
    approved: { bg: colors.isDark ? 'rgba(76,175,80,0.16)' : 'rgba(46,125,50,0.14)', fg: colors.statusApproved },
    pending: { bg: colors.isDark ? 'rgba(230,184,76,0.16)' : 'rgba(224,165,39,0.16)', fg: colors.statusPending },
    rejected: { bg: colors.isDark ? 'rgba(224,101,90,0.16)' : 'rgba(194,74,63,0.14)', fg: colors.statusRejected },
    neutral: { bg: colors.goldSoft, fg: colors.goldDeep },
  };
  const { bg, fg } = toneMap[tone];
  return (
    <View style={[styles.pill, { backgroundColor: bg }]}>
      <View style={[styles.dot, { backgroundColor: fg }]} />
      <Text style={[type.label, { color: fg }]}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  pill: {
    flexDirection: 'row',
    alignItems: 'center',
    alignSelf: 'flex-start',
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: radius.pill,
    gap: 6,
  },
  dot: { width: 6, height: 6, borderRadius: 3 },
});
